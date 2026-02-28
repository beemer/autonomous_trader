package com.avants.autonomoustrader.service;

import com.avants.autonomoustrader.dto.KiteDto;
import com.avants.autonomoustrader.model.PositionsManifest;
import com.avants.autonomoustrader.model.StrategyManifest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
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

    public GovernorService(
            @Value("${trading.strategy.path:strategy.json}") String strategyPath,
            @Value("${trading.positions.path:positions.json}") String positionsPath) {
        this.strategyPath = strategyPath;
        this.positionsPath = positionsPath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Loads the strategy manifest (read-only rules) from strategy.json.
     */
    public StrategyManifest loadStrategy() throws IOException {
        File file = new File(strategyPath);
        if (!file.exists()) {
            log.error("strategy.json not found at: {}", file.getAbsolutePath());
            throw new IOException("strategy.json not found at: " + file.getAbsolutePath());
        }
        log.info("Loading strategy from: {}", file.getAbsolutePath());
        return objectMapper.readValue(file, StrategyManifest.class);
    }

    /**
     * Loads the positions manifest (live portfolio) from positions.json.
     */
    public PositionsManifest loadPositions() throws IOException {
        File file = new File(positionsPath);
        if (!file.exists()) {
            log.warn("positions.json not found at: {} — returning empty manifest", file.getAbsolutePath());
            return new PositionsManifest();
        }
        log.info("Loading positions from: {}", file.getAbsolutePath());
        return objectMapper.readValue(file, PositionsManifest.class);
    }

    /**
     * Persists updated live portfolio data to positions.json only.
     * Strategy rules in strategy.json are never touched.
     */
    public void savePositions(KiteDto.LivePortfolio livePortfolio) throws IOException {
        PositionsManifest manifest = new PositionsManifest();
        manifest.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        manifest.setLivePortfolio(livePortfolio);
        objectMapper.writeValue(new File(positionsPath), manifest);
        log.info("positions.json saved — {} holdings, {} positions",
                livePortfolio.holdings() != null ? livePortfolio.holdings().size() : 0,
                livePortfolio.positions() != null ? livePortfolio.positions().size() : 0);
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
}
