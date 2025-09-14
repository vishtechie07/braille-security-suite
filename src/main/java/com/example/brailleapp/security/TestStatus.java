package com.example.brailleapp.security;

/**
 * Enumeration of penetration test status levels
 */
public enum TestStatus {
    SECURE("Secure", "No vulnerabilities found"),
    LOW_RISK("Low Risk", "Low severity vulnerabilities found"),
    MEDIUM_RISK("Medium Risk", "Medium severity vulnerabilities found"),
    HIGH_RISK("High Risk", "High severity vulnerabilities found"),
    CRITICAL_VULNERABILITIES("Critical", "Critical vulnerabilities found"),
    UNKNOWN("Unknown", "Test status not determined");
    
    private final String displayName;
    private final String description;
    
    TestStatus(String displayName, String description) {
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
            case SECURE: return "#00FF00"; // Green
            case LOW_RISK: return "#FFA500"; // Orange
            case MEDIUM_RISK: return "#FF8C00"; // Dark Orange
            case HIGH_RISK: return "#FF4500"; // Red Orange
            case CRITICAL_VULNERABILITIES: return "#FF0000"; // Red
            case UNKNOWN: return "#808080"; // Gray
            default: return "#808080"; // Gray
        }
    }
    
    /**
     * Check if system is secure
     */
    public boolean isSecure() {
        return this == SECURE || this == LOW_RISK;
    }
    
    /**
     * Check if immediate action is required
     */
    public boolean requiresImmediateAction() {
        return this == CRITICAL_VULNERABILITIES || this == HIGH_RISK;
    }
}
