package com.Nexus.Nexus.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity representing a financial transaction in the system
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "transactions")
public class Transaction {
    
    @Id
    private String id;
    
    @NotNull(message = "User is required")
    @DBRef
    private User user;
    
    @NotNull(message = "Transaction type is required")
    private TransactionType type;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotNull(message = "Status is required")
    private TransactionStatus status;
    
    private String description;
    
    // For transfers - the recipient user
    @DBRef
    private User recipientUser;
    
    // Payment gateway reference ID (from Stripe, PayPal, etc.)
    private String externalTransactionId;
    
    // Payment method used
    private PaymentMethodType paymentMethod;
    
    // Additional metadata (can store payment gateway specific data)
    private Map<String, Object> metadata;
    
    // Error message if transaction failed
    private String errorMessage;
    
    // Transaction fees (if any)
    private BigDecimal fee;
    
    // Net amount (amount - fee)
    private BigDecimal netAmount;
    
    // Currency code (default USD)
    @Builder.Default
    private String currency = "USD";
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    
    /**
     * Get the display name for this transaction
     */
    public String getDisplayName() {
        return type.getDisplayName() + " - $" + amount;
    }
    
    /**
     * Check if this transaction is completed
     */
    public boolean isCompleted() {
        return status == TransactionStatus.COMPLETED;
    }
    
    /**
     * Check if this transaction is pending
     */
    public boolean isPending() {
        return status == TransactionStatus.PENDING || status == TransactionStatus.PROCESSING;
    }
    
    /**
     * Check if this transaction failed
     */
    public boolean isFailed() {
        return status == TransactionStatus.FAILED;
    }
}