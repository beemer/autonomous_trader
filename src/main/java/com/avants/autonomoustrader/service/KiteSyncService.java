package com.avants.autonomoustrader.service;

import com.avants.autonomoustrader.dto.KiteDto;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Holding;
import com.zerodhatech.models.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * KiteSyncService — The Nervous System.
 * Fetches live Holdings and Positions from Zerodha in parallel using CompletableFuture
 * on Java 21 Virtual Threads, then updates positions.json every minute.
 * Strictly isolated: only writes to positions.json; strategy.json is never touched.
 */
@Service
public class KiteSyncService {

    private static final Logger log = LoggerFactory.getLogger(KiteSyncService.class);

    private final KiteConnect kiteConnect;
    private final GovernorService governorService;
    private final Executor virtualThreadExecutor;

    @Value("${kite.api-key}")
    private String apiKey;

    /** True when the last sync failed due to an expired/invalid token (HTTP 403). */
    private final AtomicBoolean sessionExpired = new AtomicBoolean(false);

    public KiteSyncService(KiteConnect kiteConnect,
                           GovernorService governorService,
                           @Qualifier("virtualThreadExecutor") Executor virtualThreadExecutor) {
        this.kiteConnect = kiteConnect;
        this.governorService = governorService;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    /**
     * Returns true if the last sync attempt failed due to an expired Kite session.
     */
    public boolean isSessionExpired() {
        return sessionExpired.get();
    }

    /**
     * Called by KiteAuthController after a successful OAuth handshake to clear the
     * expired-session flag so the next scheduled sync can proceed normally.
     */
    public void clearSessionExpired() {
        sessionExpired.set(false);
        log.info("Session-expired flag cleared — sync will resume on next tick");
    }

    /**
     * Scheduled task: runs every minute to fetch live portfolio data from Zerodha
     * and persist it into positions.json only. strategy.json is never modified.
     */
    @Scheduled(fixedDelay = 60_000)
    public void syncPortfolio() {
        String accessToken = kiteConnect.getAccessToken();
        if (accessToken == null || accessToken.equals("placeholder") || accessToken.equals("your_access_token_here")) {
            log.warn("Skipping portfolio sync — access token is not set. Complete OAuth handshake via the UI login flow.");
            return;
        }

        log.info("Starting Kite portfolio sync...");
        try {
            CompletableFuture<List<KiteDto.HoldingDto>> holdingsFuture =
                    CompletableFuture.supplyAsync(this::fetchHoldings, virtualThreadExecutor);
            CompletableFuture<List<KiteDto.PositionDto>> positionsFuture =
                    CompletableFuture.supplyAsync(this::fetchPositions, virtualThreadExecutor);

            CompletableFuture.allOf(holdingsFuture, positionsFuture).join();

            List<KiteDto.HoldingDto> holdings = holdingsFuture.get();
            List<KiteDto.PositionDto> positions = positionsFuture.get();

            KiteDto.LivePortfolio livePortfolio = new KiteDto.LivePortfolio(holdings, positions);
            governorService.savePositions(livePortfolio);

            sessionExpired.set(false);
            log.info("Portfolio sync complete — {} holdings, {} net positions", holdings.size(), positions.size());
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof KiteException ke && ke.code == 403) {
                log.warn("Kite session expired (HTTP 403) — UI login required");
                sessionExpired.set(true);
            } else {
                log.error("Portfolio sync failed", e);
            }
        }
    }

    /**
     * Returns the latest live portfolio from positions.json, or null if not yet synced.
     */
    public KiteDto.LivePortfolio getLivePortfolio() {
        return governorService.loadPositions().getLivePortfolio();
    }

    private List<KiteDto.HoldingDto> fetchHoldings() {
        try {
            log.debug("Fetching holdings from Kite...");
            List<Holding> holdings = kiteConnect.getHoldings();
            List<KiteDto.HoldingDto> result = holdings.stream()
                    .map(h -> new KiteDto.HoldingDto(
                            h.tradingSymbol,
                            h.exchange,
                            h.product,
                            h.quantity,
                            h.t1Quantity,
                            h.averagePrice != null ? h.averagePrice : 0.0,
                            h.lastPrice != null ? h.lastPrice : 0.0,
                            h.pnl != null ? h.pnl : 0.0
                    ))
                    .toList();
            log.info("Fetched {} holdings from Kite", result.size());
            return result;
        } catch (KiteException | IOException e) {
            log.error("Failed to fetch holdings from Kite", e);
            return List.of();
        }
    }

    private List<KiteDto.PositionDto> fetchPositions() {
        try {
            log.debug("Fetching positions from Kite...");
            List<Position> netPositions = kiteConnect.getPositions().get("net");
            if (netPositions == null) {
                log.warn("No net positions returned from Kite");
                return List.of();
            }
            List<KiteDto.PositionDto> result = netPositions.stream()
                    .map(p -> new KiteDto.PositionDto(
                            p.tradingSymbol,
                            p.exchange,
                            p.product,
                            p.netQuantity,
                            p.averagePrice,
                            p.lastPrice != null ? p.lastPrice : 0.0,
                            p.closePrice != null ? p.closePrice : 0.0,
                            p.pnl != null ? p.pnl : 0.0,
                            p.unrealised != null ? p.unrealised : 0.0,
                            p.realised != null ? p.realised : 0.0,
                            p.m2m != null ? p.m2m : 0.0
                    ))
                    .toList();
            log.info("Fetched {} net positions from Kite", result.size());
            return result;
        } catch (KiteException | IOException e) {
            log.error("Failed to fetch positions from Kite", e);
            return List.of();
        }
    }
}
