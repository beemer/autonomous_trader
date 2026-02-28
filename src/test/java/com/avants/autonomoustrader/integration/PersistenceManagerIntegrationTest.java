package com.avants.autonomoustrader.integration;

import com.avants.autonomoustrader.model.LivePortfolio;
import com.avants.autonomoustrader.model.TradingStrategy;
import com.avants.autonomoustrader.service.PersistenceManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PersistenceManagerIntegrationTest {

    @Autowired
    private PersistenceManager persistenceManager;

    @Test
    void contextLoads() {
        assertNotNull(persistenceManager);
    }

    @Test
    void shouldLoadRealStrategyFromDisk() throws IOException {
        TradingStrategy strategy = persistenceManager.loadStrategy();

        assertNotNull(strategy);
        assertNotNull(strategy.getStrategyVersion());
        assertNotNull(strategy.getLastUpdated());
        assertNotNull(strategy.getUniverse());
        assertNotNull(strategy.getTechnicalStrategy());
        assertNotNull(strategy.getRiskParameters());
    }

    @Test
    void shouldLoadCorrectUniverseFromRealStrategy() throws IOException {
        TradingStrategy strategy = persistenceManager.loadStrategy();

        assertEquals("Nifty 50", strategy.getUniverse().name());
        assertEquals("NSE", strategy.getUniverse().exchange());
        assertFalse(strategy.getUniverse().symbols().isEmpty());
        assertEquals(50, strategy.getUniverse().symbols().size());
        assertTrue(strategy.getUniverse().symbols().contains("RELIANCE"));
        assertTrue(strategy.getUniverse().symbols().contains("TCS"));
    }

    @Test
    void shouldLoadCorrectStrategyFromRealManifest() throws IOException {
        TradingStrategy strategy = persistenceManager.loadStrategy();

        assertEquals("EMA Crossover + MACD Breakout", strategy.getTechnicalStrategy().name());
        assertFalse(strategy.getTechnicalStrategy().indicators().isEmpty());
        assertFalse(strategy.getTechnicalStrategy().entryConditions().isEmpty());
        assertFalse(strategy.getTechnicalStrategy().exitConditions().isEmpty());
    }

    @Test
    void shouldLoadCorrectRiskParametersFromRealManifest() throws IOException {
        TradingStrategy strategy = persistenceManager.loadStrategy();

        assertEquals(5.0, strategy.getRiskParameters().maxCapitalPerTradePct());
        assertEquals(5, strategy.getRiskParameters().maxOpenPositions());
        assertEquals(1.5, strategy.getRiskParameters().stopLossPct());
        assertEquals(3.0, strategy.getRiskParameters().targetPct());
    }

    @Test
    void shouldSaveAndLoadPositionsRoundTrip() throws IOException {
        com.avants.autonomoustrader.dto.KiteDto.LivePortfolio portfolio =
                new com.avants.autonomoustrader.dto.KiteDto.LivePortfolio(java.util.List.of(), java.util.List.of());
        persistenceManager.savePositions(portfolio);

        LivePortfolio loaded = persistenceManager.loadPositions();
        assertNotNull(loaded);
        assertNotNull(loaded.getLastUpdated());
    }
}
