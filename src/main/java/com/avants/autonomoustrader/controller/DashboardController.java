package com.avants.autonomoustrader.controller;

import com.avants.autonomoustrader.dto.DashboardDto;
import com.avants.autonomoustrader.dto.KiteDto;
import com.avants.autonomoustrader.model.LivePortfolio;
import com.avants.autonomoustrader.model.TradingStrategy;
import com.avants.autonomoustrader.service.PersistenceManager;
import com.avants.autonomoustrader.service.KiteSyncService;
import com.zerodhatech.kiteconnect.KiteConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
    private final PersistenceManager governorService;
    private final KiteConnect kiteConnect;

    public DashboardController(KiteSyncService kiteSyncService, PersistenceManager governorService, KiteConnect kiteConnect) {
        this.kiteSyncService = kiteSyncService;
        this.governorService = governorService;
        this.kiteConnect = kiteConnect;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto.DashboardResponse> getDashboard() {
        String accessToken = kiteConnect.getAccessToken();
        if (accessToken == null || accessToken.isBlank()
                || accessToken.equals("placeholder")
                || accessToken.equals("your_access_token_here")
                || kiteSyncService.isSessionExpired()) {
            log.warn("Dashboard request rejected — no active Kite session");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Serving dashboard — loading strategy and positions...");
        try {
            TradingStrategy strategy = governorService.loadStrategy();
            LivePortfolio positions = governorService.loadPositions();

            KiteDto.LivePortfolio livePortfolio = positions.getLivePortfolio();
            TradingStrategy.TechnicalStrategy ts = strategy.getTechnicalStrategy();
            TradingStrategy.RiskParameters riskParameters = strategy.getRiskParameters();

            // Build holdings from live portfolio
            List<DashboardDto.Holding> holdings;
            double totalPnl = 0.0;
            if (livePortfolio != null && livePortfolio.holdings() != null) {
                double targetPct = riskParameters != null ? riskParameters.targetPct() : 3.0;
                holdings = livePortfolio.holdings().stream()
                        .map(h -> {
                            double cost = h.averagePrice() * h.quantity();
                            double pnlPct = cost > 0 ? (h.pnl() / cost) * 100.0 : 0.0;
                            String strategyMatch;
                            if (pnlPct >= targetPct) {
                                strategyMatch = "STRONG MATCH";
                            } else if (pnlPct > 0) {
                                strategyMatch = "PARTIAL MATCH";
                            } else {
                                strategyMatch = "NO MATCH";
                            }
                            return new DashboardDto.Holding(h.tradingSymbol(), h.pnl(), pnlPct, strategyMatch);
                        })
                        .toList();
                totalPnl = livePortfolio.holdings().stream().mapToDouble(KiteDto.HoldingDto::pnl).sum();
            } else {
                log.warn("No live portfolio in positions.json — sync may not have run yet");
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

            // Strategy from strategy.json
            List<DashboardDto.Indicator> indicators = ts.indicators().stream()
                    .map(i -> new DashboardDto.Indicator(i.type(), i.period(), i.source()))
                    .toList();
            List<DashboardDto.StrategyRule> entryConditions = ts.entryConditions().stream()
                    .map(DashboardDto.StrategyRule::new)
                    .toList();
            List<DashboardDto.StrategyRule> exitConditions = ts.exitConditions().stream()
                    .map(DashboardDto.StrategyRule::new)
                    .toList();
            var strategyViewer = new DashboardDto.StrategyViewer(
                    ts.name(),
                    ts.description(),
                    indicators,
                    entryConditions,
                    exitConditions
            );

            return ResponseEntity.ok(new DashboardDto.DashboardResponse(performance, holdings, strategyViewer));
        } catch (IOException e) {
            log.error("Failed to load manifests for dashboard", e);
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
