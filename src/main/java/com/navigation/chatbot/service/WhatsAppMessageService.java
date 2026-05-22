package com.navigation.chatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class WhatsAppMessageService implements MessagingPlatform {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppMessageService.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${meta.whatsapp.phone-number-id}") private String phoneNumberId;
    @Value("${meta.whatsapp.api-version}")      private String apiVersion;
    @Value("${meta.whatsapp.base-url}")         private String baseUrl;

    public WhatsAppMessageService(OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendTextMessage(String recipientPhone, String text) {
        Map<String, Object> payload = buildBasePayload(recipientPhone);
        payload.put("type", "text");
        payload.put("text", Map.of("body", text, "preview_url", false));
        sendMessage(payload);
    }

    @Override
    public void sendButtonMessage(String recipientPhone, String headerText,
                                  String bodyText, List<ActionBtn> buttons) {
        List<Map<String, Object>> buttonList = new ArrayList<>();
        for (int i = 0; i < Math.min(buttons.size(), 3); i++) {
            buttonList.add(Map.of("type", "reply",
                    "reply", Map.of("id", buttons.get(i).id(), "title", buttons.get(i).title())));
        }
        Map<String, Object> payload = buildBasePayload(recipientPhone);
        payload.put("type", "interactive");
        payload.put("interactive", Map.of(
                "type", "button",
                "header", Map.of("type", "text", "text", headerText),
                "body", Map.of("text", bodyText),
                "action", Map.of("buttons", buttonList)
        ));
        sendMessage(payload);
    }

    public void sendListMessage(String recipientPhone, String bodyText, String buttonLabel,
                                String sectionTitle, Map<String, String> items) {
        List<Map<String, Object>> rows = new ArrayList<>();
        items.forEach((id, title) -> rows.add(Map.of("id", id, "title", title)));
        Map<String, Object> payload = buildBasePayload(recipientPhone);
        payload.put("type", "interactive");
        payload.put("interactive", Map.of(
                "type", "list", "body", Map.of("text", bodyText),
                "action", Map.of("button", buttonLabel,
                        "sections", List.of(Map.of("title", sectionTitle, "rows", rows)))
        ));
        sendMessage(payload);
    }

    @Override
    public void sendLocationRequest(String recipientPhone) {
        Map<String, Object> payload = buildBasePayload(recipientPhone);
        payload.put("type", "interactive");
        payload.put("interactive", Map.of(
                "type", "location_request_message",
                "body", Map.of("text", "📍 Please share your location for accurate navigation."),
                "action", Map.of("name", "send_location")
        ));
        sendMessage(payload);
    }

    @Override
    public void markAsRead(String messageId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("status", "read");
        payload.put("message_id", messageId);
        sendMessage(payload);
    }

    private Map<String, Object> buildBasePayload(String recipientPhone) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("recipient_type", "individual");
        payload.put("to", recipientPhone);
        return payload;
    }

    private void sendMessage(Map<String, Object> payload) {
        String url = String.format("%s/%s/%s/messages", baseUrl, apiVersion, phoneNumberId);
        try {
            String json = objectMapper.writeValueAsString(payload);
            Request request = new Request.Builder()
                    .url(url).post(RequestBody.create(json, JSON)).build();
                    
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    log.error("Failed to send WhatsApp message: {}", e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (response) {
                        if (!response.isSuccessful()) {
                            String body = response.body() != null ? response.body().string() : "null";
                            log.error("Meta API error [{}]: {}", response.code(), body);
                        }
                    }
                }
            });
        } catch (Exception e) {
            log.error("Error creating WhatsApp message: {}", e.getMessage());
        }
    }
}
