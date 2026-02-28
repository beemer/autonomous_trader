package com.avants.autonomoustrader.dto;

import java.util.List;

public class DashboardDto {

    public record PerformanceStats(
            double dailyPct,
            double weeklyPct,
            double monthlyPct
    ) {}

    public record Holding(
            String symbol,
            double pnl,
            double pnlPct,
            String strategyMatch
    ) {}

    public record Indicator(
            String type,
            int period,
            String source
    ) {}

    public record StrategyRule(
            String condition
    ) {}

    public record StrategyViewer(
            String name,
            String description,
            List<Indicator> indicators,
            List<StrategyRule> entryConditions,
            List<StrategyRule> exitConditions
    ) {}

    public record DashboardResponse(
            PerformanceStats performance,
            List<Holding> holdings,
            StrategyViewer strategy
    ) {}
}
