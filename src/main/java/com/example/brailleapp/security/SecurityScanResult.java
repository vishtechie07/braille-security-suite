package com.example.brailleapp.security;

import java.util.*;

/**
 * Security scan result containing all detected threats and security status
 */
public class SecurityScanResult {
    private String fileName;
    private long fileSize;
    private Date scanTimestamp;
    private String fileHash;
    private SecurityStatus securityStatus;
    private List<SecurityThreat> threats;
    private Map<String, Object> metadata;
    
    public SecurityScanResult() {
        this.threats = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.securityStatus = SecurityStatus.UNKNOWN;
    }
    
    /**
     * Add a security threat to the result
     */
    public void addThreat(String type, String description, ThreatLevel level) {
        SecurityThreat threat = new SecurityThreat(type, description, level, new Date());
        threats.add(threat);
    }
    
    /**
     * Add metadata to the result
     */
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    /**
     * Get threat count by level
     */
    public int getThreatCount(ThreatLevel level) {
        return (int) threats.stream()
            .filter(threat -> threat.getLevel() == level)
            .count();
    }
    
    /**
     * Check if file is safe to process
     */
    public boolean isSafe() {
        return securityStatus == SecurityStatus.SAFE || securityStatus == SecurityStatus.LOW_RISK;
    }
    
    /**
     * Get summary of security status
     */
    public String getSecuritySummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Security Status: ").append(securityStatus).append("\n");
        summary.append("Total Threats: ").append(threats.size()).append("\n");
        summary.append("Critical: ").append(getThreatCount(ThreatLevel.CRITICAL)).append("\n");
        summary.append("High: ").append(getThreatCount(ThreatLevel.HIGH)).append("\n");
        summary.append("Medium: ").append(getThreatCount(ThreatLevel.MEDIUM)).append("\n");
        summary.append("Low: ").append(getThreatCount(ThreatLevel.LOW)).append("\n");
        
        if (!threats.isEmpty()) {
            summary.append("\nThreats Detected:\n");
            for (SecurityThreat threat : threats) {
                summary.append("- ").append(threat.getType()).append(": ").append(threat.getDescription()).append("\n");
            }
        }
        
        return summary.toString();
    }
    
    // Getters and Setters
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public Date getScanTimestamp() {
        return scanTimestamp;
    }
    
    public void setScanTimestamp(Date scanTimestamp) {
        this.scanTimestamp = scanTimestamp;
    }
    
    public String getFileHash() {
        return fileHash;
    }
    
    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }
    
    public SecurityStatus getSecurityStatus() {
        return securityStatus;
    }
    
    public void setSecurityStatus(SecurityStatus securityStatus) {
        this.securityStatus = securityStatus;
    }
    
    public List<SecurityThreat> getThreats() {
        return threats;
    }
    
    public void setThreats(List<SecurityThreat> threats) {
        this.threats = threats;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
