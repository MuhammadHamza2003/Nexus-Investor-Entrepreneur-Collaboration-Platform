package com.Nexus.Nexus.dto;

import com.Nexus.Nexus.model.TransactionStatus;
import com.Nexus.Nexus.model.TransactionType;
import com.Nexus.Nexus.model.PaymentMethodType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for transaction response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    
    private String id;
    private TransactionType type;
    private BigDecimal amount;
    private TransactionStatus status;
    private String description;
    private String recipientUsername; // for transfers
    private PaymentMethodType paymentMethod;
    private String currency;
    private BigDecimal fee;
    private BigDecimal netAmount;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}