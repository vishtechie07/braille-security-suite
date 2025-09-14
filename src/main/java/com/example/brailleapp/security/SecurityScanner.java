package com.example.brailleapp.security;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Comprehensive Security Scanner for file uploads and content analysis
 * Provides vulnerability detection, malware scanning, and security validation
 */
public class SecurityScanner {
    private static final Logger logger = Logger.getLogger(SecurityScanner.class.getName());
    
    // Security patterns for detection
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|vbscript|onload|onerror|onclick)"
    );
    
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "(?i)(<script|</script|javascript:|vbscript:|onload=|onerror=|onclick=|<iframe|</iframe|alert\\s*\\(|document\\.cookie)"
    );
    
    private static final Pattern MALICIOUS_PATTERN = Pattern.compile(
        "(?i)(eval\\s*\\(|system\\s*\\(|exec\\s*\\(|shell_exec|passthru|file_get_contents|fopen|fwrite|base64_decode|gzinflate|str_rot13)"
    );
    
    private static final Pattern SUSPICIOUS_FILE_PATTERN = Pattern.compile(
        "(?i)\\.(exe|bat|cmd|com|scr|pif|vbs|js|jar|war|sh|ps1|php|asp|jsp)$"
    );
    
    // Known malicious file signatures (first few bytes)
    private static final Map<String, String> MALICIOUS_SIGNATURES = new HashMap<>();
    static {
        MALICIOUS_SIGNATURES.put("4D5A", "PE Executable"); // Windows PE
        MALICIOUS_SIGNATURES.put("7F454C46", "ELF Executable"); // Linux ELF
        MALICIOUS_SIGNATURES.put("CAFEBABE", "Java Class File");
        MALICIOUS_SIGNATURES.put("504B0304", "ZIP/Office Document");
    }
    
    /**
     * Comprehensive security scan of uploaded file
     * @param file The file to scan
     * @return SecurityScanResult containing scan results
     */
    public SecurityScanResult scanFile(File file) {
        SecurityScanResult result = new SecurityScanResult();
        result.setFileName(file.getName());
        result.setFileSize(file.length());
        result.setScanTimestamp(new Date());
        
        try {
            // 1. File type validation
            validateFileType(file, result);
            
            // 2. File signature validation
            validateFileSignature(file, result);
            
            // 3. Content analysis
            analyzeFileContent(file, result);
            
            // 4. Malware pattern detection
            detectMalwarePatterns(file, result);
            
            // 5. Calculate file hash for tracking
            result.setFileHash(calculateFileHash(file));
            
            // 6. Determine overall security status
            determineSecurityStatus(result);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during security scan: " + e.getMessage(), e);
            result.addThreat("SCAN_ERROR", "Security scan failed: " + e.getMessage(), ThreatLevel.HIGH);
        }
        
        return result;
    }
    
    /**
     * Validate file type and extension
     */
    private void validateFileType(File file, SecurityScanResult result) {
        String fileName = file.getName().toLowerCase();
        String extension = getFileExtension(fileName);
        
        // Check for suspicious file extensions
        if (SUSPICIOUS_FILE_PATTERN.matcher(fileName).find()) {
            result.addThreat("SUSPICIOUS_EXTENSION", 
                "File has potentially dangerous extension: " + extension, 
                ThreatLevel.HIGH);
        }
        
        // Check file size limits
        long maxSize = 50 * 1024 * 1024; // 50MB limit
        if (file.length() > maxSize) {
            result.addThreat("FILE_TOO_LARGE", 
                "File size exceeds maximum allowed size: " + (file.length() / 1024 / 1024) + "MB", 
                ThreatLevel.MEDIUM);
        }
        
        // Validate allowed extensions
        List<String> allowedExtensions = Arrays.asList("txt", "pdf", "docx", "png", "jpg", "jpeg", "gif", "bmp", "tiff");
        if (!allowedExtensions.contains(extension)) {
            result.addThreat("UNSUPPORTED_FORMAT", 
                "File format not supported: " + extension, 
                ThreatLevel.MEDIUM);
        }
    }
    
    /**
     * Validate file signature (magic bytes)
     */
    private void validateFileSignature(File file, SecurityScanResult result) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] header = new byte[8];
            int bytesRead = fis.read(header);
            
            if (bytesRead >= 4) {
                String hexSignature = bytesToHex(header, 4);
                
                // Check against known malicious signatures
                for (Map.Entry<String, String> entry : MALICIOUS_SIGNATURES.entrySet()) {
                    if (hexSignature.startsWith(entry.getKey())) {
                        if (entry.getValue().contains("Executable")) {
                            result.addThreat("EXECUTABLE_DETECTED", 
                                "File appears to be an executable: " + entry.getValue(), 
                                ThreatLevel.CRITICAL);
                        } else {
                            result.addThreat("SUSPICIOUS_SIGNATURE", 
                                "File has suspicious signature: " + entry.getValue(), 
                                ThreatLevel.MEDIUM);
                        }
                    }
                }
            }
        } catch (IOException e) {
            result.addThreat("SIGNATURE_SCAN_ERROR", 
                "Could not read file signature: " + e.getMessage(), 
                ThreatLevel.LOW);
        }
    }
    
    /**
     * Analyze file content for security threats
     */
    private void analyzeFileContent(File file, SecurityScanResult result) {
        try {
            String content = readFileContent(file);
            
            // Check for SQL injection patterns
            if (SQL_INJECTION_PATTERN.matcher(content).find()) {
                result.addThreat("SQL_INJECTION", 
                    "Potential SQL injection pattern detected in content", 
                    ThreatLevel.HIGH);
            }
            
            // Check for XSS patterns
            if (XSS_PATTERN.matcher(content).find()) {
                result.addThreat("XSS_VULNERABILITY", 
                    "Potential XSS vulnerability detected in content", 
                    ThreatLevel.HIGH);
            }
            
            // Check for malicious code patterns
            if (MALICIOUS_PATTERN.matcher(content).find()) {
                result.addThreat("MALICIOUS_CODE", 
                    "Potential malicious code pattern detected", 
                    ThreatLevel.CRITICAL);
            }
            
            // Check for embedded scripts
            if (content.contains("<script") || content.contains("javascript:")) {
                result.addThreat("EMBEDDED_SCRIPT", 
                    "Embedded script detected in content", 
                    ThreatLevel.MEDIUM);
            }
            
            // Check for suspicious URLs
            Pattern urlPattern = Pattern.compile("https?://[^\\s]+");
            java.util.regex.Matcher urlMatcher = urlPattern.matcher(content);
            while (urlMatcher.find()) {
                String url = urlMatcher.group();
                if (isSuspiciousUrl(url)) {
                    result.addThreat("SUSPICIOUS_URL", 
                        "Suspicious URL detected: " + url, 
                        ThreatLevel.MEDIUM);
                }
            }
            
        } catch (Exception e) {
            result.addThreat("CONTENT_ANALYSIS_ERROR", 
                "Could not analyze file content: " + e.getMessage(), 
                ThreatLevel.LOW);
        }
    }
    
    /**
     * Detect malware patterns in file
     */
    private void detectMalwarePatterns(File file, SecurityScanResult result) {
        try {
            // Check for embedded executables in documents
            if (file.getName().toLowerCase().endsWith(".docx")) {
                checkForEmbeddedExecutables(file, result);
            }
            
            // Check for suspicious file structure
            if (file.getName().toLowerCase().endsWith(".pdf")) {
                checkPdfSecurity(file, result);
            }
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in malware pattern detection: " + e.getMessage());
        }
    }
    
    /**
     * Check for embedded executables in DOCX files
     */
    private void checkForEmbeddedExecutables(File file, SecurityScanResult result) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName().toLowerCase();
                
                // Check for embedded executables
                if (entryName.endsWith(".exe") || entryName.endsWith(".bat") || 
                    entryName.endsWith(".cmd") || entryName.endsWith(".vbs")) {
                    result.addThreat("EMBEDDED_EXECUTABLE", 
                        "Embedded executable detected: " + entryName, 
                        ThreatLevel.CRITICAL);
                }
                
                // Check for suspicious embedded files
                if (entryName.contains("script") || entryName.contains("macro")) {
                    result.addThreat("EMBEDDED_SCRIPT", 
                        "Embedded script/macro detected: " + entryName, 
                        ThreatLevel.MEDIUM);
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error checking for embedded executables: " + e.getMessage());
        }
    }
    
    /**
     * Check PDF security features
     */
    private void checkPdfSecurity(File file, SecurityScanResult result) {
        try {
            String content = readFileContent(file);
            
            // Check for JavaScript in PDF
            if (content.contains("/JavaScript") || content.contains("/JS")) {
                result.addThreat("PDF_JAVASCRIPT", 
                    "PDF contains JavaScript which may be malicious", 
                    ThreatLevel.MEDIUM);
            }
            
            // Check for embedded files
            if (content.contains("/EmbeddedFile") || content.contains("/FileAttachment")) {
                result.addThreat("PDF_EMBEDDED_FILE", 
                    "PDF contains embedded files which may be malicious", 
                    ThreatLevel.MEDIUM);
            }
            
            // Check for form actions
            if (content.contains("/SubmitForm") || content.contains("/ResetForm")) {
                result.addThreat("PDF_FORM_ACTION", 
                    "PDF contains form actions which may be used for data exfiltration", 
                    ThreatLevel.LOW);
            }
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error checking PDF security: " + e.getMessage());
        }
    }
    
    /**
     * Read file content safely
     */
    private String readFileContent(File file) throws IOException {
        // Limit content reading to first 1MB to prevent memory issues
        long maxContentSize = 1024 * 1024; // 1MB
        long fileSize = file.length();
        long readSize = Math.min(fileSize, maxContentSize);
        
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] content = new byte[(int) readSize];
            fis.read(content);
            return new String(content, "UTF-8");
        }
    }
    
    /**
     * Calculate file hash for tracking
     */
    private String calculateFileHash(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }
            return bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            logger.log(Level.WARNING, "Error calculating file hash: " + e.getMessage());
            return "HASH_ERROR";
        }
    }
    
    /**
     * Check if URL is suspicious
     */
    private boolean isSuspiciousUrl(String url) {
        String lowerUrl = url.toLowerCase();
        
        // Check for suspicious domains
        String[] suspiciousDomains = {
            "bit.ly", "tinyurl.com", "goo.gl", "t.co", "shortened",
            "malware", "virus", "phishing", "suspicious"
        };
        
        for (String domain : suspiciousDomains) {
            if (lowerUrl.contains(domain)) {
                return true;
            }
        }
        
        // Check for suspicious patterns
        return lowerUrl.contains("javascript:") || 
               lowerUrl.contains("data:") || 
               lowerUrl.contains("vbscript:");
    }
    
    /**
     * Determine overall security status
     */
    private void determineSecurityStatus(SecurityScanResult result) {
        int criticalCount = 0;
        int highCount = 0;
        int mediumCount = 0;
        int lowCount = 0;
        
        for (SecurityThreat threat : result.getThreats()) {
            switch (threat.getLevel()) {
                case CRITICAL: criticalCount++; break;
                case HIGH: highCount++; break;
                case MEDIUM: mediumCount++; break;
                case LOW: lowCount++; break;
            }
        }
        
        if (criticalCount > 0) {
            result.setSecurityStatus(SecurityStatus.CRITICAL);
        } else if (highCount > 0) {
            result.setSecurityStatus(SecurityStatus.HIGH_RISK);
        } else if (mediumCount > 0) {
            result.setSecurityStatus(SecurityStatus.MEDIUM_RISK);
        } else if (lowCount > 0) {
            result.setSecurityStatus(SecurityStatus.LOW_RISK);
        } else {
            result.setSecurityStatus(SecurityStatus.SAFE);
        }
    }
    
    /**
     * Get file extension
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }
    
    /**
     * Convert bytes to hex string
     */
    private String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, bytes.length);
    }
    
    private String bytesToHex(byte[] bytes, int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(String.format("%02X", bytes[i]));
        }
        return result.toString();
    }
}
