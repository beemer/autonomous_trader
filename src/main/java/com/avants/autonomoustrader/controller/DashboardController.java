package com.avants.autonomoustrader.controller;

import com.avants.autonomoustrader.dto.DashboardDto;
import com.avants.autonomoustrader.dto.KiteDto;
import com.avants.autonomoustrader.model.TradingManifest;
import com.avants.autonomoustrader.service.GovernorService;
import com.avants.autonomoustrader.service.KiteSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final KiteSyncService kiteSyncService;
    private final GovernorService governorService;

    public DashboardController(KiteSyncService kiteSyncService, GovernorService governorService) {
        this.kiteSyncService = kiteSyncService;
        this.governorService = governorService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto.DashboardResponse> getDashboard() {
        log.info("Serving dashboard data from manifest");
        try {
            TradingManifest manifest = governorService.loadManifest();
            KiteDto.LivePortfolio livePortfolio = manifest.getLivePortfolio();
            TradingManifest.TechnicalStrategy ts = manifest.getTechnicalStrategy();

            // Build holdings from live portfolio
            List<DashboardDto.Holding> holdings;
            double totalPnl = 0.0;
            if (livePortfolio != null && livePortfolio.holdings() != null) {
                holdings = livePortfolio.holdings().stream()
                        .map(h -> {
                            double cost = h.averagePrice() * h.quantity();
                            double pnlPct = cost > 0 ? (h.pnl() / cost) * 100.0 : 0.0;
                            return new DashboardDto.Holding(h.tradingSymbol(), h.pnl(), pnlPct, "LIVE");
                        })
                        .toList();
                totalPnl = livePortfolio.holdings().stream().mapToDouble(KiteDto.HoldingDto::pnl).sum();
            } else {
                log.warn("No live portfolio in manifest — sync may not have run yet");
                holdings = List.of();
            }

            // Performance: derive daily from total PnL (placeholder pct until historical data is available)
            double dailyPct = 0.0;
            double weeklyPct = 0.0;
            double monthlyPct = 0.0;
            if (livePortfolio != null && livePortfolio.holdings() != null) {
                double totalCost = livePortfolio.holdings().stream()
                        .mapToDouble(h -> h.averagePrice() * h.quantity())
                        .sum();
                dailyPct = totalCost > 0 ? (totalPnl / totalCost) * 100.0 : 0.0;
            }
            var performance = new DashboardDto.PerformanceStats(dailyPct, weeklyPct, monthlyPct);

            // Strategy from manifest
            List<DashboardDto.Indicator> indicators = ts.getIndicators().stream()
                    .map(i -> new DashboardDto.Indicator(i.getType(), i.getPeriod(), i.getSource()))
                    .toList();
            List<DashboardDto.StrategyRule> entryConditions = ts.getEntryConditions().stream()
                    .map(DashboardDto.StrategyRule::new)
                    .toList();
            List<DashboardDto.StrategyRule> exitConditions = ts.getExitConditions().stream()
                    .map(DashboardDto.StrategyRule::new)
                    .toList();
            var strategy = new DashboardDto.StrategyViewer(
                    ts.getName(),
                    ts.getDescription(),
                    indicators,
                    entryConditions,
                    exitConditions
            );

            return ResponseEntity.ok(new DashboardDto.DashboardResponse(performance, holdings, strategy));
        } catch (IOException e) {
            log.error("Failed to load manifest for dashboard", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/portfolio")
    public ResponseEntity<KiteDto.LivePortfolio> getPortfolio() {
        log.info("Serving live portfolio data");
        KiteDto.LivePortfolio portfolio = kiteSyncService.getLivePortfolio();
        if (portfolio == null) {
            log.warn("Live portfolio not yet available — sync may not have run");
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(portfolio);
    }
}
