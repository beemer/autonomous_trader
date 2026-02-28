package com.avants.autonomoustrader.controller;

import com.avants.autonomoustrader.dto.DashboardDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @GetMapping
    public DashboardDto.DashboardResponse getDashboard() {
        log.info("Serving dashboard data");

        var performance = new DashboardDto.PerformanceStats(1.24, -0.87, 5.63);

        var holdings = List.of(
                new DashboardDto.Holding("RELIANCE", 4250.00, 2.83, "STRONG MATCH"),
                new DashboardDto.Holding("TCS", -1800.00, -1.12, "PARTIAL MATCH"),
                new DashboardDto.Holding("HDFCBANK", 3100.00, 1.95, "STRONG MATCH"),
                new DashboardDto.Holding("INFY", -950.00, -0.74, "NO MATCH"),
                new DashboardDto.Holding("ICICIBANK", 2750.00, 2.10, "STRONG MATCH")
        );

        var indicators = List.of(
                new DashboardDto.Indicator("EMA", 9, "close"),
                new DashboardDto.Indicator("EMA", 200, "close"),
                new DashboardDto.Indicator("MACD", 12, "close"),
                new DashboardDto.Indicator("MACD_SIGNAL", 9, "close")
        );

        var entryConditions = List.of(
                new DashboardDto.StrategyRule("EMA_9 > EMA_200"),
                new DashboardDto.StrategyRule("MACD_LINE crosses_above MACD_SIGNAL"),
                new DashboardDto.StrategyRule("VOLUME > 1.5x 20-period average volume")
        );

        var exitConditions = List.of(
                new DashboardDto.StrategyRule("MACD_LINE crosses_below MACD_SIGNAL"),
                new DashboardDto.StrategyRule("Price closes below EMA_9"),
                new DashboardDto.StrategyRule("Stop loss hit"),
                new DashboardDto.StrategyRule("Target hit")
        );

        var strategy = new DashboardDto.StrategyViewer(
                "EMA Crossover + MACD Breakout",
                "Enter long when 9 EMA is above 200 EMA and MACD line crosses above signal line, confirming bullish momentum breakout.",
                indicators,
                entryConditions,
                exitConditions
        );

        return new DashboardDto.DashboardResponse(performance, holdings, strategy);
    }
}
