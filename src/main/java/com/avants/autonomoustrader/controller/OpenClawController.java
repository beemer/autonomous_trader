package com.avants.autonomoustrader.controller;

import com.avants.autonomoustrader.dto.CandidateDto;
import com.avants.autonomoustrader.service.TechnicalScannerService;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * REST API controller for OpenClaw LLM agent integration.
 * Provides simplified, LLM-friendly summaries of market analysis.
 */
@RestController
@RequestMapping("/api/v1/openclaw")
public class OpenClawController {

    private static final Logger log = LoggerFactory.getLogger(OpenClawController.class);

    private final TechnicalScannerService technicalScannerService;

    public OpenClawController(TechnicalScannerService technicalScannerService) {
        this.technicalScannerService = technicalScannerService;
    }

    /**
     * Returns a plain-text summary of top stock recommendations for LLM consumption.
     * Optimized for OpenClaw agent to quickly understand market opportunities.
     *
     * Example output:
     * "Top 5 candidates near EMA 200:
     * 1. RELIANCE at ₹2,500.00 (0.23% from EMA 200)
     * 2. TCS at ₹3,450.00 (0.45% from EMA 200)
     * ..."
     *
     * @return Plain text summary
     */
    @GetMapping(value = "/summary", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getSummary() {
        log.info("OpenClaw agent requesting market summary");

        try {
            List<CandidateDto> candidates = technicalScannerService.scanWithStrategyParameters();

            if (candidates.isEmpty()) {
                return ResponseEntity.ok("No candidates found meeting the EMA 200 criteria at this time.");
            }

            StringBuilder summary = new StringBuilder();
            summary.append("Top ").append(candidates.size()).append(" candidates near EMA 200:\n\n");

            for (int i = 0; i < candidates.size(); i++) {
                CandidateDto candidate = candidates.get(i);
                summary.append(String.format("%d. %s at ₹%.2f (%.2f%% from EMA 200)\n",
                        i + 1,
                        candidate.symbol(),
                        candidate.currentPrice(),
                        candidate.distancePct()));
            }

            summary.append("\nAll candidates are in uptrend (Price > EMA 200).");
            summary.append("\nCloser to EMA 200 indicates potential bounce opportunity.");

            log.info("Returning summary to OpenClaw agent with {} candidates", candidates.size());
            return ResponseEntity.ok(summary.toString());

        } catch (IOException e) {
            log.error("Failed to generate summary for OpenClaw: {}", e.getMessage(), e);
            return ResponseEntity.ok("Error: Unable to fetch market data. " + e.getMessage());
        } catch (KiteException e) {
            log.error("Kite API error while generating OpenClaw summary: {}", e.getMessage(), e);
            return ResponseEntity.ok("Error: Market data service unavailable. " + e.getMessage());
        }
    }

    /**
     * Returns a JSON summary for programmatic LLM consumption.
     * Alternative to plain text for structured parsing.
     *
     * @return JSON with summary and top pick
     */
    @GetMapping(value = "/summary-json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSummaryJson() {
        log.info("OpenClaw agent requesting JSON market summary");

        try {
            List<CandidateDto> candidates = technicalScannerService.scanWithStrategyParameters();

            if (candidates.isEmpty()) {
                return ResponseEntity.ok(new SummaryResponse(
                        "No candidates found",
                        null,
                        0
                ));
            }

            CandidateDto topPick = candidates.get(0);
            String summaryText = String.format("Top pick is %s at ₹%.2f (%.2f%% from EMA 200)",
                    topPick.symbol(), topPick.currentPrice(), topPick.distancePct());

            log.info("Returning JSON summary to OpenClaw: {}", summaryText);
            return ResponseEntity.ok(new SummaryResponse(
                    summaryText,
                    topPick,
                    candidates.size()
            ));

        } catch (IOException e) {
            log.error("Failed to generate JSON summary for OpenClaw: {}", e.getMessage(), e);
            return ResponseEntity.ok(new SummaryResponse(
                    "Error: " + e.getMessage(),
                    null,
                    0
            ));
        } catch (KiteException e) {
            log.error("Kite API error while generating OpenClaw JSON summary: {}", e.getMessage(), e);
            return ResponseEntity.ok(new SummaryResponse(
                    "Error: Market data unavailable - " + e.getMessage(),
                    null,
                    0
            ));
        }
    }

    /**
     * Simple response record for JSON endpoint.
     */
    public record SummaryResponse(
            String summary,
            CandidateDto topPick,
            int totalCandidates
    ) {
    }
}
