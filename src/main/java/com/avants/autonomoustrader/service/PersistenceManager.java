package com.avants.autonomoustrader.service;

import com.avants.autonomoustrader.dto.KiteDto;
import com.avants.autonomoustrader.model.LivePortfolio;
import com.avants.autonomoustrader.model.TradingStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PersistenceManager {

    private static final Logger log = LoggerFactory.getLogger(PersistenceManager.class);

    private final Path strategyPath;
    private final Path positionsPath;
    private final ObjectMapper objectMapper;

    public PersistenceManager(
            @Value("${trading.strategy.path:strategy.json}") String strategyPath,
            @Value("${trading.positions.path:positions.json}") String positionsPath) {
        this.strategyPath = Paths.get(strategyPath);
        this.positionsPath = Paths.get(positionsPath);
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Loads Strategy (The Rules).
     * If missing, it tries to copy a default from classpath to disk.
     */
    public TradingStrategy loadStrategy() throws IOException {
        if (!Files.exists(strategyPath)) {
            log.warn("Strategy file not found at {}. Attempting to seed from defaults...", strategyPath);
            seedDefaultStrategy();
        }
        return objectMapper.readValue(strategyPath.toFile(), TradingStrategy.class);
    }

    /**
     * Loads Positions (The Money).
     * If missing, returns empty - allowing for "Delete to Refresh" testing.
     */
    public LivePortfolio loadPositions() {
        if (!Files.exists(positionsPath)) {
            log.info("positions.json missing - returning empty state.");
            return new LivePortfolio();
        }
        try {
            return objectMapper.readValue(positionsPath.toFile(), LivePortfolio.class);
        } catch (IOException e) {
            log.error("Failed to parse positions.json, returning empty.", e);
            return new LivePortfolio();
        }
    }

    /**
     * Persists only position/portfolio data.
     */
    public void savePositions(KiteDto.LivePortfolio livePortfolio) throws IOException {
        LivePortfolio manifest = new LivePortfolio();
        manifest.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        manifest.setLivePortfolio(livePortfolio);

        objectMapper.writeValue(positionsPath.toFile(), manifest);
        log.info("Saved {} holdings to {}",
                livePortfolio.holdings() != null ? livePortfolio.holdings().size() : 0,
                positionsPath.toAbsolutePath());
    }

    private void seedDefaultStrategy() throws IOException {
        try (var is = getClass().getResourceAsStream("/strategy.json")) {
            if (is != null) {
                Files.copy(is, strategyPath);
                log.info("Seeded default strategy.json from classpath to {}", strategyPath.toAbsolutePath());
            } else {
                throw new IOException("Could not find default strategy.json in classpath resources.");
            }
        }
    }

    public void printSummary() throws IOException {
        var s = loadStrategy();
        var p = loadPositions();
        log.info("=== GOVERNOR SUMMARY ===");
        log.info("Strategy: {} | Version: {}", s.getTechnicalStrategy().name(), s.getStrategyVersion());
        log.info("Holdings: {}", p.getLivePortfolio() != null ? p.getLivePortfolio().holdings().size() : 0);
        log.info("========================");
    }
}