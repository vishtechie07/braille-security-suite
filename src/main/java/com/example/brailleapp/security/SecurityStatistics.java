package com.example.brailleapp.security;

import java.util.Date;

/**
 * Security statistics for monitoring and reporting
 */
public class SecurityStatistics {
    private int totalEvents;
    private int totalThreats;
    private int totalVulnerabilities;
    private int criticalThreats;
    private int highThreats;
    private int mediumThreats;
    private int lowThreats;
    private Date lastUpdated;
    
    public SecurityStatistics() {
        this.totalEvents = 0;
        this.totalThreats = 0;
        this.totalVulnerabilities = 0;
        this.criticalThreats = 0;
        this.highThreats = 0;
        this.mediumThreats = 0;
        this.lowThreats = 0;
        this.lastUpdated = new Date();
    }
    
    /**
     * Get overall security score (0-100)
     */
    public int getSecurityScore() {
        int totalIssues = criticalThreats + highThreats + mediumThreats + lowThreats;
        if (totalIssues == 0) {
            return 100; // Perfect score
        }
        
        // Calculate score based on severity
        int penalty = (criticalThreats * 25) + (highThreats * 15) + (mediumThreats * 10) + (lowThreats * 5);
        int score = Math.max(0, 100 - penalty);
        
        return score;
    }
    
    /**
     * Get security status based on statistics
     */
    public String getSecurityStatus() {
        if (criticalThreats > 0) {
            return "CRITICAL";
        } else if (highThreats > 0) {
            return "HIGH_RISK";
        } else if (mediumThreats > 0) {
            return "MEDIUM_RISK";
        } else if (lowThreats > 0) {
            return "LOW_RISK";
        } else {
            return "SECURE";
        }
    }
    
    /**
     * Get summary of statistics
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Security Statistics Summary\n");
        summary.append("==========================\n");
        summary.append("Total Events: ").append(totalEvents).append("\n");
        summary.append("Total Threats: ").append(totalThreats).append("\n");
        summary.append("Total Vulnerabilities: ").append(totalVulnerabilities).append("\n");
        summary.append("Critical: ").append(criticalThreats).append("\n");
        summary.append("High: ").append(highThreats).append("\n");
        summary.append("Medium: ").append(mediumThreats).append("\n");
        summary.append("Low: ").append(lowThreats).append("\n");
        summary.append("Security Score: ").append(getSecurityScore()).append("/100\n");
        summary.append("Status: ").append(getSecurityStatus()).append("\n");
        summary.append("Last Updated: ").append(lastUpdated).append("\n");
        
        return summary.toString();
    }
    
    // Getters and Setters
    public int getTotalEvents() {
        return totalEvents;
    }
    
    public void setTotalEvents(int totalEvents) {
        this.totalEvents = totalEvents;
    }
    
    public int getTotalThreats() {
        return totalThreats;
    }
    
    public void setTotalThreats(int totalThreats) {
        this.totalThreats = totalThreats;
    }
    
    public int getTotalVulnerabilities() {
        return totalVulnerabilities;
    }
    
    public void setTotalVulnerabilities(int totalVulnerabilities) {
        this.totalVulnerabilities = totalVulnerabilities;
    }
    
    public int getCriticalThreats() {
        return criticalThreats;
    }
    
    public void setCriticalThreats(int criticalThreats) {
        this.criticalThreats = criticalThreats;
    }
    
    public int getHighThreats() {
        return highThreats;
    }
    
    public void setHighThreats(int highThreats) {
        this.highThreats = highThreats;
    }
    
    public int getMediumThreats() {
        return mediumThreats;
    }
    
    public void setMediumThreats(int mediumThreats) {
        this.mediumThreats = mediumThreats;
    }
    
    public int getLowThreats() {
        return lowThreats;
    }
    
    public void setLowThreats(int lowThreats) {
        this.lowThreats = lowThreats;
    }
    
    public Date getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
