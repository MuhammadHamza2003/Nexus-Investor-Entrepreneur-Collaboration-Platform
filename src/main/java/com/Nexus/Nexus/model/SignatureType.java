package com.Nexus.Nexus.model;

/**
 * Types of signatures supported
 */
public enum SignatureType {
    ELECTRONIC("Electronic"),
    DIGITAL("Digital"),
    HANDWRITTEN("Handwritten");
    
    private final String displayName;
    
    SignatureType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
