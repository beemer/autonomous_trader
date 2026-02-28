package com.avants.autonomoustrader.controller;

import com.avants.autonomoustrader.dto.CandidateDto;
import com.avants.autonomoustrader.service.TechnicalScannerService;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * REST API controller for investment advisor functionality.
 * Provides technical analysis recommendations to the React UI.
 */
@RestController
@RequestMapping("/api/v1/advice")
public class AdvisorController {

    private static final Logger log = LoggerFactory.getLogger(AdvisorController.class);

    private final TechnicalScannerService technicalScannerService;

    public AdvisorController(TechnicalScannerService technicalScannerService) {
        this.technicalScannerService = technicalScannerService;
    }

    /**
     * Scans Nifty 50 stocks and returns top candidates based on EMA 200 analysis.
     * Used by the React UI Investment Advisor tab.
     *
     * @param topK Optional parameter to limit number of results (default from strategy.json)
     * @return List of candidate stocks sorted by distance from EMA 200
     */
    @GetMapping("/top-candidates")
    public ResponseEntity<List<CandidateDto>> getTopCandidates(
            @RequestParam(required = false) Integer topK) {

        log.info("Received request for top candidates (topK={})", topK);

        try {
            List<CandidateDto> candidates;

            if (topK != null) {
                candidates = technicalScannerService.scanForCandidates(topK);
            } else {
                candidates = technicalScannerService.scanWithStrategyParameters();
            }

            log.info("Returning {} candidates to UI", candidates.size());
            return ResponseEntity.ok(candidates);

        } catch (IOException e) {
            log.error("Failed to scan for candidates: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        } catch (KiteException e) {
            log.error("Kite API error while scanning: {}", e.getMessage(), e);
            return ResponseEntity.status(503).build(); // Service Unavailable
        }
    }
}
