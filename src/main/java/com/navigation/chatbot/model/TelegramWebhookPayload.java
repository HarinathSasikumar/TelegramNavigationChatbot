package com.navigation.chatbot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramWebhookPayload {

    @JsonProperty("update_id")
    private Long updateId;

    @JsonProperty("message")
    private Message message;

    @JsonProperty("callback_query")
    private CallbackQuery callbackQuery;

    public Long getUpdateId() { return updateId; }
    public void setUpdateId(Long updateId) { this.updateId = updateId; }
    public Message getMessage() { return message; }
    public void setMessage(Message message) { this.message = message; }
    public CallbackQuery getCallbackQuery() { return callbackQuery; }
    public void setCallbackQuery(CallbackQuery callbackQuery) { this.callbackQuery = callbackQuery; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        @JsonProperty("message_id")
        private Long messageId;
        @JsonProperty("from")
        private User from;
        @JsonProperty("chat")
        private Chat chat;
        @JsonProperty("text")
        private String text;
        @JsonProperty("location")
        private Location location;

        public Long getMessageId() { return messageId; }
        public void setMessageId(Long messageId) { this.messageId = messageId; }
        public User getFrom() { return from; }
        public void setFrom(User from) { this.from = from; }
        public Chat getChat() { return chat; }
        public void setChat(Chat chat) { this.chat = chat; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public Location getLocation() { return location; }
        public void setLocation(Location location) { this.location = location; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CallbackQuery {
        @JsonProperty("id")
        private String id;
        @JsonProperty("from")
        private User from;
        @JsonProperty("message")
        private Message message;
        @JsonProperty("data")
        private String data;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public User getFrom() { return from; }
        public void setFrom(User from) { this.from = from; }
        public Message getMessage() { return message; }
        public void setMessage(Message message) { this.message = message; }
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        @JsonProperty("id")
        private Long id;
        @JsonProperty("first_name")
        private String firstName;
        @JsonProperty("username")
        private String username;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Chat {
        @JsonProperty("id")
        private Long id;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        @JsonProperty("latitude")
        private Double latitude;
        @JsonProperty("longitude")
        private Double longitude;

        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
    }
}
