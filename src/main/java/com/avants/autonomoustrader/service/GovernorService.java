package com.avants.autonomoustrader.service;

import com.avants.autonomoustrader.dto.KiteDto;
import com.avants.autonomoustrader.model.PositionsManifest;
import com.avants.autonomoustrader.model.StrategyManifest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The Governor (Librarian) Service.
 * Loads "The Rules" from strategy.json (read-only) and "The Money" from positions.json (writable).
 * Provides a unified view to the DashboardController without ever mixing the two concerns.
 */
@Service
public class GovernorService {

    private static final Logger log = LoggerFactory.getLogger(GovernorService.class);

    private final String strategyPath;
    private final String positionsPath;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    public GovernorService(
            @Value("${trading.strategy.path:strategy.json}") String strategyPath,
            @Value("${trading.positions.path:positions.json}") String positionsPath,
            ResourceLoader resourceLoader) {
        this.strategyPath = strategyPath;
        this.positionsPath = positionsPath;
        this.resourceLoader = resourceLoader;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Loads the strategy manifest (read-only rules) from strategy.json.
     */
    public StrategyManifest loadStrategy() throws IOException {
        String resourcePath = normalizeResourcePath(strategyPath);
        Resource resource = resourceLoader.getResource(resourcePath);
        log.info("Loading strategy from: {}", strategyPath);
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, StrategyManifest.class);
        } catch (IOException e) {
            log.error("strategy.json not found at: {}", strategyPath);
            throw new IOException("strategy.json not found at: " + strategyPath, e);
        }
    }

    /**
     * Loads the positions manifest (live portfolio) from positions.json.
     */
    public PositionsManifest loadPositions() throws IOException {
        String resourcePath = normalizeResourcePath(positionsPath);
        Resource resource = resourceLoader.getResource(resourcePath);
        log.info("Loading positions from: {}", positionsPath);
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, PositionsManifest.class);
        } catch (IOException e) {
            log.warn("positions.json not found at: {} — returning empty manifest", positionsPath);
            return new PositionsManifest();
        }
    }

    /**
     * Persists updated live portfolio data to positions.json only.
     * Strategy rules in strategy.json are never touched.
     */
    public void savePositions(KiteDto.LivePortfolio livePortfolio) throws IOException {
        PositionsManifest manifest = new PositionsManifest();
        manifest.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        manifest.setLivePortfolio(livePortfolio);

        String resourcePath = normalizeResourcePath(positionsPath);
        Resource resource = resourceLoader.getResource(resourcePath);

        // Writing to a classpath resource (e.g., inside a JAR) is not supported.
        // We attempt to get the File handle if it's a direct file resource.
        try {
            File file = resource.getFile();
            objectMapper.writeValue(file, manifest);
            log.info("positions.json saved to: {} — {} holdings, {} positions",
                    file.getAbsolutePath(),
                    livePortfolio.holdings() != null ? livePortfolio.holdings().size() : 0,
                    livePortfolio.positions() != null ? livePortfolio.positions().size() : 0);
        } catch (IOException e) {
            log.error("Cannot save positions — path '{}' is not a writable file (might be a classpath resource in a JAR)", positionsPath);
            throw new IOException("Cannot save positions to non-writable path: " + positionsPath, e);
        }
    }

    /**
     * Logs a summary of both manifests for quick inspection.
     */
    public void printManifestSummary() throws IOException {
        StrategyManifest strategy = loadStrategy();
        PositionsManifest positions = loadPositions();
        log.info("=== Strategy Manifest Summary ===");
        log.info("Version     : {}", strategy.getStrategyVersion());
        log.info("Last Updated: {}", strategy.getLastUpdated());
        log.info("Universe    : {} ({} symbols)", strategy.getUniverse().name(), strategy.getUniverse().symbols().size());
        log.info("Strategy    : {}", strategy.getTechnicalStrategy().name());
        log.info("=== Positions Manifest Summary ===");
        log.info("Last Updated: {}", positions.getLastUpdated());
        KiteDto.LivePortfolio portfolio = positions.getLivePortfolio();
        if (portfolio != null) {
            log.info("Holdings    : {}", portfolio.holdings() != null ? portfolio.holdings().size() : 0);
            log.info("Positions   : {}", portfolio.positions() != null ? portfolio.positions().size() : 0);
        } else {
            log.info("Portfolio   : (not yet synced)");
        }
        log.info("==================================");
    }

    /**
     * Normalizes a path to work with Spring's ResourceLoader.
     * If the path starts with "classpath:", "file:", "http:", or "https:", it's returned as-is.
     * Otherwise, it's treated as an absolute file path and prefixed with "file:".
     */
    private String normalizeResourcePath(String path) {
        if (path.startsWith("classpath:") || path.startsWith("file:") ||
            path.startsWith("http:") || path.startsWith("https:")) {
            return path;
        }
        // Treat as absolute file path
        return "file:" + path;
    }
}
