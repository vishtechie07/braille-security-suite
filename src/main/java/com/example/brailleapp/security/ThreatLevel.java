package com.example.brailleapp.security;

/**
 * Enumeration of threat levels for security scanning
 */
public enum ThreatLevel {
    LOW("Low", "Minor security concern"),
    MEDIUM("Medium", "Moderate security risk"),
    HIGH("High", "Significant security risk"),
    CRITICAL("Critical", "Immediate security threat");
    
    private final String displayName;
    private final String description;
    
    ThreatLevel(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get color code for UI display
     */
    public String getColorCode() {
        switch (this) {
            case LOW: return "#FFA500"; // Orange
            case MEDIUM: return "#FF8C00"; // Dark Orange
            case HIGH: return "#FF4500"; // Red Orange
            case CRITICAL: return "#FF0000"; // Red
            default: return "#808080"; // Gray
        }
    }
    
    /**
     * Get priority score for sorting
     */
    public int getPriority() {
        switch (this) {
            case CRITICAL: return 4;
            case HIGH: return 3;
            case MEDIUM: return 2;
            case LOW: return 1;
            default: return 0;
        }
    }
}
