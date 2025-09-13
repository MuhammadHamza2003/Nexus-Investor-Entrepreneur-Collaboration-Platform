package com.Nexus.Nexus.model;

/**
 * Enumeration for document status
 */
public enum DocumentStatus {
    DRAFT("Draft"),
    REVIEWED("Reviewed"),
    SIGNED("Signed"),
    ARCHIVED("Archived"),
    DELETED("Deleted");
    
    private final String displayName;
    
    DocumentStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
