package com.navigation.chatbot.service;

import java.util.List;

public interface MessagingPlatform {

    record ActionBtn(String id, String title) {}

    void sendTextMessage(String recipientId, String text);

    void sendButtonMessage(String recipientId, String headerText, String bodyText, List<ActionBtn> buttons);

    void sendLocationRequest(String recipientId);

    void markAsRead(String messageId);
}
