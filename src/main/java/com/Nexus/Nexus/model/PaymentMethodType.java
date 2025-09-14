package com.Nexus.Nexus.model;

/**
 * Enum representing different payment methods
 */
public enum PaymentMethodType {
    STRIPE("Stripe"),
    PAYPAL("PayPal"),
    BANK_TRANSFER("Bank Transfer"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card");
    
    private final String displayName;
    
    PaymentMethodType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}