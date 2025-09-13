package com.Nexus.Nexus.dto;

import com.Nexus.Nexus.model.PaymentMethodType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO for payment method response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodResponse {
    
    private String id;
    private PaymentMethodType type;
    private String displayName;
    private String lastFourDigits;
    private String brand;
    private String formattedExpiration;
    private boolean isDefault;
    private boolean isActive;
    private boolean isExpired;
    private LocalDateTime createdAt;
}