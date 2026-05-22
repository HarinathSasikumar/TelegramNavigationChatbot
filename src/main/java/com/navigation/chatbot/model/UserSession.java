package com.navigation.chatbot.model;

import java.time.Instant;
import java.util.Map;

public class UserSession {
    private String phoneNumber;
    private String name;
    private String currentState;
    private String destination;
    private Map<String, Object> contextData;
    private String createdAt;
    private String updatedAt;
    private int messageCount;

    public UserSession() {}

    public UserSession(String phoneNumber, String name, String currentState, String destination,
                       Map<String, Object> contextData, String createdAt, String updatedAt, int messageCount) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.currentState = currentState;
        this.destination = destination;
        this.contextData = contextData;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.messageCount = messageCount;
    }

    // Getters
    public String getPhoneNumber()            { return phoneNumber; }
    public String getName()                   { return name; }
    public String getCurrentState()           { return currentState; }
    public String getDestination()            { return destination; }
    public Map<String, Object> getContextData() { return contextData; }
    public String getCreatedAt()              { return createdAt; }
    public String getUpdatedAt()              { return updatedAt; }
    public int getMessageCount()              { return messageCount; }

    // Setters
    public void setPhoneNumber(String phoneNumber)       { this.phoneNumber = phoneNumber; }
    public void setName(String name)                     { this.name = name; }
    public void setCurrentState(String currentState)     { this.currentState = currentState; }
    public void setDestination(String destination)       { this.destination = destination; }
    public void setContextData(Map<String, Object> ctx)  { this.contextData = ctx; }
    public void setCreatedAt(String createdAt)           { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt)           { this.updatedAt = updatedAt; }
    public void setMessageCount(int messageCount)        { this.messageCount = messageCount; }

    public static class State {
        public static final String WELCOME               = "WELCOME";
        public static final String AWAITING_DESTINATION  = "AWAITING_DESTINATION";
        public static final String SHOWING_OPTIONS       = "SHOWING_OPTIONS";
        public static final String NAVIGATION_ACTIVE     = "NAVIGATION_ACTIVE";
        public static final String AWAITING_LOCATION     = "AWAITING_LOCATION";
        public static final String HELP                  = "HELP";
    }

    public static UserSession newSession(String phoneNumber, String name) {
        String now = Instant.now().toString();
        UserSession s = new UserSession();
        s.phoneNumber = phoneNumber;
        s.name = name;
        s.currentState = State.WELCOME;
        s.messageCount = 0;
        s.createdAt = now;
        s.updatedAt = now;
        return s;
    }

    // --- Builder ---
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String phoneNumber, name, currentState, destination, createdAt, updatedAt;
        private Map<String, Object> contextData;
        private int messageCount;

        public Builder phoneNumber(String v)           { phoneNumber = v; return this; }
        public Builder name(String v)                  { name = v; return this; }
        public Builder currentState(String v)          { currentState = v; return this; }
        public Builder destination(String v)           { destination = v; return this; }
        public Builder contextData(Map<String, Object> v) { contextData = v; return this; }
        public Builder createdAt(String v)             { createdAt = v; return this; }
        public Builder updatedAt(String v)             { updatedAt = v; return this; }
        public Builder messageCount(int v)             { messageCount = v; return this; }

        public UserSession build() {
            return new UserSession(phoneNumber, name, currentState, destination,
                    contextData, createdAt, updatedAt, messageCount);
        }
    }
}
