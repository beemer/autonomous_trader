package com.avants.autonomoustrader.model;

import com.avants.autonomoustrader.dto.KiteDto;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Mutable model for positions.json — "The Money".
 * Contains the live portfolio (holdings and positions) synced from Zerodha.
 * Only KiteSyncService writes to this file; strategy rules are never touched here.
 */
public class LivePortfolio {

    @JsonProperty("last_updated")
    private String lastUpdated;

    @JsonProperty("live_portfolio")
    private KiteDto.LivePortfolio livePortfolio;

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }

    public KiteDto.LivePortfolio getLivePortfolio() { return livePortfolio; }
    public void setLivePortfolio(KiteDto.LivePortfolio livePortfolio) { this.livePortfolio = livePortfolio; }
}
