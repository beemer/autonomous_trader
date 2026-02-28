package com.avants.autonomoustrader.service;

import com.avants.autonomoustrader.model.TradingManifest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The Governor (Librarian) Service.
 * Responsible for reading, writing, and managing "The Bible" — trading_manifest.json.
 * This manifest defines the trading Universe and Technical Strategy consumed by OpenClaw (The Executioner).
 */
@Service
public class GovernorService {

    private static final Logger log = LoggerFactory.getLogger(GovernorService.class);

    private String manifestPath;
    private final ObjectMapper objectMapper;

    public GovernorService() {
        this("trading_manifest.json");
    }

    public GovernorService(String manifestPath) {
        this.manifestPath = manifestPath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Loads the current trading manifest from disk.
     *
     * @return the deserialized TradingManifest
     * @throws IOException if the file cannot be read or parsed
     */
    public TradingManifest loadManifest() throws IOException {
        File manifestFile = new File(manifestPath);
        if (!manifestFile.exists()) {
            log.error("trading_manifest.json not found at: {}", manifestFile.getAbsolutePath());
            throw new IOException("trading_manifest.json not found at: " + manifestFile.getAbsolutePath());
        }
        log.info("Loading manifest from: {}", manifestFile.getAbsolutePath());
        return objectMapper.readValue(manifestFile, TradingManifest.class);
    }

    /**
     * Persists the trading manifest to disk, stamping the current timestamp.
     *
     * @param manifest the TradingManifest to save
     * @throws IOException if the file cannot be written
     */
    public void saveManifest(TradingManifest manifest) throws IOException {
        manifest.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        objectMapper.writeValue(new File(manifestPath), manifest);
        log.info("Manifest saved to: {} (version: {}, lastUpdated: {})", manifestPath, manifest.getManifestVersion(), manifest.getLastUpdated());
    }

    /**
     * Logs a summary of the current manifest for quick inspection.
     */
    public void printManifestSummary() throws IOException {
        TradingManifest manifest = loadManifest();
        log.info("=== Trading Manifest Summary ===");
        log.info("Version     : {}", manifest.getManifestVersion());
        log.info("Last Updated: {}", manifest.getLastUpdated());
        log.info("Universe    : {} ({} symbols)", manifest.getUniverse().getName(), manifest.getUniverse().getSymbols().size());
        log.info("Strategy    : {}", manifest.getTechnicalStrategy().getName());
        log.info("================================");
    }
}
