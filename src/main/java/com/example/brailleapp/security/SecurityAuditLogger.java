package com.example.brailleapp.security;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Security Audit Logger for tracking security events and activities
 * Provides comprehensive logging and monitoring capabilities
 */
public class SecurityAuditLogger {
    private static final Logger logger = Logger.getLogger(SecurityAuditLogger.class.getName());
    private static final String AUDIT_LOG_DIR = "security_logs";
    private static final String AUDIT_LOG_FILE = "security_audit.log";
    private static final String THREAT_LOG_FILE = "threat_detection.log";
    private static final String VULNERABILITY_LOG_FILE = "vulnerability_scan.log";
    
    private final Path auditLogPath;
    private final Path threatLogPath;
    private final Path vulnerabilityLogPath;
    
    public SecurityAuditLogger() {
        try {
            // Create security logs directory
            Files.createDirectories(Paths.get(AUDIT_LOG_DIR));
            
            this.auditLogPath = Paths.get(AUDIT_LOG_DIR, AUDIT_LOG_FILE);
            this.threatLogPath = Paths.get(AUDIT_LOG_DIR, THREAT_LOG_FILE);
            this.vulnerabilityLogPath = Paths.get(AUDIT_LOG_DIR, VULNERABILITY_LOG_FILE);
            
            // Initialize log files with headers
            initializeLogFiles();
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to initialize security audit logger: " + e.getMessage(), e);
            throw new RuntimeException("Security audit logger initialization failed", e);
        }
    }
    
    /**
     * Log security event
     */
    public void logSecurityEvent(SecurityEvent event) {
        try {
            String logEntry = formatSecurityEvent(event);
            writeToLog(auditLogPath, logEntry);
            
            // Also log to console for immediate visibility
            logger.info("SECURITY EVENT: " + event.getEventType() + " - " + event.getDescription());
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to log security event: " + e.getMessage(), e);
        }
    }
    
    /**
     * Log threat detection
     */
    public void logThreatDetection(SecurityThreat threat, String context) {
        try {
            String logEntry = formatThreatDetection(threat, context);
            writeToLog(threatLogPath, logEntry);
            
            // Log critical threats to console
            if (threat.getLevel() == ThreatLevel.CRITICAL || threat.getLevel() == ThreatLevel.HIGH) {
                logger.warning("THREAT DETECTED: " + threat.getType() + " - " + threat.getDescription());
            }
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to log threat detection: " + e.getMessage(), e);
        }
    }
    
    /**
     * Log vulnerability scan result
     */
    public void logVulnerabilityScan(PenetrationTestResult result) {
        try {
            String logEntry = formatVulnerabilityScan(result);
            writeToLog(vulnerabilityLogPath, logEntry);
            
            // Log critical vulnerabilities to console
            if (result.getTestStatus() == TestStatus.CRITICAL_VULNERABILITIES || 
                result.getTestStatus() == TestStatus.HIGH_RISK) {
                logger.warning("VULNERABILITY FOUND: " + result.getTestType() + " - " + result.getTestStatus());
            }
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to log vulnerability scan: " + e.getMessage(), e);
        }
    }
    
