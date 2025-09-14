package com.example.brailleapp.security;

/**
 * Enumeration of overall security status levels
 */
public enum SecurityStatus {
    SAFE("Safe", "No security threats detected"),
    LOW_RISK("Low Risk", "Minor security concerns detected"),
    MEDIUM_RISK("Medium Risk", "Moderate security risks detected"),
    HIGH_RISK("High Risk", "Significant security risks detected"),
    CRITICAL("Critical", "Immediate security threats detected"),
    UNKNOWN("Unknown", "Security status not determined");
    
    private final String displayName;
    private final String description;
    
    SecurityStatus(String displayName, String description) {
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
            case SAFE: return "#00FF00"; // Green
            case LOW_RISK: return "#FFA500"; // Orange
            case MEDIUM_RISK: return "#FF8C00"; // Dark Orange
            case HIGH_RISK: return "#FF4500"; // Red Orange
            case CRITICAL: return "#FF0000"; // Red
            case UNKNOWN: return "#808080"; // Gray
            default: return "#808080"; // Gray
        }
    }
    
    /**
     * Check if file is safe to process
     */
    public boolean isProcessable() {
        return this == SAFE || this == LOW_RISK;
    }
    
    /**
     * Check if file should be blocked
     */
    public boolean shouldBlock() {
        return this == CRITICAL || this == HIGH_RISK;
    }
}
