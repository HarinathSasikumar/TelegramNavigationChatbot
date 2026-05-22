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
public class TelegramMessageService implements MessagingPlatform {

    private static final Logger log = LoggerFactory.getLogger(TelegramMessageService.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${telegram.bot.token:}")
    private String botToken;

    private static final String BASE_URL = "https://api.telegram.org/bot";

    public TelegramMessageService(OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendTextMessage(String recipientId, String text) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("chat_id", recipientId);
        payload.put("text", text);
        payload.put("parse_mode", "Markdown"); // Gives bold/italic support similar to WhatsApp 
        
        sendMessage(payload, "sendMessage");
    }

    @Override
    public void sendButtonMessage(String recipientId, String headerText, String bodyText, List<ActionBtn> buttons) {
        String fullText = headerText != null ? "*" + headerText + "*\n" + bodyText : bodyText;
        
        // Convert to Telegram Inline Keyboard Buttons
        List<List<Map<String, Object>>> inlineKeyboard = new ArrayList<>();
        List<Map<String, Object>> row = new ArrayList<>();
        
        for (ActionBtn btn : buttons) {
            row.add(Map.of("text", btn.title(), "callback_data", btn.id()));
            // Stack buttons vertically if more than 2, or whatever looks best
            if (row.size() == 2) {
                inlineKeyboard.add(new ArrayList<>(row));
                row.clear();
            }
        }
        if (!row.isEmpty()) {
            inlineKeyboard.add(row);
        }

        Map<String, Object> replyMarkup = Map.of("inline_keyboard", inlineKeyboard);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("chat_id", recipientId);
        payload.put("text", fullText);
        payload.put("parse_mode", "Markdown");
        payload.put("reply_markup", replyMarkup);

        sendMessage(payload, "sendMessage");
    }

    @Override
    public void sendLocationRequest(String recipientId) {
        // Telegram supports robust location requests via Reply Keyboard
        List<List<Map<String, Object>>> keyboard = List.of(
            List.of(Map.of(
                "text", "📍 Share Location",
                "request_location", true
            ))
        );

        Map<String, Object> replyMarkup = Map.of(
            "keyboard", keyboard,
            "one_time_keyboard", true,
            "resize_keyboard", true
        );

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("chat_id", recipientId);
        payload.put("text", "Please tap the button below to share your live location, or tap the attachment icon (📎) and select Location.");
        payload.put("reply_markup", replyMarkup);

        sendMessage(payload, "sendMessage");
    }

    @Override
    public void markAsRead(String messageId) {
        // Telegram marks as read automatically upon fetch
    }

    private void sendMessage(Map<String, Object> payload, String method) {
        if (botToken == null || botToken.isEmpty()) {
            log.warn("Telegram Bot Token is not configured. Cannot send message.");
            return;
        }

        String url = BASE_URL + botToken + "/" + method;
        try {
            String json = objectMapper.writeValueAsString(payload);
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(json, JSON))
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    log.error("Failed to send Telegram message: {}", e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (response) {
                        if (!response.isSuccessful()) {
                            String body = response.body() != null ? response.body().string() : "null";
                            log.error("Telegram API error [{}]: {}", response.code(), body);
                        }
                    }
                }
            });
        } catch (Exception e) {
            log.error("Error creating Telegram message request: {}", e.getMessage());
        }
    }
}
