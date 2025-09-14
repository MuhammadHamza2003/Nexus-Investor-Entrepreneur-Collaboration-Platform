package com.Nexus.Nexus.service;

import com.Nexus.Nexus.config.RateLimitConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    
    private final ConcurrentHashMap<String, RateLimitConfig.RateLimitInfo> rateLimitCache = new ConcurrentHashMap<>();
    
    /**
     * Checks if the request should be rate limited
     * @param request HTTP request to get IP address from
     * @param type Type of rate limit (login, general)
     * @return true if request should be allowed, false if rate limited
     */
    public boolean isAllowed(HttpServletRequest request, String type) {
        String clientIp = getClientIpAddress(request);
        RateLimitConfig.RateLimitInfo rateLimitInfo = getRateLimitInfo(clientIp, type);
        
        return rateLimitInfo.isAllowed();
    }
    
    /**
     * Gets remaining attempts for the client
     * @param request HTTP request to get IP address from
     * @param type Type of rate limit (login, general)
     * @return Number of remaining attempts
     */
    public int getRemainingAttempts(HttpServletRequest request, String type) {
        String clientIp = getClientIpAddress(request);
        RateLimitConfig.RateLimitInfo rateLimitInfo = getRateLimitInfo(clientIp, type);
        
        return rateLimitInfo.getRemainingAttempts();
    }
    
    /**
     * Gets the time until reset in seconds
     * @param request HTTP request to get IP address from
     * @param type Type of rate limit (login, general)
     * @return Seconds until rate limit resets
     */
    public long getSecondsUntilReset(HttpServletRequest request, String type) {
        String clientIp = getClientIpAddress(request);
        RateLimitConfig.RateLimitInfo rateLimitInfo = getRateLimitInfo(clientIp, type);
        
        return rateLimitInfo.getSecondsUntilReset();
    }
    
    /**
     * Gets or creates rate limit info for a client and type
     */
    private RateLimitConfig.RateLimitInfo getRateLimitInfo(String clientIp, String type) {
        String key = clientIp + "_" + type;
        
        return rateLimitCache.computeIfAbsent(key, k -> {
            if ("login".equals(type)) {
                // 5 attempts per 15 minutes for login/register
                return new RateLimitConfig.RateLimitInfo(5, 15);
            } else {
                // 100 attempts per minute for general API
                return new RateLimitConfig.RateLimitInfo(100, 1);
            }
        });
    }
    
    /**
     * Extracts client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}