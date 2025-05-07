package com.example.sparkyaisystem.scheduler;

import com.example.sparkyaisystem.service.LimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for automatically resetting expired user limits.
 * This ensures that user limits are properly reset at the end of their window period.
 */
@Component
@EnableScheduling
@Slf4j
public class LimitResetScheduler {

    private final LimitService limitService;

    public LimitResetScheduler(LimitService limitService) {
        this.limitService = limitService;
    }

    /**
     * Scheduled task to reset expired limits.
     * Runs every 15 minutes to check for and reset any limits that have expired.
     */
    @Scheduled(fixedRate = 15 * 60 * 1000) // Run every 15 minutes
    public void resetExpiredLimits() {
        log.info("Running scheduled task to reset expired limits");
        try {
            limitService.resetExpiredLimits();
            log.info("Successfully reset expired limits");
        } catch (Exception e) {
            log.error("Error resetting expired limits: {}", e.getMessage(), e);
        }
    }
}