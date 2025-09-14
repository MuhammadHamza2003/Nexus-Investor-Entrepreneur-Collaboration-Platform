package com.Nexus.Nexus.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Size;
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
    @DecimalMax(value = "50000.00", message = "Maximum deposit amount is $50,000.00")
    private BigDecimal amount;
    
    @Size(max = 255, message = "Payment method ID must not exceed 255 characters")
    private String paymentMethodId; // Optional - for saved payment methods
    
    @Size(max = 255, message = "Payment token must not exceed 255 characters")
    private String paymentToken; // Optional - for one-time payments
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}