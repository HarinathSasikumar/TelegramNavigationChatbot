package com.navigation.chatbot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstagramWebhookPayload {

    @JsonProperty("object")
    private String object;

    @JsonProperty("entry")
    private List<Entry> entry;

    public String getObject() { return object; }
    public List<Entry> getEntry() { return entry; }
    public void setObject(String object) { this.object = object; }
    public void setEntry(List<Entry> entry) { this.entry = entry; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry {
        @JsonProperty("id")
        private String id;
        @JsonProperty("messaging")
        private List<Messaging> messaging;

        public String getId() { return id; }
        public List<Messaging> getMessaging() { return messaging; }
        public void setId(String id) { this.id = id; }
        public void setMessaging(List<Messaging> messaging) { this.messaging = messaging; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Messaging {
        @JsonProperty("sender")
        private Sender sender;
        @JsonProperty("recipient")
        private Recipient recipient;
        @JsonProperty("message")
        private Message message;

        public Sender getSender() { return sender; }
        public Recipient getRecipient() { return recipient; }
        public Message getMessage() { return message; }
        public void setSender(Sender sender) { this.sender = sender; }
        public void setRecipient(Recipient recipient) { this.recipient = recipient; }
        public void setMessage(Message message) { this.message = message; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sender {
        @JsonProperty("id")
        private String id;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Recipient {
        @JsonProperty("id")
        private String id;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        @JsonProperty("mid")
        private String mid;
        @JsonProperty("text")
        private String text;
        @JsonProperty("quick_reply")
        private QuickReply quickReply;

        public String getMid() { return mid; }
        public String getText() { return text; }
        public QuickReply getQuickReply() { return quickReply; }
        public void setMid(String mid) { this.mid = mid; }
        public void setText(String text) { this.text = text; }
        public void setQuickReply(QuickReply quickReply) { this.quickReply = quickReply; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuickReply {
        @JsonProperty("payload")
        private String payload;
        public String getPayload() { return payload; }
        public void setPayload(String payload) { this.payload = payload; }
    }
}
