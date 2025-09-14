package com.example.brailleapp.security;

/**
 * Enumeration of penetration test types
 */
public enum PenetrationTestType {
    SQL_INJECTION("SQL Injection", "Test for SQL injection vulnerabilities"),
    XSS("Cross-Site Scripting", "Test for XSS vulnerabilities"),
    COMMAND_INJECTION("Command Injection", "Test for command injection vulnerabilities"),
    FILE_UPLOAD("File Upload", "Test file upload security"),
    AUTHENTICATION("Authentication", "Test authentication security"),
    COMPREHENSIVE("Comprehensive", "Perform all security tests");
    
    private final String displayName;
    private final String description;
    
    PenetrationTestType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
