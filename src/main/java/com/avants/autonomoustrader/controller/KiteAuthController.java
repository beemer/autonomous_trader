package com.avants.autonomoustrader.controller;

import com.avants.autonomoustrader.service.KiteSessionStore;
import com.avants.autonomoustrader.service.KiteSyncService;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class KiteAuthController {

    private static final Logger log = LoggerFactory.getLogger(KiteAuthController.class);

    private final KiteConnect kiteConnect;
    private final KiteSessionStore sessionStore;
    private final KiteSyncService kiteSyncService;

    @Value("${kite.api-secret}")
    private String apiSecret;

    public KiteAuthController(KiteConnect kiteConnect,
                              KiteSessionStore sessionStore,
                              KiteSyncService kiteSyncService) {
        this.kiteConnect = kiteConnect;
        this.sessionStore = sessionStore;
        this.kiteSyncService = kiteSyncService;
    }

    @GetMapping("/api/auth/callback")
    public String handleCallback(@RequestParam("request_token") String requestToken) {
        try {
            User user = kiteConnect.generateSession(requestToken, apiSecret);
            String accessToken = user.accessToken;
            String publicToken = user.publicToken;

            // Update the singleton so background sync resumes immediately
            kiteConnect.setAccessToken(accessToken);
            if (publicToken != null) {
                kiteConnect.setPublicToken(publicToken);
            }

            // Persist tokens for next startup
            sessionStore.save(accessToken, publicToken);

            // Clear the session-expired flag so the next sync tick proceeds
            kiteSyncService.clearSessionExpired();

            log.info("OAuth handshake successful. Access Token: {}", accessToken);
            return "Success";
        } catch (KiteException | IOException e) {
            log.error("OAuth handshake failed", e);
            return "Error: " + e.getMessage();
        }
    }
}
