package com.navigation.chatbot.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navigation.chatbot.model.InstagramWebhookPayload;
import com.navigation.chatbot.model.TelegramWebhookPayload;
import com.navigation.chatbot.model.WhatsAppWebhookPayload;
import com.navigation.chatbot.service.ChatbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final ChatbotService chatbotService;
    private final ObjectMapper objectMapper;

    @Value("${meta.whatsapp.verify-token}")
    private String verifyToken;

    public WebhookController(ChatbotService chatbotService, ObjectMapper objectMapper) {
        this.chatbotService = chatbotService;
        this.objectMapper = objectMapper;
    }

    /** GET /webhook — Meta webhook verification */
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode")          String mode,
            @RequestParam("hub.verify_token")  String token,
            @RequestParam("hub.challenge")     String challenge) {

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("Webhook verified successfully.");
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Verification failed");
    }

    /** POST /webhook — Receive incoming messages */
    @PostMapping
    public ResponseEntity<String> receiveWebhook(@RequestBody String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            String objectType = root.has("object") ? root.get("object").asText() : "";

            if ("whatsapp_business_account".equals(objectType)) {
                WhatsAppWebhookPayload payload = objectMapper.treeToValue(root, WhatsAppWebhookPayload.class);
                if (payload.getEntry() != null) {
                    for (var entry : payload.getEntry()) {
                        if (entry.getChanges() == null) continue;
                        for (var change : entry.getChanges()) {
                            if (!"messages".equals(change.getField())) continue;
                            var value = change.getValue();
                            if (value == null || value.getMessages() == null) continue;
                            List<WhatsAppWebhookPayload.Contact> contacts = value.getContacts();
                            for (var message : value.getMessages()) {
                                var contact = (contacts != null && !contacts.isEmpty()) ? contacts.get(0) : null;
                                String senderName = contact != null && contact.getProfile() != null ? contact.getProfile().getName() : "User";
                                String text = message.getText() != null ? message.getText().getBody() : null;
                                String interactiveId = null;
                                if (message.getInteractive() != null) {
                                    if ("button_reply".equals(message.getInteractive().getType()) && message.getInteractive().getButtonReply() != null)
                                        interactiveId = message.getInteractive().getButtonReply().getId();
                                    else if ("list_reply".equals(message.getInteractive().getType()) && message.getInteractive().getListReply() != null)
                                        interactiveId = message.getInteractive().getListReply().getId();
                                }
                                Double lat = message.getLocation() != null ? message.getLocation().getLatitude() : null;
                                Double lng = message.getLocation() != null ? message.getLocation().getLongitude() : null;
                                
                                chatbotService.processMessage("whatsapp", message.getFrom(), senderName,
                                        message.getType(), text, interactiveId, lat, lng);
                            }
                        }
                    }
                }
            } else if ("instagram".equals(objectType) || "page".equals(objectType)) {
                InstagramWebhookPayload payload = objectMapper.treeToValue(root, InstagramWebhookPayload.class);
                if (payload.getEntry() != null) {
                    for (var entry : payload.getEntry()) {
                        if (entry.getMessaging() == null) continue;
                        for (var messaging : entry.getMessaging()) {
                            if (messaging.getMessage() == null) continue;
                            var msg = messaging.getMessage();
                            String senderId = messaging.getSender().getId();
                            String text = msg.getText();
                            String interactiveId = msg.getQuickReply() != null ? msg.getQuickReply().getPayload() : null;
                            String type = interactiveId != null ? "interactive" : (text != null ? "text" : "unknown");
                            
                            // For location sharing on IG via maps URL (simplified)
                            Double lat = null; Double lng = null;
                            if (text != null && text.contains("google.com/maps")) {
                                type = "location";
                                lat = 0.0; lng = 0.0;
                            }
                            
                            chatbotService.processMessage("instagram", senderId, "IG User",
                                    type, text, interactiveId, lat, lng);
                        }
                    }
                }
            } else if (root.has("update_id")) {
                TelegramWebhookPayload payload = objectMapper.treeToValue(root, TelegramWebhookPayload.class);
                
                if (payload.getMessage() != null) {
                    var msg = payload.getMessage();
                    String senderId = String.valueOf(msg.getFrom().getId());
                    String senderName = msg.getFrom().getFirstName();
                    String text = msg.getText();
                    Double lat = msg.getLocation() != null ? msg.getLocation().getLatitude() : null;
                    Double lng = msg.getLocation() != null ? msg.getLocation().getLongitude() : null;
                    String type = (lat != null && lng != null) ? "location" : (text != null ? "text" : "unknown");
                    
                    chatbotService.processMessage("telegram", senderId, senderName, type, text, null, lat, lng);
                } else if (payload.getCallbackQuery() != null) {
                    var cb = payload.getCallbackQuery();
                    String senderId = String.valueOf(cb.getFrom().getId());
                    String senderName = cb.getFrom().getFirstName();
                    String interactiveId = cb.getData();
                    
                    chatbotService.processMessage("telegram", senderId, senderName, "interactive", null, interactiveId, null, null);
                }
            }
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
        }
        return ResponseEntity.ok("EVENT_RECEIVED");
    }
}
