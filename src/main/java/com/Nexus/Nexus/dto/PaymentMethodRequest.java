package com.Nexus.Nexus.dto;

import com.Nexus.Nexus.model.PaymentMethodType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for adding a new payment method
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodRequest {
    
    @NotNull(message = "Payment method type is required")
    private PaymentMethodType type;
    
    @NotBlank(message = "Payment token is required")
    private String paymentToken; // Token from Stripe/PayPal frontend integration
    
    private String displayName;
    private boolean setAsDefault;
}