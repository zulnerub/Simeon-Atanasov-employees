package com.simeonatanasov.employees.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Exposes a {@link Clock} bean for injection into components that need the current time.
 */
@Configuration
public class TimeConfig {

    /**
     * Provides a system clock using the JVM's default time zone.
     */
    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
