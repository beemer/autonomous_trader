package com.avants.autonomoustrader.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TradingManifestTest {

    @Test
    void shouldSetAndGetManifestVersion() {
        TradingManifest manifest = new TradingManifest();
        manifest.setManifestVersion("1.0.0");
        assertEquals("1.0.0", manifest.getManifestVersion());
    }

    @Test
    void shouldSetAndGetLastUpdated() {
        TradingManifest manifest = new TradingManifest();
        manifest.setLastUpdated("2026-02-28T12:00:00");
        assertEquals("2026-02-28T12:00:00", manifest.getLastUpdated());
    }

    @Test
    void shouldSetAndGetUniverse() {
        TradingManifest manifest = new TradingManifest();
        TradingManifest.Universe universe = new TradingManifest.Universe();
        universe.setName("Nifty 50");
        universe.setExchange("NSE");
        universe.setSymbols(List.of("RELIANCE", "TCS", "INFY"));
        manifest.setUniverse(universe);

        assertNotNull(manifest.getUniverse());
        assertEquals("Nifty 50", manifest.getUniverse().getName());
        assertEquals("NSE", manifest.getUniverse().getExchange());
        assertEquals(3, manifest.getUniverse().getSymbols().size());
        assertTrue(manifest.getUniverse().getSymbols().contains("RELIANCE"));
    }

    @Test
    void shouldSetAndGetTechnicalStrategy() {
        TradingManifest manifest = new TradingManifest();
        TradingManifest.TechnicalStrategy strategy = new TradingManifest.TechnicalStrategy();
        strategy.setName("EMA Crossover + MACD Breakout");
        strategy.setDescription("Test description");
        strategy.setEntryConditions(List.of("EMA_9 > EMA_200"));
        strategy.setExitConditions(List.of("Stop loss hit"));

        TradingManifest.Indicator indicator = new TradingManifest.Indicator();
        indicator.setType("EMA");
        indicator.setPeriod(9);
        indicator.setSource("close");
        strategy.setIndicators(List.of(indicator));

        manifest.setTechnicalStrategy(strategy);

        assertNotNull(manifest.getTechnicalStrategy());
        assertEquals("EMA Crossover + MACD Breakout", manifest.getTechnicalStrategy().getName());
        assertEquals(1, manifest.getTechnicalStrategy().getIndicators().size());
        assertEquals("EMA", manifest.getTechnicalStrategy().getIndicators().get(0).getType());
        assertEquals(9, manifest.getTechnicalStrategy().getIndicators().get(0).getPeriod());
        assertEquals("close", manifest.getTechnicalStrategy().getIndicators().get(0).getSource());
        assertEquals(1, manifest.getTechnicalStrategy().getEntryConditions().size());
        assertEquals(1, manifest.getTechnicalStrategy().getExitConditions().size());
    }

    @Test
    void shouldSetAndGetRiskParameters() {
        TradingManifest manifest = new TradingManifest();
        TradingManifest.RiskParameters risk = new TradingManifest.RiskParameters();
        risk.setMaxCapitalPerTradePct(5.0);
        risk.setMaxOpenPositions(5);
        risk.setStopLossPct(1.5);
        risk.setTargetPct(3.0);
        manifest.setRiskParameters(risk);

        assertNotNull(manifest.getRiskParameters());
        assertEquals(5.0, manifest.getRiskParameters().getMaxCapitalPerTradePct());
        assertEquals(5, manifest.getRiskParameters().getMaxOpenPositions());
        assertEquals(1.5, manifest.getRiskParameters().getStopLossPct());
        assertEquals(3.0, manifest.getRiskParameters().getTargetPct());
    }

    @Test
    void shouldHandleNullFieldsGracefully() {
        TradingManifest manifest = new TradingManifest();
        assertNull(manifest.getManifestVersion());
        assertNull(manifest.getLastUpdated());
        assertNull(manifest.getUniverse());
        assertNull(manifest.getTechnicalStrategy());
        assertNull(manifest.getRiskParameters());
    }
}
