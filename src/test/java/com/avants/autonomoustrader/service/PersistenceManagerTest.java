package com.avants.autonomoustrader.service;

import com.avants.autonomoustrader.dto.KiteDto;
import com.avants.autonomoustrader.model.LivePortfolio;
import com.avants.autonomoustrader.model.TradingStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersistenceManagerTest {

    @TempDir
    Path tempDir;

    private PersistenceManager persistenceManager;
    private File positionsFile;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        File strategyFile = tempDir.resolve("strategy.json").toFile();
        positionsFile = tempDir.resolve("positions.json").toFile();
        persistenceManager = new PersistenceManager(strategyFile.getAbsolutePath(), positionsFile.getAbsolutePath());
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Write a minimal strategy.json for tests that need it
        TradingStrategy strategy = buildSampleStrategy();
        objectMapper.writeValue(strategyFile, strategy);
    }

    private TradingStrategy buildSampleStrategy() {
        TradingStrategy strategy = new TradingStrategy();
        strategy.setStrategyVersion("1.0.0");
        strategy.setLastUpdated("2026-02-28T12:00:00");
        strategy.setUniverse(new TradingStrategy.Universe("Nifty 50", "NSE", List.of("RELIANCE", "TCS", "INFY")));
        strategy.setTechnicalStrategy(new TradingStrategy.TechnicalStrategy(
                "EMA Crossover + MACD Breakout",
                "Test strategy",
                List.of(new TradingStrategy.Indicator("EMA", 9, "close")),
                List.of("EMA_9 > EMA_200"),
                List.of("Stop loss hit")
        ));
        strategy.setRiskParameters(new TradingStrategy.RiskParameters(5.0, 5, 1.5, 3.0));
        return strategy;
    }

    private KiteDto.LivePortfolio buildSamplePortfolio() {
        KiteDto.HoldingDto holding = new KiteDto.HoldingDto("RELIANCE", "NSE", "CNC", 10, 0, 1400.0, 1450.0, 500.0);
        return new KiteDto.LivePortfolio(List.of(holding), List.of());
    }

    @Test
    void shouldLoadStrategyFromDisk() throws IOException {
        TradingStrategy loaded = persistenceManager.loadStrategy();

        assertNotNull(loaded);
        assertEquals("1.0.0", loaded.getStrategyVersion());
        assertEquals("Nifty 50", loaded.getUniverse().name());
        assertEquals("NSE", loaded.getUniverse().exchange());
        assertEquals(3, loaded.getUniverse().symbols().size());
        assertEquals("EMA Crossover + MACD Breakout", loaded.getTechnicalStrategy().name());
        assertEquals(5.0, loaded.getRiskParameters().maxCapitalPerTradePct());
        assertEquals(5, loaded.getRiskParameters().maxOpenPositions());
        assertEquals(1.5, loaded.getRiskParameters().stopLossPct());
        assertEquals(3.0, loaded.getRiskParameters().targetPct());
    }

    @Test
    void shouldReturnEmptyPositionsWhenFileMissing() throws IOException {
        LivePortfolio positions = persistenceManager.loadPositions();
        assertNotNull(positions);
        assertNull(positions.getLivePortfolio());
    }

    @Test
    void shouldSaveAndLoadPositionsRoundTrip() throws IOException {
        KiteDto.LivePortfolio portfolio = buildSamplePortfolio();
        persistenceManager.savePositions(portfolio);

        LivePortfolio loaded = persistenceManager.loadPositions();
        assertNotNull(loaded);
        assertNotNull(loaded.getLastUpdated());
        assertNotNull(loaded.getLivePortfolio());
        assertEquals(1, loaded.getLivePortfolio().holdings().size());
        assertEquals("RELIANCE", loaded.getLivePortfolio().holdings().get(0).tradingSymbol());
    }

    @Test
    void shouldWriteValidJsonToPositionsFile() throws IOException {
        persistenceManager.savePositions(buildSamplePortfolio());

        assertTrue(positionsFile.exists());
        assertTrue(positionsFile.length() > 0);

        LivePortfolio parsed = objectMapper.readValue(positionsFile, LivePortfolio.class);
        assertNotNull(parsed);
        assertNotNull(parsed.getLivePortfolio());
    }


}
