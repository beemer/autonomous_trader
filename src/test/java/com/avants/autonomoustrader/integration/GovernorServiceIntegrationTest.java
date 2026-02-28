package com.avants.autonomoustrader.integration;

import com.avants.autonomoustrader.model.TradingManifest;
import com.avants.autonomoustrader.service.GovernorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class GovernorServiceIntegrationTest {

    @Autowired
    private GovernorService governorService;

    @Test
    void contextLoads() {
        assertNotNull(governorService);
    }

    @Test
    void shouldLoadRealManifestFromDisk() throws IOException {
        TradingManifest manifest = governorService.loadManifest();

        assertNotNull(manifest);
        assertNotNull(manifest.getManifestVersion());
        assertNotNull(manifest.getLastUpdated());
        assertNotNull(manifest.getUniverse());
        assertNotNull(manifest.getTechnicalStrategy());
        assertNotNull(manifest.getRiskParameters());
    }

    @Test
    void shouldLoadCorrectUniverseFromRealManifest() throws IOException {
        TradingManifest manifest = governorService.loadManifest();

        assertEquals("Nifty 50", manifest.getUniverse().getName());
        assertEquals("NSE", manifest.getUniverse().getExchange());
        assertFalse(manifest.getUniverse().getSymbols().isEmpty());
        assertEquals(50, manifest.getUniverse().getSymbols().size());
        assertTrue(manifest.getUniverse().getSymbols().contains("RELIANCE"));
        assertTrue(manifest.getUniverse().getSymbols().contains("TCS"));
    }

    @Test
    void shouldLoadCorrectStrategyFromRealManifest() throws IOException {
        TradingManifest manifest = governorService.loadManifest();

        assertEquals("EMA Crossover + MACD Breakout", manifest.getTechnicalStrategy().getName());
        assertFalse(manifest.getTechnicalStrategy().getIndicators().isEmpty());
        assertFalse(manifest.getTechnicalStrategy().getEntryConditions().isEmpty());
        assertFalse(manifest.getTechnicalStrategy().getExitConditions().isEmpty());
    }

    @Test
    void shouldLoadCorrectRiskParametersFromRealManifest() throws IOException {
        TradingManifest manifest = governorService.loadManifest();

        assertEquals(5.0, manifest.getRiskParameters().getMaxCapitalPerTradePct());
        assertEquals(5, manifest.getRiskParameters().getMaxOpenPositions());
        assertEquals(1.5, manifest.getRiskParameters().getStopLossPct());
        assertEquals(3.0, manifest.getRiskParameters().getTargetPct());
    }

    @Test
    void shouldPrintManifestSummaryWithoutException() {
        assertDoesNotThrow(() -> governorService.printManifestSummary());
    }
}
