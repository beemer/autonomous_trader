package com.avants.autonomoustrader.controller;

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

    @Value("${kite.api-secret}")
    private String apiSecret;

    public KiteAuthController(KiteConnect kiteConnect) {
        this.kiteConnect = kiteConnect;
    }

    @GetMapping("/api/auth/callback")
    public String handleCallback(@RequestParam("request_token") String requestToken) {
        try {
            User user = kiteConnect.generateSession(requestToken, apiSecret);
            String accessToken = user.accessToken;
            kiteConnect.setAccessToken(accessToken);
            log.info("OAuth handshake successful. Access Token: {}", accessToken);
            return "Success";
        } catch (KiteException | IOException e) {
            log.error("OAuth handshake failed", e);
            return "Error: " + e.getMessage();
        }
    }
}
