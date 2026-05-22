package com.navigation.chatbot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppWebhookPayload {

    @JsonProperty("object") private String object;
    @JsonProperty("entry")  private List<Entry> entry;

    public String getObject()     { return object; }
    public List<Entry> getEntry() { return entry; }
    public void setObject(String object)       { this.object = object; }
    public void setEntry(List<Entry> entry)    { this.entry = entry; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry {
        @JsonProperty("id")      private String id;
        @JsonProperty("changes") private List<Change> changes;

        public String getId()              { return id; }
        public List<Change> getChanges()   { return changes; }
        public void setId(String id)       { this.id = id; }
        public void setChanges(List<Change> changes) { this.changes = changes; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Change {
        @JsonProperty("value") private Value value;
        @JsonProperty("field") private String field;

        public Value getValue()         { return value; }
        public String getField()        { return field; }
        public void setValue(Value value)   { this.value = value; }
        public void setField(String field)  { this.field = field; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        @JsonProperty("messaging_product") private String messagingProduct;
        @JsonProperty("metadata")          private Metadata metadata;
        @JsonProperty("contacts")          private List<Contact> contacts;
        @JsonProperty("messages")          private List<Message> messages;
        @JsonProperty("statuses")          private List<Status> statuses;

        public String getMessagingProduct()      { return messagingProduct; }
        public Metadata getMetadata()            { return metadata; }
        public List<Contact> getContacts()       { return contacts; }
        public List<Message> getMessages()       { return messages; }
        public List<Status> getStatuses()        { return statuses; }
        public void setMessagingProduct(String v) { messagingProduct = v; }
        public void setMetadata(Metadata v)      { metadata = v; }
        public void setContacts(List<Contact> v) { contacts = v; }
        public void setMessages(List<Message> v) { messages = v; }
        public void setStatuses(List<Status> v)  { statuses = v; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        @JsonProperty("display_phone_number") private String displayPhoneNumber;
        @JsonProperty("phone_number_id")      private String phoneNumberId;

        public String getDisplayPhoneNumber() { return displayPhoneNumber; }
        public String getPhoneNumberId()      { return phoneNumberId; }
        public void setDisplayPhoneNumber(String v) { displayPhoneNumber = v; }
        public void setPhoneNumberId(String v)      { phoneNumberId = v; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Contact {
        @JsonProperty("profile") private Profile profile;
        @JsonProperty("wa_id")   private String waId;

        public Profile getProfile()    { return profile; }
        public String getWaId()        { return waId; }
        public void setProfile(Profile profile) { this.profile = profile; }
        public void setWaId(String waId)        { this.waId = waId; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile {
        @JsonProperty("name") private String name;

        public String getName()         { return name; }
        public void setName(String name) { this.name = name; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        @JsonProperty("from")        private String from;
        @JsonProperty("id")          private String id;
        @JsonProperty("timestamp")   private String timestamp;
        @JsonProperty("type")        private String type;
        @JsonProperty("text")        private TextContent text;
        @JsonProperty("interactive") private Interactive interactive;
        @JsonProperty("location")    private Location location;

        public String getFrom()              { return from; }
        public String getId()                { return id; }
        public String getTimestamp()         { return timestamp; }
        public String getType()              { return type; }
        public TextContent getText()         { return text; }
        public Interactive getInteractive()  { return interactive; }
        public Location getLocation()        { return location; }
        public void setFrom(String v)        { from = v; }
        public void setId(String v)          { id = v; }
        public void setTimestamp(String v)   { timestamp = v; }
        public void setType(String v)        { type = v; }
        public void setText(TextContent v)   { text = v; }
        public void setInteractive(Interactive v) { interactive = v; }
        public void setLocation(Location v)  { location = v; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextContent {
        @JsonProperty("body") private String body;

        public String getBody()         { return body; }
        public void setBody(String body) { this.body = body; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Interactive {
        @JsonProperty("type")         private String type;
        @JsonProperty("button_reply") private ButtonReply buttonReply;
        @JsonProperty("list_reply")   private ListReply listReply;

        public String getType()               { return type; }
        public ButtonReply getButtonReply()   { return buttonReply; }
        public ListReply getListReply()       { return listReply; }
        public void setType(String v)         { type = v; }
        public void setButtonReply(ButtonReply v) { buttonReply = v; }
        public void setListReply(ListReply v)  { listReply = v; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ButtonReply {
        @JsonProperty("id")    private String id;
        @JsonProperty("title") private String title;

        public String getId()           { return id; }
        public String getTitle()        { return title; }
        public void setId(String v)     { id = v; }
        public void setTitle(String v)  { title = v; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ListReply {
        @JsonProperty("id")          private String id;
        @JsonProperty("title")       private String title;
        @JsonProperty("description") private String description;

        public String getId()              { return id; }
        public String getTitle()           { return title; }
        public String getDescription()     { return description; }
        public void setId(String v)        { id = v; }
        public void setTitle(String v)     { title = v; }
        public void setDescription(String v) { description = v; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        @JsonProperty("latitude")  private Double latitude;
        @JsonProperty("longitude") private Double longitude;
        @JsonProperty("name")      private String name;
        @JsonProperty("address")   private String address;

        public Double getLatitude()      { return latitude; }
        public Double getLongitude()     { return longitude; }
        public String getName()          { return name; }
        public String getAddress()       { return address; }
        public void setLatitude(Double v)  { latitude = v; }
        public void setLongitude(Double v) { longitude = v; }
        public void setName(String v)      { name = v; }
        public void setAddress(String v)   { address = v; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        @JsonProperty("id")           private String id;
        @JsonProperty("status")       private String status;
        @JsonProperty("timestamp")    private String timestamp;
        @JsonProperty("recipient_id") private String recipientId;

        public String getId()             { return id; }
        public String getStatus()         { return status; }
        public String getTimestamp()      { return timestamp; }
        public String getRecipientId()    { return recipientId; }
        public void setId(String v)        { id = v; }
        public void setStatus(String v)    { status = v; }
        public void setTimestamp(String v) { timestamp = v; }
        public void setRecipientId(String v) { recipientId = v; }
    }
}
