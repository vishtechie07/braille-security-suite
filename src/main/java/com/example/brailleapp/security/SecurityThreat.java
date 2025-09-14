package com.example.brailleapp.security;

import java.util.Date;

/**
 * Represents a security threat detected during scanning
 */
public class SecurityThreat {
    private String type;
    private String description;
    private ThreatLevel level;
    private Date detectedAt;
    private String recommendation;
    
    public SecurityThreat(String type, String description, ThreatLevel level, Date detectedAt) {
        this.type = type;
        this.description = description;
        this.level = level;
        this.detectedAt = detectedAt;
        this.recommendation = generateRecommendation(type, level);
    }
    
    /**
     * Generate security recommendation based on threat type and level
     */
    private String generateRecommendation(String type, ThreatLevel level) {
        switch (type) {
            case "SQL_INJECTION":
                return "Sanitize input data and use parameterized queries";
            case "XSS_VULNERABILITY":
                return "Escape HTML/JavaScript content and validate user input";
            case "MALICIOUS_CODE":
                return "Block file processing and investigate source";
            case "EXECUTABLE_DETECTED":
                return "Reject file - executables are not allowed";
            case "EMBEDDED_EXECUTABLE":
                return "Extract and scan embedded files separately";
            case "SUSPICIOUS_EXTENSION":
                return "Verify file type and scan for malware";
            case "FILE_TOO_LARGE":
                return "Compress file or split into smaller chunks";
            case "UNSUPPORTED_FORMAT":
                return "Convert to supported format or reject";
            case "PDF_JAVASCRIPT":
                return "Disable JavaScript execution in PDF viewer";
            case "PDF_EMBEDDED_FILE":
                return "Extract and scan embedded files";
            case "SUSPICIOUS_URL":
                return "Verify URL safety before accessing";
            case "EMBEDDED_SCRIPT":
                return "Review script content for malicious code";
            default:
                return "Review file content and apply appropriate security measures";
        }
    }
    
    // Getters and Setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public ThreatLevel getLevel() {
        return level;
    }
    
    public void setLevel(ThreatLevel level) {
        this.level = level;
    }
    
    public Date getDetectedAt() {
        return detectedAt;
    }
    
    public void setDetectedAt(Date detectedAt) {
        this.detectedAt = detectedAt;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
    
    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", level, type, description);
    }
}
