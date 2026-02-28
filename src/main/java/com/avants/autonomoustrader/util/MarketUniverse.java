package com.avants.autonomoustrader.util;

import java.util.List;

/**
 * Market universe definitions containing hardcoded lists of trading symbols.
 */
public class MarketUniverse {

    /**
     * Nifty 50 NSE trading symbols as of 2026.
     */
    public static final List<String> NIFTY_50 = List.of(
            "RELIANCE", "TCS", "HDFCBANK", "INFY", "ICICIBANK",
            "HINDUNILVR", "ITC", "SBIN", "BHARTIARTL", "KOTAKBANK",
            "LT", "AXISBANK", "ASIANPAINT", "MARUTI", "TITAN",
            "SUNPHARMA", "ULTRACEMCO", "BAJFINANCE", "WIPRO", "NESTLEIND",
            "POWERGRID", "NTPC", "TECHM", "HCLTECH", "ONGC",
            "TATAMOTORS", "TATASTEEL", "JSWSTEEL", "ADANIENT", "ADANIPORTS",
            "COALINDIA", "DIVISLAB", "DRREDDY", "CIPLA", "APOLLOHOSP",
            "BAJAJFINSV", "BAJAJ-AUTO", "EICHERMOT", "HEROMOTOCO", "M&M",
            "BRITANNIA", "GRASIM", "HINDALCO", "INDUSINDBK", "SBILIFE",
            "HDFCLIFE", "BPCL", "IOC", "UPL", "TATACONSUM"
    );

    private MarketUniverse() {
        // Utility class
    }
}
