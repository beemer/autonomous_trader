package com.avants.autonomoustrader.config;

import com.avants.autonomoustrader.service.KiteSessionStore;
import com.zerodhatech.kiteconnect.KiteConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KiteConfig {

    private static final Logger log = LoggerFactory.getLogger(KiteConfig.class);

    @Value("${kite.api-key}")
    private String apiKey;

    @Value("${kite.access-token}")
    private String accessTokenProperty;

    @Bean
    public KiteConnect kiteConnect(KiteSessionStore sessionStore) {
        log.info("Initialising KiteConnect client for apiKey: {}...", apiKey.substring(0, Math.min(4, apiKey.length())));
        KiteConnect kiteConnect = new KiteConnect(apiKey, false);

        // Prefer persisted session token over the property value
        KiteSessionStore.SessionData session = sessionStore.load();
        if (session != null && session.accessToken != null && !session.accessToken.isBlank()) {
            log.info("Restoring access token from persisted session file");
            kiteConnect.setAccessToken(session.accessToken);
            if (session.publicToken != null) {
                kiteConnect.setPublicToken(session.publicToken);
            }
        } else {
            log.info("No persisted session found — using access token from application properties");
            kiteConnect.setAccessToken(accessTokenProperty);
        }

        return kiteConnect;
    }
}
