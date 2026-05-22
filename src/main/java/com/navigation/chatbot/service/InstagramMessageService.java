package com.navigation.chatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class InstagramMessageService implements MessagingPlatform {

    private static final Logger log = LoggerFactory.getLogger(InstagramMessageService.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${meta.instagram.page-access-token:}")
    private String pageAccessToken;

    @Value("${meta.instagram.api-version:v19.0}")
    private String apiVersion;

    private static final String BASE_URL = "https://graph.facebook.com";

    public InstagramMessageService(OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendTextMessage(String recipientId, String text) {
        Map<String, Object> payload = buildBasePayload(recipientId);
        payload.put("message", Map.of("text", text));
        sendMessage(payload);
    }

    @Override
    public void sendButtonMessage(String recipientId, String headerText, String bodyText, List<ActionBtn> buttons) {
        // Instagram uses quick replies for button interactions
        List<Map<String, Object>> quickReplies = new ArrayList<>();
        for (int i = 0; i < Math.min(buttons.size(), 13); i++) {
            ActionBtn btn = buttons.get(i);
            quickReplies.add(Map.of(
                    "content_type", "text",
                    "title", btn.title(),
                    "payload", btn.id()
            ));
        }

        Map<String, Object> payload = buildBasePayload(recipientId);
        String fullText = headerText != null ? "*" + headerText + "*\n" + bodyText : bodyText;
        payload.put("message", Map.of(
                "text", fullText,
                "quick_replies", quickReplies
        ));
        sendMessage(payload);
    }

    @Override
    public void sendLocationRequest(String recipientId) {
        // Instagram does not support native location picker quick replies like WhatsApp.
        // We will send a fallback message instructing them to share a map link or text.
        sendTextMessage(recipientId, "📍 Please reply with an exact address or paste a Google Maps link to your destination.");
    }

    @Override
    public void markAsRead(String messageId) {
        // Instagram Graph API automatically marks messages as read when fetched via webhook,
        // or we can explicitly send a mark-seen request. For now, empty implementation.
    }

    private Map<String, Object> buildBasePayload(String recipientId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("recipient", Map.of("id", recipientId));
        return payload;
    }

    private void sendMessage(Map<String, Object> payload) {
        if (pageAccessToken == null || pageAccessToken.isEmpty()) {
            log.warn("Instagram Page Access Token is not configured. Cannot send message.");
            return;
        }
        
        String url = String.format("%s/%s/me/messages?access_token=%s", BASE_URL, apiVersion, pageAccessToken);
        try {
            String json = objectMapper.writeValueAsString(payload);
            Request request = new Request.Builder()
                    .url(url).post(RequestBody.create(json, JSON)).build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    log.error("Failed to send Instagram message: {}", e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (response) {
                        if (!response.isSuccessful()) {
                            String body = response.body() != null ? response.body().string() : "null";
                            log.error("Meta IG API error [{}]: {}", response.code(), body);
                        }
                    }
                }
            });
        } catch (Exception e) {
            log.error("Error creating Instagram message: {}", e.getMessage());
        }
    }
}
