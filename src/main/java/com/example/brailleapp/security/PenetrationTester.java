package com.example.brailleapp.security;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Penetration Testing Module for security vulnerability assessment
 * Provides automated security testing capabilities
 */
public class PenetrationTester {
    private static final Logger logger = Logger.getLogger(PenetrationTester.class.getName());
    
    // Common SQL injection payloads
    private static final String[] SQL_INJECTION_PAYLOADS = {
        "' OR '1'='1",
        "' OR 1=1--",
        "'; DROP TABLE users; --",
        "' UNION SELECT * FROM users--",
        "' OR 'x'='x",
        "admin'--",
        "admin' OR '1'='1'--",
        "' OR 1=1#",
        "' OR '1'='1' /*",
        "1' OR '1'='1' AND '1'='1"
    };
    
    // Common XSS payloads
    private static final String[] XSS_PAYLOADS = {
        "<script>alert('XSS')</script>",
        "<img src=x onerror=alert('XSS')>",
        "<svg onload=alert('XSS')>",
        "javascript:alert('XSS')",
        "<iframe src=javascript:alert('XSS')></iframe>",
        "<body onload=alert('XSS')>",
        "<input onfocus=alert('XSS') autofocus>",
        "<select onfocus=alert('XSS') autofocus>",
        "<textarea onfocus=alert('XSS') autofocus>",
        "<keygen onfocus=alert('XSS') autofocus>"
    };
    
    // Common command injection payloads
    private static final String[] COMMAND_INJECTION_PAYLOADS = {
        "; ls -la",
        "| whoami",
        "& dir",
        "` id `",
        "$(whoami)",
        "; cat /etc/passwd",
        "| type C:\\Windows\\System32\\drivers\\etc\\hosts",
        "& net user",
        "; ps aux",
        "| tasklist"
    };
    
    /**
     * Perform comprehensive penetration test
     * @param target The target to test (file, URL, or input field)
     * @param testType Type of penetration test to perform
     * @return PenetrationTestResult containing test results
     */
    public PenetrationTestResult performPenetrationTest(String target, PenetrationTestType testType) {
        PenetrationTestResult result = new PenetrationTestResult();
        result.setTarget(target);
        result.setTestType(testType);
        result.setTestTimestamp(new Date());
        
        try {
            switch (testType) {
                case SQL_INJECTION:
                    testSqlInjection(target, result);
                    break;
                case XSS:
                    testXss(target, result);
                    break;
                case COMMAND_INJECTION:
                    testCommandInjection(target, result);
                    break;
                case FILE_UPLOAD:
                    testFileUpload(target, result);
                    break;
                case AUTHENTICATION:
                    testAuthentication(target, result);
                    break;
                case COMPREHENSIVE:
                    performComprehensiveTest(target, result);
                    break;
            }
            
            // Determine overall test result
            determineTestResult(result);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during penetration test: " + e.getMessage(), e);
            result.addVulnerability("TEST_ERROR", "Penetration test failed: " + e.getMessage(), VulnerabilityLevel.HIGH);
        }
        
        return result;
    }
    
    /**
     * Test for SQL injection vulnerabilities
     */
    private void testSqlInjection(String target, PenetrationTestResult result) {
        logger.info("Testing SQL injection vulnerabilities...");
        
        for (String payload : SQL_INJECTION_PAYLOADS) {
            try {
                // Simulate SQL injection test
                String testInput = target + payload;
                
                // Check for common SQL error patterns
                if (containsSqlErrorPatterns(testInput)) {
                    result.addVulnerability("SQL_INJECTION", 
                        "SQL injection vulnerability detected with payload: " + payload, 
                        VulnerabilityLevel.CRITICAL);
                }
                
                // Check for successful injection patterns
                if (containsSqlSuccessPatterns(testInput)) {
                    result.addVulnerability("SQL_INJECTION_SUCCESS", 
                        "SQL injection successful with payload: " + payload, 
                        VulnerabilityLevel.CRITICAL);
                }
                
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error testing SQL injection payload: " + payload, e);
            }
        }
    }
    
