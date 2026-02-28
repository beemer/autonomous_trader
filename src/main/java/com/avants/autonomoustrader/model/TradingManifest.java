package com.avants.autonomoustrader.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TradingManifest {

    @JsonProperty("manifest_version")
    private String manifestVersion;

    @JsonProperty("last_updated")
    private String lastUpdated;

    @JsonProperty("universe")
    private Universe universe;

    @JsonProperty("technical_strategy")
    private TechnicalStrategy technicalStrategy;

    @JsonProperty("risk_parameters")
    private RiskParameters riskParameters;

    // Getters and Setters

    public String getManifestVersion() { return manifestVersion; }
    public void setManifestVersion(String manifestVersion) { this.manifestVersion = manifestVersion; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }

    public Universe getUniverse() { return universe; }
    public void setUniverse(Universe universe) { this.universe = universe; }

    public TechnicalStrategy getTechnicalStrategy() { return technicalStrategy; }
    public void setTechnicalStrategy(TechnicalStrategy technicalStrategy) { this.technicalStrategy = technicalStrategy; }

    public RiskParameters getRiskParameters() { return riskParameters; }
    public void setRiskParameters(RiskParameters riskParameters) { this.riskParameters = riskParameters; }

    // --- Nested Classes ---

    public static class Universe {
        @JsonProperty("name")
        private String name;

        @JsonProperty("exchange")
        private String exchange;

        @JsonProperty("symbols")
        private List<String> symbols;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getExchange() { return exchange; }
        public void setExchange(String exchange) { this.exchange = exchange; }

        public List<String> getSymbols() { return symbols; }
        public void setSymbols(List<String> symbols) { this.symbols = symbols; }
    }

    public static class TechnicalStrategy {
        @JsonProperty("name")
        private String name;

        @JsonProperty("description")
        private String description;

        @JsonProperty("indicators")
        private List<Indicator> indicators;

        @JsonProperty("entry_conditions")
        private List<String> entryConditions;

        @JsonProperty("exit_conditions")
        private List<String> exitConditions;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public List<Indicator> getIndicators() { return indicators; }
        public void setIndicators(List<Indicator> indicators) { this.indicators = indicators; }

        public List<String> getEntryConditions() { return entryConditions; }
        public void setEntryConditions(List<String> entryConditions) { this.entryConditions = entryConditions; }

        public List<String> getExitConditions() { return exitConditions; }
        public void setExitConditions(List<String> exitConditions) { this.exitConditions = exitConditions; }
    }

    public static class Indicator {
        @JsonProperty("type")
        private String type;

        @JsonProperty("period")
        private int period;

        @JsonProperty("source")
        private String source;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public int getPeriod() { return period; }
        public void setPeriod(int period) { this.period = period; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }

    public static class RiskParameters {
        @JsonProperty("max_capital_per_trade_pct")
        private double maxCapitalPerTradePct;

        @JsonProperty("max_open_positions")
        private int maxOpenPositions;

        @JsonProperty("stop_loss_pct")
        private double stopLossPct;

        @JsonProperty("target_pct")
        private double targetPct;

        public double getMaxCapitalPerTradePct() { return maxCapitalPerTradePct; }
        public void setMaxCapitalPerTradePct(double maxCapitalPerTradePct) { this.maxCapitalPerTradePct = maxCapitalPerTradePct; }

        public int getMaxOpenPositions() { return maxOpenPositions; }
        public void setMaxOpenPositions(int maxOpenPositions) { this.maxOpenPositions = maxOpenPositions; }

        public double getStopLossPct() { return stopLossPct; }
        public void setStopLossPct(double stopLossPct) { this.stopLossPct = stopLossPct; }

        public double getTargetPct() { return targetPct; }
        public void setTargetPct(double targetPct) { this.targetPct = targetPct; }
    }
}
