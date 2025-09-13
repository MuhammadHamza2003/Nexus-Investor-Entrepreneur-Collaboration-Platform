package com.Nexus.Nexus.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * DTO for withdraw request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawRequest {
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum withdraw amount is $1.00")
    private BigDecimal amount;
    
    @NotBlank(message = "Payment method ID is required")
    private String paymentMethodId;
    
    private String description;
}