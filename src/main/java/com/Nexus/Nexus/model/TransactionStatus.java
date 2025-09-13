package com.Nexus.Nexus.model;

/**
 * Enum representing the different statuses a transaction can have
 */
public enum TransactionStatus {
    PENDING("Pending"),
    COMPLETED("Completed"), 
    FAILED("Failed"),
    CANCELLED("Cancelled"),
    PROCESSING("Processing");
    
    private final String displayName;
    
    TransactionStatus(String displayName) {
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