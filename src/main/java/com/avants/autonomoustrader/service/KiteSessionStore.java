package com.avants.autonomoustrader.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * KiteSessionStore — persists Kite access/public tokens to a local JSON file
 * so that the app can resume authenticated sessions across restarts without
 * manual URL fudging.
 */
@Service
public class KiteSessionStore {

    private static final Logger log = LoggerFactory.getLogger(KiteSessionStore.class);
    private static final String SESSION_FILE = ".kite_session.json";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SessionData {
        public String accessToken;
        public String publicToken;

        public SessionData() {}

        public SessionData(String accessToken, String publicToken) {
            this.accessToken = accessToken;
            this.publicToken = publicToken;
        }
    }

    public void save(String accessToken, String publicToken) {
        try {
            objectMapper.writeValue(new File(SESSION_FILE), new SessionData(accessToken, publicToken));
            log.info("Kite session saved to {}", SESSION_FILE);
        } catch (IOException e) {
            log.error("Failed to save Kite session to {}", SESSION_FILE, e);
        }
    }

    public SessionData load() {
        File file = new File(SESSION_FILE);
        if (!file.exists()) {
            log.info("No Kite session file found at {}", SESSION_FILE);
            return null;
        }
        try {
            SessionData data = objectMapper.readValue(file, SessionData.class);
            log.info("Loaded Kite session from {}", SESSION_FILE);
            return data;
        } catch (IOException e) {
            log.warn("Failed to read Kite session from {} — will require fresh login", SESSION_FILE, e);
            return null;
        }
    }
}
