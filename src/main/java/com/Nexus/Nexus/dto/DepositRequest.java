package com.Nexus.Nexus.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * DTO for deposit request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositRequest {
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum deposit amount is $1.00")
    private BigDecimal amount;
    
    private String paymentMethodId; // Optional - for saved payment methods
    
    private String paymentToken; // Optional - for one-time payments
    
    private String description;
}