package com.example.brailleapp.security;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a security event for audit logging
 */
public class SecurityEvent {
    private String eventType;
    private String description;
    private String level;
    private Date timestamp;
    private String userId;
    private String sessionId;
    private String ipAddress;
    private String userAgent;
    private Map<String, Object> metadata;
    
    public SecurityEvent(String eventType, String description, String level) {
        this.eventType = eventType;
        this.description = description;
        this.level = level;
        this.timestamp = new Date();
        this.metadata = new java.util.HashMap<>();
    }
    
    public SecurityEvent(String eventType, String description, String level, String userId) {
        this(eventType, description, level);
        this.userId = userId;
    }
    
    /**
     * Add metadata to the event
     */
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    // Getters and Setters
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getLevel() {
        return level;
    }
    
    public void setLevel(String level) {
        this.level = level;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", level, eventType, description);
    }
}
