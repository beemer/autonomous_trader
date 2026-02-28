package com.avants.autonomoustrader.service;

import com.avants.autonomoustrader.dto.CandidateDto;
import com.avants.autonomoustrader.model.TradingStrategy;
import com.avants.autonomoustrader.util.MarketUniverse;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.HistoricalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Technical analysis scanner for identifying trading candidates.
 * Implements EMA-based filtering and ranking logic.
 */
@Service
public class TechnicalScannerService {

    private static final Logger log = LoggerFactory.getLogger(TechnicalScannerService.class);
    private static final int EMA_PERIOD = 200;
    private static final int HISTORICAL_DAYS = 400; // ~1 year of trading days

    private final MarketDataService marketDataService;
    private final PersistenceManager persistenceManager;

    public TechnicalScannerService(MarketDataService marketDataService, PersistenceManager persistenceManager) {
        this.marketDataService = marketDataService;
        this.persistenceManager = persistenceManager;
    }

    /**
     * Scans Nifty 50 stocks for candidates near their EMA 200 levels.
     * Returns stocks in uptrend (Price > EMA200) sorted by smallest distance.
     *
     * @param topK Maximum number of candidates to return
     * @return List of candidates sorted by distance from EMA200 (ascending)
     */
    public List<CandidateDto> scanForCandidates(int topK) throws IOException, KiteException {
        log.info("Starting technical scan for Nifty 50 stocks (topK={})", topK);

        List<String> symbols = MarketUniverse.NIFTY_50;
        Map<String, HistoricalData> candlesMap = marketDataService.fetchHistoricalCandlesForSymbols(
                symbols,
                "NSE",
                "day",
                HISTORICAL_DAYS
        );

        List<CandidateDto> candidates = new ArrayList<>();

        for (Map.Entry<String, HistoricalData> entry : candlesMap.entrySet()) {
            String symbol = entry.getKey();
            HistoricalData candles = entry.getValue();

            if (candles == null || candles.dataArrayList == null || candles.dataArrayList.isEmpty()) {
                log.warn("No candle data for symbol: {}", symbol);
                continue;
            }

            try {
                double ema200 = calculateEMA(candles.dataArrayList, EMA_PERIOD);
                double ltp = candles.dataArrayList.get(candles.dataArrayList.size() - 1).close;
                double distancePct = ((ltp - ema200) / ema200) * 100.0;

                // Filter: Only stocks in uptrend (Price > EMA200)
                if (ltp > ema200) {
                    candidates.add(new CandidateDto(symbol, ltp, ema200, distancePct));
                    log.debug("Candidate found: {} at ₹{} (EMA200: ₹{}, Distance: {:.2f}%)",
                            symbol, ltp, ema200, distancePct);
                }
            } catch (Exception e) {
                log.error("Failed to calculate EMA for {}: {}", symbol, e.getMessage());
            }
        }

        // Sort by smallest distance from EMA200 (ascending)
        candidates.sort(Comparator.comparingDouble(CandidateDto::distancePct));

        List<CandidateDto> topCandidates = candidates.stream()
                .limit(topK)
                .toList();

        log.info("Technical scan complete: {} candidates found, returning top {}", candidates.size(), topCandidates.size());
        return topCandidates;
    }

    /**
     * Calculates Exponential Moving Average (EMA) for a given period.
     * Formula: EMA = (Price × α) + (PrevEMA × (1 - α))
     * where α = 2 / (period + 1)
     *
     * @param candles Historical candle data
     * @param period  EMA period (e.g., 200)
     * @return The most recent EMA value
     */
    private double calculateEMA(List<HistoricalData> candles, int period) {
        if (candles.size() < period) {
            throw new IllegalArgumentException("Not enough data points for EMA calculation");
        }

        double alpha = 2.0 / (period + 1);

        // Start with Simple Moving Average (SMA) for the first EMA value
        double sma = 0.0;
        for (int i = 0; i < period; i++) {
            sma += candles.get(i).close;
        }
        double ema = sma / period;

        // Calculate EMA for remaining data points
        for (int i = period; i < candles.size(); i++) {
            double price = candles.get(i).close;
            ema = (price * alpha) + (ema * (1 - alpha));
        }

        return ema;
    }

    /**
     * Scans for candidates using parameters from strategy.json.
     * Falls back to default topK=10 if not specified in strategy.
     *
     * @return List of top candidates
     */
    public List<CandidateDto> scanWithStrategyParameters() throws IOException, KiteException {
        TradingStrategy strategy = persistenceManager.loadStrategy();

        // Extract topK from strategy if available, otherwise default to 10
        int topK = 10; // Default value

        // Note: You may want to add a topK field to TradingStrategy.RiskParameters
        // For now, we'll use a sensible default
        if (strategy.getRiskParameters() != null) {
            topK = strategy.getRiskParameters().maxOpenPositions();
        }

        log.info("Using strategy parameters: topK={}", topK);
        return scanForCandidates(topK);
    }
}