    /**
     * Test for XSS vulnerabilities
     */
    private void testXss(String target, PenetrationTestResult result) {
        logger.info("Testing XSS vulnerabilities...");
        
        for (String payload : XSS_PAYLOADS) {
            try {
                // Simulate XSS test
                String testInput = target + payload;
                
                // Check for XSS patterns
                if (containsXssPatterns(testInput)) {
                    result.addVulnerability("XSS", 
                        "XSS vulnerability detected with payload: " + payload, 
                        VulnerabilityLevel.HIGH);
                }
                
                // Check for script execution
                if (containsScriptExecution(testInput)) {
                    result.addVulnerability("XSS_EXECUTION", 
                        "Script execution detected with payload: " + payload, 
                        VulnerabilityLevel.CRITICAL);
                }
                
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error testing XSS payload: " + payload, e);
            }
        }
    }
    
    /**
     * Test for command injection vulnerabilities
     */
    private void testCommandInjection(String target, PenetrationTestResult result) {
        logger.info("Testing command injection vulnerabilities...");
        
        for (String payload : COMMAND_INJECTION_PAYLOADS) {
            try {
                // Simulate command injection test
                String testInput = target + payload;
                
                // Check for command injection patterns
                if (containsCommandInjectionPatterns(testInput)) {
                    result.addVulnerability("COMMAND_INJECTION", 
                        "Command injection vulnerability detected with payload: " + payload, 
                        VulnerabilityLevel.CRITICAL);
                }
                
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error testing command injection payload: " + payload, e);
            }
        }
    }
    
    /**
     * Test file upload security
     */
    private void testFileUpload(String target, PenetrationTestResult result) {
        logger.info("Testing file upload security...");
        
        // Test for malicious file uploads
        String[] maliciousFiles = {
            "malware.exe", "script.js", "shell.php", "backdoor.bat"
        };
        
        for (String fileName : maliciousFiles) {
            if (target.contains(fileName)) {
                result.addVulnerability("MALICIOUS_FILE_UPLOAD", 
                    "Malicious file upload detected: " + fileName, 
                    VulnerabilityLevel.CRITICAL);
            }
        }
        
        // Test for path traversal
        String[] pathTraversalPayloads = {
            "../../../etc/passwd",
            "..\\..\\..\\windows\\system32\\config\\sam",
            "....//....//....//etc/passwd"
        };
        
        for (String payload : pathTraversalPayloads) {
            if (target.contains(payload)) {
                result.addVulnerability("PATH_TRAVERSAL", 
                    "Path traversal vulnerability detected: " + payload, 
                    VulnerabilityLevel.HIGH);
            }
        }
    }
    
    /**
     * Test authentication security
     */
    private void testAuthentication(String target, PenetrationTestResult result) {
        logger.info("Testing authentication security...");
        
        // Test for weak passwords
        String[] weakPasswords = {
            "password", "123456", "admin", "root", "test", "guest"
        };
        
        for (String weakPassword : weakPasswords) {
            if (target.contains(weakPassword)) {
                result.addVulnerability("WEAK_PASSWORD", 
                    "Weak password detected: " + weakPassword, 
                    VulnerabilityLevel.MEDIUM);
            }
        }
        
        // Test for default credentials
        String[] defaultCredentials = {
            "admin:admin", "root:root", "user:user", "guest:guest"
        };
        
        for (String credentials : defaultCredentials) {
            if (target.contains(credentials)) {
                result.addVulnerability("DEFAULT_CREDENTIALS", 
                    "Default credentials detected: " + credentials, 
                    VulnerabilityLevel.HIGH);
            }
        }
    }
    
    /**
     * Perform comprehensive security test
     */
    private void performComprehensiveTest(String target, PenetrationTestResult result) {
        logger.info("Performing comprehensive security test...");
        
        // Run all individual tests
        testSqlInjection(target, result);
        testXss(target, result);
        testCommandInjection(target, result);
        testFileUpload(target, result);
        testAuthentication(target, result);
        
        // Additional comprehensive tests
        testInformationDisclosure(target, result);
        testSessionManagement(target, result);
        testInputValidation(target, result);
    }
    
