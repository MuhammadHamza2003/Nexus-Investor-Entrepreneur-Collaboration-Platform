package com.Nexus.Nexus.dto;

import lombok.Data;

@Data
public class TwoFactorResponse {
    
    private boolean success;
    private String message;
    private String otp; // Only for testing purposes
    private long expiresInMinutes;
    
    public TwoFactorResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public TwoFactorResponse(boolean success, String message, String otp, long expiresInMinutes) {
        this.success = success;
        this.message = message;
        this.otp = otp;
        this.expiresInMinutes = expiresInMinutes;
    }
}