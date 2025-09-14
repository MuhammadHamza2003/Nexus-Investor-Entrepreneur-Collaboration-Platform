package com.Nexus.Nexus.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity representing a user's saved payment method
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "payment_methods")
public class PaymentMethod {
    
    @Id
    private String id;
    
    @NotNull(message = "User is required")
    @DBRef
    private User user;
    
    @NotNull(message = "Payment method type is required")
    private PaymentMethodType type;
    
    @NotBlank(message = "Display name is required")
    private String displayName; // e.g., "Visa ending in 4242"
    
    // External ID from payment gateway (Stripe customer ID, PayPal account ID, etc.)
    private String externalId;
    
    // Last 4 digits for cards (for display purposes)
    private String lastFourDigits;
    
    // Card brand (Visa, MasterCard, etc.) - only for card types
    private String brand;
    
    // Expiration month/year for cards
    private Integer expirationMonth;
    private Integer expirationYear;
    
    // Whether this is the default payment method
    @Builder.Default
    private boolean isDefault = false;
    
    // Whether this payment method is active
    @Builder.Default
    private boolean isActive = true;
    
    // Additional metadata from payment gateway
    private Map<String, Object> metadata;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Check if this payment method is expired (for cards)
     */
    public boolean isExpired() {
        if (expirationMonth == null || expirationYear == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = LocalDateTime.of(expirationYear, expirationMonth, 1, 0, 0)
            .plusMonths(1).minusDays(1);
            
        return now.isAfter(expiration);
    }
    
    /**
     * Get formatted expiration date
     */
    public String getFormattedExpiration() {
        if (expirationMonth == null || expirationYear == null) {
            return null;
        }
        return String.format("%02d/%d", expirationMonth, expirationYear % 100);
    }
}