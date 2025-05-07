package com.example.sparkyaisystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;

/**
 * Exception thrown when a user exceeds their usage limits.
 * This could be due to exceeding the maximum number of requests or tokens in a time window.
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class LimitExceededException extends RuntimeException {

    private final String limitType;
    private final int current;
    private final int max;
    private final String windowType;
    private final LocalDateTime windowResetTime;

    public LimitExceededException(String message) {
        super(message);
        this.limitType = "unknown";
        this.current = 0;
        this.max = 0;
        this.windowType = "unknown";
        this.windowResetTime = null;
    }

    public LimitExceededException(String limitType, int current, int max) {
        super(String.format("Limit exceeded for %s: %d/%d", limitType, current, max));
        this.limitType = limitType;
        this.current = current;
        this.max = max;
        this.windowType = "unknown";
        this.windowResetTime = null;
    }

    public LimitExceededException(String limitType, int current, int max, String windowType, LocalDateTime windowResetTime) {
        super(String.format("Limit exceeded for %s: %d/%d. Window type: %s. Resets at: %s", 
                limitType, current, max, windowType, windowResetTime));
        this.limitType = limitType;
        this.current = current;
        this.max = max;
        this.windowType = windowType;
        this.windowResetTime = windowResetTime;
    }

    public String getLimitType() {
        return limitType;
    }

    public int getCurrent() {
        return current;
    }

    public int getMax() {
        return max;
    }

    public String getWindowType() {
        return windowType;
    }

    public LocalDateTime getWindowResetTime() {
        return windowResetTime;
    }
}
