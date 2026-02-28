package com.avants.autonomoustrader.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Read-only model for strategy.json — "The Rules".
 * Contains the trading universe, technical strategy, and risk parameters.
 * This file is never written by the sync service.
 */
public class TradingStrategy {

    @JsonProperty("strategy_version")
    private String strategyVersion;

    @JsonProperty("last_updated")
    private String lastUpdated;

    @JsonProperty("universe")
    private Universe universe;

    @JsonProperty("technical_strategy")
    private TechnicalStrategy technicalStrategy;

    @JsonProperty("risk_parameters")
    private RiskParameters riskParameters;

    public String getStrategyVersion() { return strategyVersion; }
    public void setStrategyVersion(String strategyVersion) { this.strategyVersion = strategyVersion; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }

    public Universe getUniverse() { return universe; }
    public void setUniverse(Universe universe) { this.universe = universe; }

    public TechnicalStrategy getTechnicalStrategy() { return technicalStrategy; }
    public void setTechnicalStrategy(TechnicalStrategy technicalStrategy) { this.technicalStrategy = technicalStrategy; }

    public RiskParameters getRiskParameters() { return riskParameters; }
    public void setRiskParameters(RiskParameters riskParameters) { this.riskParameters = riskParameters; }

    // --- Nested Records (Java 21) ---

    public record Universe(
            @JsonProperty("name") String name,
            @JsonProperty("exchange") String exchange,
            @JsonProperty("symbols") List<String> symbols
    ) {}

    public record Indicator(
            @JsonProperty("type") String type,
            @JsonProperty("period") int period,
            @JsonProperty("source") String source
    ) {}

    public record TechnicalStrategy(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("indicators") List<Indicator> indicators,
            @JsonProperty("entry_conditions") List<String> entryConditions,
            @JsonProperty("exit_conditions") List<String> exitConditions
    ) {}

    public record RiskParameters(
            @JsonProperty("max_capital_per_trade_pct") double maxCapitalPerTradePct,
            @JsonProperty("max_open_positions") int maxOpenPositions,
            @JsonProperty("stop_loss_pct") double stopLossPct,
            @JsonProperty("target_pct") double targetPct
    ) {}
}
