package com.Nexus.Nexus.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "two_factor_auth")
public class TwoFactorAuth {
    
    @Id
    private String id;
    
    private String userId;
    private String email;
    private String otp;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean verified;
    private int attempts;
    private String purpose; // LOGIN, REGISTER, PASSWORD_RESET
    
    public TwoFactorAuth() {
        this.createdAt = LocalDateTime.now();
        this.verified = false;
        this.attempts = 0;
    }
    
    public TwoFactorAuth(String userId, String email, String otp, LocalDateTime expiresAt, String purpose) {
        this();
        this.userId = userId;
        this.email = email;
        this.otp = otp;
        this.expiresAt = expiresAt;
        this.purpose = purpose;
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isMaxAttemptsReached() {
        return attempts >= 3;
    }
}