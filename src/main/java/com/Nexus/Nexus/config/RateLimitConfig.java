package com.Nexus.Nexus.config;

import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class RateLimitConfig {
    
    /**
     * Simple rate limiting configuration
     */
    public static class RateLimitInfo {
        private int attempts;
        private LocalDateTime windowStart;
        private final int maxAttempts;
        private final int windowMinutes;
        
        public RateLimitInfo(int maxAttempts, int windowMinutes) {
            this.maxAttempts = maxAttempts;
            this.windowMinutes = windowMinutes;
            this.attempts = 0;
            this.windowStart = LocalDateTime.now();
        }
        
        public synchronized boolean isAllowed() {
            LocalDateTime now = LocalDateTime.now();
            
            // Reset window if expired
            if (windowStart.plusMinutes(windowMinutes).isBefore(now)) {
                attempts = 0;
                windowStart = now;
            }
            
            // Check if under limit
            if (attempts < maxAttempts) {
                attempts++;
                return true;
            }
            
            return false;
        }
        
        public synchronized int getRemainingAttempts() {
            return Math.max(0, maxAttempts - attempts);
        }
        
        public synchronized long getSecondsUntilReset() {
            LocalDateTime resetTime = windowStart.plusMinutes(windowMinutes);
            LocalDateTime now = LocalDateTime.now();
            
            if (resetTime.isAfter(now)) {
                return java.time.Duration.between(now, resetTime).getSeconds();
            }
            return 0;
        }
    }
}