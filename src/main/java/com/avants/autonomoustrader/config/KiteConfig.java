package com.avants.autonomoustrader.config;

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
    private String accessToken;

    @Bean
    public KiteConnect kiteConnect() {
        log.info("Initialising KiteConnect client for apiKey: {}...", apiKey.substring(0, Math.min(4, apiKey.length())));
        KiteConnect kiteConnect = new KiteConnect(apiKey, false);
        kiteConnect.setAccessToken(accessToken);
        return kiteConnect;
    }
}
