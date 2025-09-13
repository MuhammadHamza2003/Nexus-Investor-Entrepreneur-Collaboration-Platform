package com.Nexus.Nexus.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a user's wallet/balance in the system
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "wallets")
public class Wallet {
    
    @Id
    private String id;
    
    @NotNull(message = "User is required")
    @DBRef
    @Indexed(unique = true)
    private User user;
    
    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
    
    // Currency code (default USD)
    @Builder.Default
    private String currency = "USD";
    
    // Total amount deposited (lifetime)
    @Builder.Default
    private BigDecimal totalDeposited = BigDecimal.ZERO;
    
    // Total amount withdrawn (lifetime)
    @Builder.Default
    private BigDecimal totalWithdrawn = BigDecimal.ZERO;
    
    // Total amount transferred to others (lifetime)
    @Builder.Default
    private BigDecimal totalTransferred = BigDecimal.ZERO;
    
    // Total amount received from others (lifetime)
    @Builder.Default
    private BigDecimal totalReceived = BigDecimal.ZERO;
    
    // Whether the wallet is active
    @Builder.Default
    private boolean isActive = true;
    
    // Whether the wallet is frozen (no transactions allowed)
    @Builder.Default
    private boolean isFrozen = false;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Add amount to balance
     */
    public void addToBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.balance = this.balance.add(amount);
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Subtract amount from balance
     */
    public void subtractFromBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0 && this.balance.compareTo(amount) >= 0) {
            this.balance = this.balance.subtract(amount);
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Check if wallet has sufficient balance for a transaction
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        return !isFrozen && isActive && this.balance.compareTo(amount) >= 0;
    }
    
    /**
     * Get formatted balance
     */
    public String getFormattedBalance() {
        return currency + " " + balance.toString();
    }
}