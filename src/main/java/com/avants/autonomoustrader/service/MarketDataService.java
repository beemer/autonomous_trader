package com.avants.autonomoustrader.service;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.HistoricalData;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.Tick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Market data service for fetching instruments and historical candle data.
 * Provides infrastructure for multi-symbol scanning and candidate discovery.
 */
@Service
public class MarketDataService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataService.class);

    private final KiteConnect kiteConnect;
    private Map<String, String> symbolToInstrumentTokenMap;

    public MarketDataService(KiteConnect kiteConnect) {
        this.kiteConnect = kiteConnect;
    }

    /**
     * Maps trading symbols to their instrument tokens for a given exchange.
     * Caches the result for subsequent calls.
     *
     * @param symbols  List of trading symbols (e.g., ["RELIANCE", "TCS"])
     * @param exchange Exchange code (e.g., "NSE")
     * @return Map of symbol to instrument_token
     */
    public Map<String, String> mapSymbolsToInstrumentTokens(List<String> symbols, String exchange) throws IOException, KiteException {
        if (symbolToInstrumentTokenMap == null) {
            log.info("Fetching instruments from Kite for exchange: {}", exchange);
            List<Instrument> instruments = kiteConnect.getInstruments(exchange);

            symbolToInstrumentTokenMap = instruments.stream()
                    .filter(instrument -> symbols.contains(instrument.tradingsymbol))
                    .collect(Collectors.toMap(
                            instrument -> instrument.tradingsymbol,
                            instrument -> String.valueOf(instrument.instrument_token),
                            (existing, replacement) -> existing // Keep first match if duplicates
                    ));

            log.info("Mapped {} symbols to instrument tokens", symbolToInstrumentTokenMap.size());
        }

        return symbolToInstrumentTokenMap;
    }

    /**
     * Fetches historical candles for a given instrument token.
     *
     * @param instrumentToken The instrument token from Kite
     * @param interval        Candle interval (e.g., "day", "5minute", "15minute")
     * @param daysBack        Number of days to look back from today
     * @return HistoricalData containing the candle data
     */
    public HistoricalData fetchHistoricalCandles(String instrumentToken, String interval, int daysBack) throws IOException, KiteException {
        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusDays(daysBack);

        Date from = Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date to = Date.from(toDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        log.debug("Fetching historical candles for token {} from {} to {}", instrumentToken, fromDate, toDate);

        HistoricalData candles = kiteConnect.getHistoricalData(
                from,
                to,
                instrumentToken,
                interval,
                false,
                false
        );

        log.debug("Retrieved {} candles for instrument token {}", candles.dataArrayList.size(), instrumentToken);

        return candles;
    }

    /**
     * Fetches historical candles for multiple symbols in batch.
     * Includes a small delay to respect Kite API rate limits (3 requests/sec).
     *
     * @param symbols  List of trading symbols
     * @param exchange Exchange code
     * @param interval Candle interval
     * @param daysBack Number of days to look back
     * @return Map of symbol to historical candles
     */
    public Map<String, HistoricalData> fetchHistoricalCandlesForSymbols(
            List<String> symbols,
            String exchange,
            String interval,
            int daysBack) throws IOException, KiteException {

        Map<String, String> instrumentTokens;
        try {
            instrumentTokens = mapSymbolsToInstrumentTokens(symbols, exchange);
        } catch (Exception e) {
            log.error("Failed to map instrument tokens for batch scan", e);
            return Collections.emptyMap();
        }

        Map<String, HistoricalData> result = new HashMap<>();

        for (int i = 0; i < symbols.size(); i++) {
            String symbol = symbols.get(i);
            String instrumentToken = instrumentTokens.get(symbol);
            if (instrumentToken == null) {
                log.warn("No instrument token found for symbol: {}", symbol);
                continue;
            }

            // Throttling: 350ms sleep between requests to stay within 3 req/sec limit
            if (i > 0) {
                try {
                    Thread.sleep(350);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Throttling interrupted for symbol {}", symbol);
                    break;
                }
            }

            // Retry logic (3 attempts with backoff)
            int attempts = 0;
            boolean success = false;
            while (attempts < 3 && !success) {
                attempts++;
                try {
                    HistoricalData candles = fetchHistoricalCandles(instrumentToken, interval, daysBack);
                    result.put(symbol, candles);
                    success = true;
                } catch (Exception e) {
                    if (attempts < 3) {
                        long backoff = attempts * 500L;
                        log.warn("Attempt {} failed for {}: {}. Retrying in {}ms...", attempts, symbol, e.getMessage(), backoff);
                        try {
                            Thread.sleep(backoff);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } else {
                        log.error("Failed to fetch candles for {} after {} attempts", symbol, attempts, e);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Clears the cached instrument token map. Useful for refreshing data.
     */
    public void clearCache() {
        symbolToInstrumentTokenMap = null;
        log.info("Cleared instrument token cache");
    }
}
