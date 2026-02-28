package com.avants.autonomoustrader.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableScheduling
@Profile("!test")
public class SchedulingConfig {
}
