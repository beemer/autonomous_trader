package com.avants.autonomoustrader.service;

import com.avants.autonomoustrader.model.TradingManifest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

    private static final String MANIFEST_PATH = "trading_manifest.json";
    private final ObjectMapper objectMapper;

    public GovernorService() {
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
        File manifestFile = new File(MANIFEST_PATH);
        if (!manifestFile.exists()) {
            throw new IOException("trading_manifest.json not found at: " + manifestFile.getAbsolutePath());
        }
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
        objectMapper.writeValue(new File(MANIFEST_PATH), manifest);
    }

    /**
     * Prints a summary of the current manifest to stdout for quick inspection.
     */
    public void printManifestSummary() throws IOException {
        TradingManifest manifest = loadManifest();
        System.out.println("=== Trading Manifest Summary ===");
        System.out.println("Version     : " + manifest.getManifestVersion());
        System.out.println("Last Updated: " + manifest.getLastUpdated());
        System.out.println("Universe    : " + manifest.getUniverse().getName()
                + " (" + manifest.getUniverse().getSymbols().size() + " symbols)");
        System.out.println("Strategy    : " + manifest.getTechnicalStrategy().getName());
        System.out.println("================================");
    }
}
