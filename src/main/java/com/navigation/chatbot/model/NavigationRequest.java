package com.navigation.chatbot.model;

import java.util.Map;

public class NavigationRequest {
    private String id;
    private String phoneNumber;
    private String userName;
    private String destination;
    private Double startLatitude;
    private Double startLongitude;
    private String status;
    private String routeType;
    private String estimatedTime;
    private String distance;
    private String createdAt;
    private String completedAt;
    private Map<String, Object> additionalInfo;

    public NavigationRequest() {}

    public NavigationRequest(String id, String phoneNumber, String userName, String destination,
                             Double startLatitude, Double startLongitude, String status,
                             String routeType, String estimatedTime, String distance,
                             String createdAt, String completedAt, Map<String, Object> additionalInfo) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.userName = userName;
        this.destination = destination;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.status = status;
        this.routeType = routeType;
        this.estimatedTime = estimatedTime;
        this.distance = distance;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.additionalInfo = additionalInfo;
    }

    // Getters
    public String getId()                        { return id; }
    public String getPhoneNumber()               { return phoneNumber; }
    public String getUserName()                  { return userName; }
    public String getDestination()               { return destination; }
    public Double getStartLatitude()             { return startLatitude; }
    public Double getStartLongitude()            { return startLongitude; }
    public String getStatus()                    { return status; }
    public String getRouteType()                 { return routeType; }
    public String getEstimatedTime()             { return estimatedTime; }
    public String getDistance()                  { return distance; }
    public String getCreatedAt()                 { return createdAt; }
    public String getCompletedAt()               { return completedAt; }
    public Map<String, Object> getAdditionalInfo() { return additionalInfo; }

    // Setters
    public void setId(String id)                        { this.id = id; }
    public void setPhoneNumber(String phoneNumber)       { this.phoneNumber = phoneNumber; }
    public void setUserName(String userName)             { this.userName = userName; }
    public void setDestination(String destination)       { this.destination = destination; }
    public void setStartLatitude(Double startLatitude)   { this.startLatitude = startLatitude; }
    public void setStartLongitude(Double startLongitude) { this.startLongitude = startLongitude; }
    public void setStatus(String status)                 { this.status = status; }
    public void setRouteType(String routeType)           { this.routeType = routeType; }
    public void setEstimatedTime(String estimatedTime)   { this.estimatedTime = estimatedTime; }
    public void setDistance(String distance)             { this.distance = distance; }
    public void setCreatedAt(String createdAt)           { this.createdAt = createdAt; }
    public void setCompletedAt(String completedAt)       { this.completedAt = completedAt; }
    public void setAdditionalInfo(Map<String, Object> additionalInfo) { this.additionalInfo = additionalInfo; }

    // --- Builder ---
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id, phoneNumber, userName, destination, status, routeType;
        private String estimatedTime, distance, createdAt, completedAt;
        private Double startLatitude, startLongitude;
        private Map<String, Object> additionalInfo;

        public Builder id(String v)                 { id = v; return this; }
        public Builder phoneNumber(String v)        { phoneNumber = v; return this; }
        public Builder userName(String v)           { userName = v; return this; }
        public Builder destination(String v)        { destination = v; return this; }
        public Builder startLatitude(Double v)      { startLatitude = v; return this; }
        public Builder startLongitude(Double v)     { startLongitude = v; return this; }
        public Builder status(String v)             { status = v; return this; }
        public Builder routeType(String v)          { routeType = v; return this; }
        public Builder estimatedTime(String v)      { estimatedTime = v; return this; }
        public Builder distance(String v)           { distance = v; return this; }
        public Builder createdAt(String v)          { createdAt = v; return this; }
        public Builder completedAt(String v)        { completedAt = v; return this; }
        public Builder additionalInfo(Map<String, Object> v) { additionalInfo = v; return this; }

        public NavigationRequest build() {
            return new NavigationRequest(id, phoneNumber, userName, destination,
                    startLatitude, startLongitude, status, routeType,
                    estimatedTime, distance, createdAt, completedAt, additionalInfo);
        }
    }
}
