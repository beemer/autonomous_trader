package com.avants.autonomoustrader.integration;

import com.avants.autonomoustrader.model.StrategyManifest;
import com.avants.autonomoustrader.service.GovernorService;
import com.zerodhatech.kiteconnect.KiteConnect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class GovernorServiceIntegrationTest {

    @Autowired
    private GovernorService governorService;

    @MockBean
    private KiteConnect kiteConnect;

    @Test
    void contextLoads() {
        assertNotNull(governorService);
    }

    @Test
    void shouldLoadRealStrategyFromDisk() throws IOException {
        StrategyManifest strategy = governorService.loadStrategy();

        assertNotNull(strategy);
        assertNotNull(strategy.getStrategyVersion());
        assertNotNull(strategy.getLastUpdated());
        assertNotNull(strategy.getUniverse());
        assertNotNull(strategy.getTechnicalStrategy());
        assertNotNull(strategy.getRiskParameters());
    }

    @Test
    void shouldLoadCorrectUniverseFromRealStrategy() throws IOException {
        StrategyManifest strategy = governorService.loadStrategy();

        assertEquals("Nifty 50", strategy.getUniverse().name());
        assertEquals("NSE", strategy.getUniverse().exchange());
        assertFalse(strategy.getUniverse().symbols().isEmpty());
        assertEquals(50, strategy.getUniverse().symbols().size());
        assertTrue(strategy.getUniverse().symbols().contains("RELIANCE"));
        assertTrue(strategy.getUniverse().symbols().contains("TCS"));
    }

    @Test
    void shouldLoadCorrectStrategyFromRealManifest() throws IOException {
        StrategyManifest strategy = governorService.loadStrategy();

        assertEquals("EMA Crossover + MACD Breakout", strategy.getTechnicalStrategy().name());
        assertFalse(strategy.getTechnicalStrategy().indicators().isEmpty());
        assertFalse(strategy.getTechnicalStrategy().entryConditions().isEmpty());
        assertFalse(strategy.getTechnicalStrategy().exitConditions().isEmpty());
    }

    @Test
    void shouldLoadCorrectRiskParametersFromRealManifest() throws IOException {
        StrategyManifest strategy = governorService.loadStrategy();

        assertEquals(5.0, strategy.getRiskParameters().maxCapitalPerTradePct());
        assertEquals(5, strategy.getRiskParameters().maxOpenPositions());
        assertEquals(1.5, strategy.getRiskParameters().stopLossPct());
        assertEquals(3.0, strategy.getRiskParameters().targetPct());
    }

    @Test
    void shouldPrintManifestSummaryWithoutException() {
        assertDoesNotThrow(() -> governorService.printManifestSummary());
    }
}