    /**
     * Test for information disclosure
     */
    private void testInformationDisclosure(String target, PenetrationTestResult result) {
        // Check for sensitive information in target
        String[] sensitivePatterns = {
            "password", "secret", "key", "token", "api_key", "private"
        };
        
        for (String pattern : sensitivePatterns) {
            if (target.toLowerCase().contains(pattern)) {
                result.addVulnerability("INFORMATION_DISCLOSURE", 
                    "Sensitive information disclosed: " + pattern, 
                    VulnerabilityLevel.MEDIUM);
            }
        }
    }
    
    /**
     * Test session management
     */
    private void testSessionManagement(String target, PenetrationTestResult result) {
        // Check for session-related vulnerabilities
        if (target.contains("sessionid") || target.contains("jsessionid")) {
            result.addVulnerability("SESSION_EXPOSURE", 
                "Session information exposed in target", 
                VulnerabilityLevel.MEDIUM);
        }
    }
    
    /**
     * Test input validation
     */
    private void testInputValidation(String target, PenetrationTestResult result) {
        // Check for input validation bypasses
        if (target.length() > 1000) {
            result.addVulnerability("INPUT_VALIDATION_BYPASS", 
                "Large input may bypass validation", 
                VulnerabilityLevel.LOW);
        }
        
        // Check for special characters
        if (target.contains("<>\"'&")) {
            result.addVulnerability("SPECIAL_CHARACTERS", 
                "Special characters may cause validation issues", 
                VulnerabilityLevel.LOW);
        }
    }
    
    /**
     * Check for SQL error patterns
     */
    private boolean containsSqlErrorPatterns(String input) {
        String[] errorPatterns = {
            "SQL syntax", "mysql_fetch", "ORA-", "Microsoft OLE DB",
            "ODBC SQL Server Driver", "PostgreSQL query failed"
        };
        
        for (String pattern : errorPatterns) {
            if (input.toLowerCase().contains(pattern.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check for SQL success patterns
     */
    private boolean containsSqlSuccessPatterns(String input) {
        String[] successPatterns = {
            "union select", "information_schema", "mysql.user", "pg_user"
        };
        
        for (String pattern : successPatterns) {
            if (input.toLowerCase().contains(pattern.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check for XSS patterns
     */
    private boolean containsXssPatterns(String input) {
        String[] xssPatterns = {
            "<script", "javascript:", "onload=", "onerror=", "onclick="
        };
        
        for (String pattern : xssPatterns) {
            if (input.toLowerCase().contains(pattern.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check for script execution
     */
    private boolean containsScriptExecution(String input) {
        return input.contains("alert(") || input.contains("document.cookie") || 
               input.contains("window.location");
    }
    
    /**
     * Check for command injection patterns
     */
    private boolean containsCommandInjectionPatterns(String input) {
        String[] commandPatterns = {
            ";", "|", "&", "`", "$(", "&&", "||"
        };
        
        for (String pattern : commandPatterns) {
            if (input.contains(pattern)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Determine overall test result
     */
    private void determineTestResult(PenetrationTestResult result) {
        int criticalCount = 0;
        int highCount = 0;
        int mediumCount = 0;
        int lowCount = 0;
        
        for (SecurityVulnerability vuln : result.getVulnerabilities()) {
            switch (vuln.getLevel()) {
                case CRITICAL: criticalCount++; break;
                case HIGH: highCount++; break;
                case MEDIUM: mediumCount++; break;
                case LOW: lowCount++; break;
            }
        }
        
        if (criticalCount > 0) {
            result.setTestStatus(TestStatus.CRITICAL_VULNERABILITIES);
        } else if (highCount > 0) {
            result.setTestStatus(TestStatus.HIGH_RISK);
        } else if (mediumCount > 0) {
            result.setTestStatus(TestStatus.MEDIUM_RISK);
        } else if (lowCount > 0) {
            result.setTestStatus(TestStatus.LOW_RISK);
        } else {
            result.setTestStatus(TestStatus.SECURE);
        }
    }
}
