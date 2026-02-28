package com.avants.autonomoustrader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutonomousTraderApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutonomousTraderApplication.class, args);
    }
}