    /**
     * Log file upload security scan
     */
    public void logFileUploadScan(SecurityScanResult result) {
        try {
            String logEntry = formatFileUploadScan(result);
            writeToLog(auditLogPath, logEntry);
            
            // Log blocked files
            if (result.getSecurityStatus() == SecurityStatus.CRITICAL || 
                result.getSecurityStatus() == SecurityStatus.HIGH_RISK) {
                logger.warning("FILE BLOCKED: " + result.getFileName() + " - " + result.getSecurityStatus());
            }
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to log file upload scan: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get security statistics
     */
    public SecurityStatistics getSecurityStatistics() {
        SecurityStatistics stats = new SecurityStatistics();
        
        try {
            // Count events from audit log
            stats.setTotalEvents(countLogEntries(auditLogPath));
            
            // Count threats from threat log
            stats.setTotalThreats(countLogEntries(threatLogPath));
            
            // Count vulnerabilities from vulnerability log
            stats.setTotalVulnerabilities(countLogEntries(vulnerabilityLogPath));
            
            // Count by severity levels
            stats.setCriticalThreats(countThreatsByLevel(ThreatLevel.CRITICAL));
            stats.setHighThreats(countThreatsByLevel(ThreatLevel.HIGH));
            stats.setMediumThreats(countThreatsByLevel(ThreatLevel.MEDIUM));
            stats.setLowThreats(countThreatsByLevel(ThreatLevel.LOW));
            
            stats.setLastUpdated(new Date());
            
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to calculate security statistics: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Generate security report
     */
    public String generateSecurityReport() {
        StringBuilder report = new StringBuilder();
        SecurityStatistics stats = getSecurityStatistics();
        
        report.append("SECURITY AUDIT REPORT\n");
        report.append("====================\n");
        report.append("Generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n\n");
        
        report.append("OVERALL STATISTICS\n");
        report.append("------------------\n");
        report.append("Total Security Events: ").append(stats.getTotalEvents()).append("\n");
        report.append("Total Threats Detected: ").append(stats.getTotalThreats()).append("\n");
        report.append("Total Vulnerabilities Found: ").append(stats.getTotalVulnerabilities()).append("\n\n");
        
        report.append("THREAT BREAKDOWN\n");
        report.append("----------------\n");
        report.append("Critical: ").append(stats.getCriticalThreats()).append("\n");
        report.append("High: ").append(stats.getHighThreats()).append("\n");
        report.append("Medium: ").append(stats.getMediumThreats()).append("\n");
        report.append("Low: ").append(stats.getLowThreats()).append("\n\n");
        
        report.append("SECURITY STATUS\n");
        report.append("---------------\n");
        if (stats.getCriticalThreats() > 0) {
            report.append("Status: CRITICAL - Immediate action required\n");
        } else if (stats.getHighThreats() > 0) {
            report.append("Status: HIGH RISK - Urgent attention needed\n");
        } else if (stats.getMediumThreats() > 0) {
            report.append("Status: MEDIUM RISK - Monitor closely\n");
        } else if (stats.getLowThreats() > 0) {
            report.append("Status: LOW RISK - Minor concerns\n");
        } else {
            report.append("Status: SECURE - No threats detected\n");
        }
        
        return report.toString();
    }
    
    /**
     * Initialize log files with headers
     */
    private void initializeLogFiles() throws IOException {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        
        // Initialize audit log
        if (!Files.exists(auditLogPath)) {
            String auditHeader = "# Security Audit Log\n" +
                               "# Started: " + timestamp + "\n" +
                               "# Format: [TIMESTAMP] [LEVEL] [EVENT_TYPE] [DESCRIPTION]\n\n";
            Files.write(auditLogPath, auditHeader.getBytes());
        }
        
        // Initialize threat log
        if (!Files.exists(threatLogPath)) {
            String threatHeader = "# Threat Detection Log\n" +
                                "# Started: " + timestamp + "\n" +
                                "# Format: [TIMESTAMP] [THREAT_LEVEL] [THREAT_TYPE] [DESCRIPTION]\n\n";
            Files.write(threatLogPath, threatHeader.getBytes());
        }
        
        // Initialize vulnerability log
        if (!Files.exists(vulnerabilityLogPath)) {
            String vulnHeader = "# Vulnerability Scan Log\n" +
                              "# Started: " + timestamp + "\n" +
                              "# Format: [TIMESTAMP] [TEST_TYPE] [STATUS] [VULNERABILITIES]\n\n";
            Files.write(vulnerabilityLogPath, vulnHeader.getBytes());
        }
    }
    
    /**
     * Format security event for logging
     */
    private String formatSecurityEvent(SecurityEvent event) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] [%s] [%s] %s\n",
            sdf.format(event.getTimestamp()),
            event.getLevel(),
            event.getEventType(),
            event.getDescription()
        );
    }
    
    /**
     * Format threat detection for logging
     */
    private String formatThreatDetection(SecurityThreat threat, String context) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] [%s] [%s] %s (Context: %s)\n",
            sdf.format(threat.getDetectedAt()),
            threat.getLevel(),
            threat.getType(),
            threat.getDescription(),
            context
        );
    }
    
    /**
     * Format vulnerability scan for logging
     */
    private String formatVulnerabilityScan(PenetrationTestResult result) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] [%s] [%s] Vulnerabilities: %d (Critical: %d, High: %d, Medium: %d, Low: %d)\n",
            sdf.format(result.getTestTimestamp()),
            result.getTestType(),
            result.getTestStatus(),
            result.getVulnerabilities().size(),
            result.getVulnerabilityCount(VulnerabilityLevel.CRITICAL),
            result.getVulnerabilityCount(VulnerabilityLevel.HIGH),
            result.getVulnerabilityCount(VulnerabilityLevel.MEDIUM),
            result.getVulnerabilityCount(VulnerabilityLevel.LOW)
        );
    }
    
    /**
     * Format file upload scan for logging
     */
    private String formatFileUploadScan(SecurityScanResult result) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] [FILE_UPLOAD] [%s] %s (Size: %d bytes, Hash: %s)\n",
            sdf.format(result.getScanTimestamp()),
            result.getSecurityStatus(),
            result.getFileName(),
            result.getFileSize(),
            result.getFileHash()
        );
    }
    
    /**
     * Write entry to log file
     */
    private void writeToLog(Path logPath, String entry) throws IOException {
        Files.write(logPath, entry.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }
    
    /**
     * Count log entries
     */
    private int countLogEntries(Path logPath) throws IOException {
        if (!Files.exists(logPath)) {
            return 0;
        }
        
        return (int) Files.lines(logPath)
            .filter(line -> !line.startsWith("#") && !line.trim().isEmpty())
            .count();
    }
    
    /**
     * Count threats by level
     */
    private int countThreatsByLevel(ThreatLevel level) throws IOException {
        if (!Files.exists(threatLogPath)) {
            return 0;
        }
        
        return (int) Files.lines(threatLogPath)
            .filter(line -> line.contains("[" + level + "]"))
            .count();
    }
}
