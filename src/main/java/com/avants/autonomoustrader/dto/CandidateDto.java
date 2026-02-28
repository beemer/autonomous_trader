package com.avants.autonomoustrader.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Candidate recommendation for technical analysis scanning.
 * Represents a stock that meets entry criteria based on EMA distance.
 *
 * @param symbol      Trading symbol (e.g., RELIANCE)
 * @param currentPrice         Last traded price
 * @param ema200      200-period Exponential Moving Average
 * @param distancePct Percentage distance from EMA200 (positive = above, negative = below)
 */
public record CandidateDto(
        String symbol,
        @JsonProperty("currentPrice") double currentPrice,
        double ema200,
        double distancePct
) {
}
