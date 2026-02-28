package com.avants.autonomoustrader.service;

import com.avants.autonomoustrader.model.TradingManifest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GovernorServiceTest {

    @TempDir
    Path tempDir;

    private GovernorService governorService;
    private File manifestFile;

    @BeforeEach
    void setUp() {
        manifestFile = tempDir.resolve("trading_manifest.json").toFile();
        governorService = new GovernorService(manifestFile.getAbsolutePath());
    }

    private TradingManifest buildSampleManifest() {
        TradingManifest manifest = new TradingManifest();
        manifest.setManifestVersion("1.0.0");
        manifest.setLastUpdated("2026-02-28T12:00:00");

        TradingManifest.Universe universe = new TradingManifest.Universe();
        universe.setName("Nifty 50");
        universe.setExchange("NSE");
        universe.setSymbols(List.of("RELIANCE", "TCS", "INFY"));
        manifest.setUniverse(universe);

        TradingManifest.TechnicalStrategy strategy = new TradingManifest.TechnicalStrategy();
        strategy.setName("EMA Crossover + MACD Breakout");
        strategy.setDescription("Test strategy");
        strategy.setEntryConditions(List.of("EMA_9 > EMA_200"));
        strategy.setExitConditions(List.of("Stop loss hit"));

        TradingManifest.Indicator indicator = new TradingManifest.Indicator();
        indicator.setType("EMA");
        indicator.setPeriod(9);
        indicator.setSource("close");
        strategy.setIndicators(List.of(indicator));
        manifest.setTechnicalStrategy(strategy);

        TradingManifest.RiskParameters risk = new TradingManifest.RiskParameters();
        risk.setMaxCapitalPerTradePct(5.0);
        risk.setMaxOpenPositions(5);
        risk.setStopLossPct(1.5);
        risk.setTargetPct(3.0);
        manifest.setRiskParameters(risk);

        return manifest;
    }

    @Test
    void shouldSaveAndLoadManifestRoundTrip() throws IOException {
        TradingManifest original = buildSampleManifest();
        governorService.saveManifest(original);

        TradingManifest loaded = governorService.loadManifest();

        assertEquals("1.0.0", loaded.getManifestVersion());
        assertEquals("Nifty 50", loaded.getUniverse().getName());
        assertEquals("NSE", loaded.getUniverse().getExchange());
        assertEquals(3, loaded.getUniverse().getSymbols().size());
        assertEquals("EMA Crossover + MACD Breakout", loaded.getTechnicalStrategy().getName());
        assertEquals(5.0, loaded.getRiskParameters().getMaxCapitalPerTradePct());
        assertEquals(5, loaded.getRiskParameters().getMaxOpenPositions());
        assertEquals(1.5, loaded.getRiskParameters().getStopLossPct());
        assertEquals(3.0, loaded.getRiskParameters().getTargetPct());
    }

    @Test
    void shouldStampLastUpdatedOnSave() throws IOException {
        TradingManifest manifest = buildSampleManifest();
        manifest.setLastUpdated("old-timestamp");

        governorService.saveManifest(manifest);

        TradingManifest loaded = governorService.loadManifest();
        assertNotNull(loaded.getLastUpdated());
        assertNotEquals("old-timestamp", loaded.getLastUpdated());
    }

    @Test
    void shouldThrowIOExceptionWhenManifestFileMissing() {
        assertThrows(IOException.class, () -> governorService.loadManifest());
    }

    @Test
    void shouldPersistSymbolsCorrectly() throws IOException {
        TradingManifest manifest = buildSampleManifest();
        manifest.getUniverse().setSymbols(List.of("RELIANCE", "TCS", "HDFCBANK", "INFY", "ICICIBANK"));

        governorService.saveManifest(manifest);
        TradingManifest loaded = governorService.loadManifest();

        assertEquals(5, loaded.getUniverse().getSymbols().size());
        assertTrue(loaded.getUniverse().getSymbols().contains("HDFCBANK"));
    }

    @Test
    void shouldPersistIndicatorsCorrectly() throws IOException {
        TradingManifest manifest = buildSampleManifest();
        TradingManifest.Indicator macd = new TradingManifest.Indicator();
        macd.setType("MACD");
        macd.setPeriod(12);
        macd.setSource("close");
        manifest.getTechnicalStrategy().setIndicators(List.of(
                manifest.getTechnicalStrategy().getIndicators().get(0), macd
        ));

        governorService.saveManifest(manifest);
        TradingManifest loaded = governorService.loadManifest();

        assertEquals(2, loaded.getTechnicalStrategy().getIndicators().size());
        assertEquals("MACD", loaded.getTechnicalStrategy().getIndicators().get(1).getType());
        assertEquals(12, loaded.getTechnicalStrategy().getIndicators().get(1).getPeriod());
    }

    @Test
    void shouldWriteValidJsonToFile() throws IOException {
        TradingManifest manifest = buildSampleManifest();
        governorService.saveManifest(manifest);

        assertTrue(manifestFile.exists());
        assertTrue(manifestFile.length() > 0);

        // Verify it's valid JSON by parsing it independently
        ObjectMapper mapper = new ObjectMapper();
        TradingManifest parsed = mapper.readValue(manifestFile, TradingManifest.class);
        assertNotNull(parsed);
        assertEquals("1.0.0", parsed.getManifestVersion());
    }

    @Test
    void shouldPrintManifestSummaryWithoutException() throws IOException {
        TradingManifest manifest = buildSampleManifest();
        governorService.saveManifest(manifest);

        assertDoesNotThrow(() -> governorService.printManifestSummary());
    }
}
