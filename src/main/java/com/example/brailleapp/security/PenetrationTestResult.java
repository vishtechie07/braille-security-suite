package com.example.brailleapp.security;

import java.util.*;

/**
 * Result of penetration testing containing discovered vulnerabilities
 */
public class PenetrationTestResult {
    private String target;
    private PenetrationTestType testType;
    private Date testTimestamp;
    private TestStatus testStatus;
    private List<SecurityVulnerability> vulnerabilities;
    private Map<String, Object> metadata;
    
    public PenetrationTestResult() {
        this.vulnerabilities = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.testStatus = TestStatus.UNKNOWN;
    }
    
    /**
     * Add a security vulnerability to the result
     */
    public void addVulnerability(String type, String description, VulnerabilityLevel level) {
        SecurityVulnerability vuln = new SecurityVulnerability(type, description, level, new Date());
        vulnerabilities.add(vuln);
    }
    
    /**
     * Add metadata to the result
     */
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    /**
     * Get vulnerability count by level
     */
    public int getVulnerabilityCount(VulnerabilityLevel level) {
        return (int) vulnerabilities.stream()
            .filter(vuln -> vuln.getLevel() == level)
            .count();
    }
    
    /**
     * Get summary of test results
     */
    public String getTestSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Penetration Test Results\n");
        summary.append("=======================\n");
        summary.append("Target: ").append(target).append("\n");
        summary.append("Test Type: ").append(testType).append("\n");
        summary.append("Test Status: ").append(testStatus).append("\n");
        summary.append("Total Vulnerabilities: ").append(vulnerabilities.size()).append("\n");
        summary.append("Critical: ").append(getVulnerabilityCount(VulnerabilityLevel.CRITICAL)).append("\n");
        summary.append("High: ").append(getVulnerabilityCount(VulnerabilityLevel.HIGH)).append("\n");
        summary.append("Medium: ").append(getVulnerabilityCount(VulnerabilityLevel.MEDIUM)).append("\n");
        summary.append("Low: ").append(getVulnerabilityCount(VulnerabilityLevel.LOW)).append("\n");
        
        if (!vulnerabilities.isEmpty()) {
            summary.append("\nVulnerabilities Found:\n");
            for (SecurityVulnerability vuln : vulnerabilities) {
                summary.append("- ").append(vuln.getType()).append(": ").append(vuln.getDescription()).append("\n");
            }
        }
        
        return summary.toString();
    }
    
    // Getters and Setters
    public String getTarget() {
        return target;
    }
    
    public void setTarget(String target) {
        this.target = target;
    }
    
    public PenetrationTestType getTestType() {
        return testType;
    }
    
    public void setTestType(PenetrationTestType testType) {
        this.testType = testType;
    }
    
    public Date getTestTimestamp() {
        return testTimestamp;
    }
    
    public void setTestTimestamp(Date testTimestamp) {
        this.testTimestamp = testTimestamp;
    }
    
    public TestStatus getTestStatus() {
        return testStatus;
    }
    
    public void setTestStatus(TestStatus testStatus) {
        this.testStatus = testStatus;
    }
    
    public List<SecurityVulnerability> getVulnerabilities() {
        return vulnerabilities;
    }
    
    public void setVulnerabilities(List<SecurityVulnerability> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
