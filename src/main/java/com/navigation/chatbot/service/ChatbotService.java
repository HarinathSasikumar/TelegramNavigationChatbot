package com.navigation.chatbot.service;

import com.navigation.chatbot.model.*;
import com.navigation.chatbot.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    private final WhatsAppMessageService whatsappService;
    private final InstagramMessageService instagramService;
    private final TelegramMessageService telegramService;
    private final UserSessionRepository sessionRepository;
    private final NavigationRequestRepository navigationRepository;

    public ChatbotService(WhatsAppMessageService whatsappService,
                          InstagramMessageService instagramService,
                          TelegramMessageService telegramService,
                          UserSessionRepository sessionRepository,
                          NavigationRequestRepository navigationRepository) {
        this.whatsappService = whatsappService;
        this.instagramService = instagramService;
        this.telegramService = telegramService;
        this.sessionRepository = sessionRepository;
        this.navigationRepository = navigationRepository;
    }

    private MessagingPlatform getPlatform(String platformId) {
        if ("instagram".equalsIgnoreCase(platformId)) return instagramService;
        if ("telegram".equalsIgnoreCase(platformId)) return telegramService;
        return whatsappService;
    }

    @Async("taskExecutor")
    public void processMessage(String platform, String senderId, String senderName,
                               String messageType, String text, String payloadId,
                               Double lat, Double lng) {
        
        log.info("[{}] Message from {} ({}), type: {}", platform, senderName, senderId, messageType);
        MessagingPlatform messageService = getPlatform(platform);
        
        // Fire off background tasks immediately
        CompletableFuture.runAsync(() -> sessionRepository.incrementMessageCount(senderId));
        
        UserSession session = sessionRepository.findByPhoneNumber(senderId)
                .orElseGet(() -> UserSession.newSession(senderId, senderName));
        session.setName(senderName);

        switch (messageType) {
            case "text"        -> handleTextMessage(text, session, messageService);
            case "interactive" -> handleInteractiveMessage(payloadId, session, messageService);
            case "location"    -> handleLocationMessage(lat, lng, session, messageService);
            default            -> messageService.sendTextMessage(senderId,
                    "⚠️ *Format Not Supported*\n\nI received a message type I don't recognize. To get started, I can understand:\n• Text messages\n• Shared locations\n• Button clicks\n\nPlease type *menu* to see a list of valid options and restart your session.");
        }
    }

    private void handleTextMessage(String text, UserSession session, MessagingPlatform messageService) {
        String phone = session.getPhoneNumber();
        String lower = text != null ? text.trim().toLowerCase() : "";

        if (lower.matches("hi|hello|start|menu")) { sendWelcomeMessage(session, messageService); return; }
        if (lower.equals("help"))                  { sendHelpMessage(phone, messageService); return; }
        if (lower.matches("reset|restart"))        { resetSession(session, messageService); return; }

        switch (session.getCurrentState()) {
            case UserSession.State.WELCOME,
                 UserSession.State.AWAITING_DESTINATION -> handleDestinationInput(text, session, messageService);
            case UserSession.State.NAVIGATION_ACTIVE    -> handleActiveNavigationInput(lower, session, messageService);
            default -> { session.setCurrentState(UserSession.State.WELCOME);
                         sessionRepository.save(session);
                         sendWelcomeMessage(session, messageService); }
        }
    }

    private void handleInteractiveMessage(String replyId, UserSession session, MessagingPlatform messageService) {
        if (replyId == null || replyId.isEmpty()) return;

        switch (replyId) {
            case "btn_0", "btn_navigate" -> { session.setCurrentState(UserSession.State.AWAITING_DESTINATION);
                              sessionRepository.save(session);
                              messageService.sendTextMessage(session.getPhoneNumber(),
                                      "🗺️ Where would you like to go? Type your destination."); }
            case "btn_1", "btn_history" -> sendNavigationHistory(session, messageService);
            case "btn_2", "btn_help" -> sendHelpMessage(session.getPhoneNumber(), messageService);
            case "route_drive"   -> startNavigation(session, "DRIVING", messageService);
            case "route_walk"    -> startNavigation(session, "WALKING", messageService);
            case "route_transit" -> startNavigation(session, "TRANSIT", messageService);
            case "nav_stop"      -> stopNavigation(session, messageService);
            case "nav_share"     -> shareNavigationLink(session, messageService);
            case "nav_restart"   -> { session.setCurrentState(UserSession.State.AWAITING_DESTINATION);
                                      sessionRepository.save(session);
                                      messageService.sendTextMessage(session.getPhoneNumber(), "🔄 Enter a new destination:"); }
            default -> messageService.sendTextMessage(session.getPhoneNumber(),
                    "🤔 *Command Unrecognized*\n\nI couldn't process that selection. To keep things moving, please try typing *menu* to bring up your primary options, or type *help* if you are stuck.");
        }
    }

    private void handleLocationMessage(Double lat, Double lng, UserSession session, MessagingPlatform messageService) {
        if (lat == null || lng == null) {
            
            // Check if they pasted a Maps link instead for fallback
            if (session.getCurrentState().equals(UserSession.State.AWAITING_LOCATION)) {
                messageService.sendTextMessage(session.getPhoneNumber(), "Sorry, I couldn't parse that location. Please try typing the address manually.");
            }
            return;
        }

        Map<String, Object> context = session.getContextData() != null
                ? new HashMap<>(session.getContextData()) : new HashMap<>();
        context.put("userLatitude",  lat);
        context.put("userLongitude", lng);
        session.setContextData(context);

        if (UserSession.State.AWAITING_LOCATION.equals(session.getCurrentState()) && session.getDestination() != null) {
            session.setCurrentState(UserSession.State.SHOWING_OPTIONS);
            sessionRepository.save(session);
            sendRouteTypeSelection(session, messageService);
        } else {
            session.setCurrentState(UserSession.State.AWAITING_DESTINATION);
            sessionRepository.save(session);
            messageService.sendTextMessage(session.getPhoneNumber(),
                    "📍 Location received! Where would you like to navigate to?");
        }
    }

    private void handleDestinationInput(String destination, UserSession session, MessagingPlatform messageService) {
        session.setDestination(destination);
        session.setCurrentState(UserSession.State.AWAITING_LOCATION);
        sessionRepository.save(session);
        messageService.sendTextMessage(session.getPhoneNumber(),
                "🎯 Destination set: *" + destination + "*\n\nNow please share your current location.");
        messageService.sendLocationRequest(session.getPhoneNumber());
    }

    private void sendRouteTypeSelection(UserSession session, MessagingPlatform messageService) {
        messageService.sendButtonMessage(session.getPhoneNumber(), "🗺️ Route Options",
                "How would you like to travel to *" + session.getDestination() + "*?",
                List.of(
                        new MessagingPlatform.ActionBtn("route_drive", "🚗 Drive"),
                        new MessagingPlatform.ActionBtn("route_walk", "🚶 Walk"),
                        new MessagingPlatform.ActionBtn("route_transit", "🚌 Transit")
                ));
    }

    private void startNavigation(UserSession session, String routeType, MessagingPlatform messageService) {
        String phone = session.getPhoneNumber();
        String dest  = session.getDestination();
        Map<String, Object> ctx = session.getContextData();

        NavigationRequest navRequest = NavigationRequest.builder()
                .phoneNumber(phone).userName(session.getName()).destination(dest)
                .startLatitude(ctx != null ? (Double) ctx.get("userLatitude")  : null)
                .startLongitude(ctx != null ? (Double) ctx.get("userLongitude") : null)
                .routeType(routeType).status("IN_PROGRESS").build();
        navigationRepository.save(navRequest);

        session.setCurrentState(UserSession.State.NAVIGATION_ACTIVE);
        Map<String, Object> ctx2 = ctx != null ? new HashMap<>(ctx) : new HashMap<>();
        ctx2.put("currentNavRequestId", navRequest.getId());
        session.setContextData(ctx2);
        sessionRepository.save(session);

        String travelMode = switch (routeType) { case "WALKING" -> "w"; case "TRANSIT" -> "r"; default -> "d"; };
        String originParams = "";
        if (ctx != null && ctx.get("userLatitude") != null && ctx.get("userLongitude") != null) {
            originParams = "&origin=" + ctx.get("userLatitude") + "," + ctx.get("userLongitude");
        }
        String mapsLink   = "https://www.google.com/maps/dir/?api=1&destination="
                            + URLEncoder.encode(dest, StandardCharsets.UTF_8) + "&travelmode=" + travelMode + originParams;

        messageService.sendTextMessage(phone, String.format(
                "🚗 *Navigation Started!*\n\n📍 *Destination:* %s\n🛣️ *Mode:* %s\n\n🔗 Open in Maps:\n%s",
                dest, routeType, mapsLink));
        messageService.sendButtonMessage(phone, "Navigation Controls",
                "What would you like to do?", List.of(
                        new MessagingPlatform.ActionBtn("nav_stop", "⏹️ Stop"),
                        new MessagingPlatform.ActionBtn("nav_share", "🔗 Share Link"),
                        new MessagingPlatform.ActionBtn("nav_restart", "🔄 New Dest")
                ));
    }

    private void stopNavigation(UserSession session, MessagingPlatform messageService) {
        Map<String, Object> ctx = session.getContextData();
        if (ctx != null && ctx.get("currentNavRequestId") != null)
            navigationRepository.updateStatus((String) ctx.get("currentNavRequestId"), "COMPLETED");
        session.setCurrentState(UserSession.State.WELCOME);
        session.setDestination(null);
        sessionRepository.save(session);
        messageService.sendTextMessage(session.getPhoneNumber(), "✅ Navigation stopped. Have a safe journey!");
        sendWelcomeMessage(session, messageService);
    }

    private void shareNavigationLink(UserSession session, MessagingPlatform messageService) {
        String dest = session.getDestination();
        messageService.sendTextMessage(session.getPhoneNumber(),
                "🔗 *Share link:*\nhttps://www.google.com/maps/search/?api=1&query=" + URLEncoder.encode(dest, StandardCharsets.UTF_8));
    }

    private void sendNavigationHistory(UserSession session, MessagingPlatform messageService) {
        List<NavigationRequest> history = navigationRepository.findByPhoneNumber(session.getPhoneNumber());
        if (history.isEmpty()) {
            messageService.sendTextMessage(session.getPhoneNumber(),
                    "📋 No navigation history yet. Type *menu* to start!"); return;
        }
        StringBuilder sb = new StringBuilder("📋 *Your Recent Navigations:*\n\n");
        for (int i = 0; i < Math.min(history.size(), 5); i++) {
            NavigationRequest r = history.get(i);
            sb.append(String.format("%d. 📍 *%s*\n   Mode: %s | Status: %s\n\n",
                    i + 1, r.getDestination(), r.getRouteType(), r.getStatus()));
        }
        messageService.sendTextMessage(session.getPhoneNumber(), sb.toString().trim());
    }

    private void handleActiveNavigationInput(String lower, UserSession session, MessagingPlatform messageService) {
        if (lower.contains("stop") || lower.contains("cancel")) { stopNavigation(session, messageService); return; }
        messageService.sendButtonMessage(session.getPhoneNumber(), "Navigation Active",
                "You're navigating to *" + session.getDestination() + "*. What next?",
                List.of(
                        new MessagingPlatform.ActionBtn("nav_stop", "⏹️ Stop"),
                        new MessagingPlatform.ActionBtn("nav_share", "🔗 Share Link"),
                        new MessagingPlatform.ActionBtn("nav_restart", "🔄 New Dest")
                ));
    }

    private void resetSession(UserSession session, MessagingPlatform messageService) {
        session.setCurrentState(UserSession.State.WELCOME);
        session.setDestination(null); session.setContextData(null);
        sessionRepository.save(session);
        sendWelcomeMessage(session, messageService);
    }

    private void sendWelcomeMessage(UserSession session, MessagingPlatform messageService) {
        String name = session.getName() != null ? session.getName() : "there";
        
        // Step 1: Send the exact greeting reply
        messageService.sendTextMessage(session.getPhoneNumber(),
                String.format("Hello %s! 👋\nWelcome to the *Navigation Chatbot*!", name));

        // Step 2: Set state and prompt for destination
        session.setCurrentState(UserSession.State.AWAITING_DESTINATION);
        sessionRepository.save(session);
        messageService.sendTextMessage(session.getPhoneNumber(), 
                "🗺️ Where would you like to go? Type your destination.");
    }

    private void sendHelpMessage(String phone, MessagingPlatform messageService) {
        messageService.sendTextMessage(phone, """
                🆘 *Help & Troubleshooting Guide*
                
                Welcome to the Nexus Navigation Assistant. Here is how you can use this bot:
                
                1️⃣ *Getting Directions:* Type 'menu' and click 'Navigate', or simply share your location using the WhatsApp attachment menu.
                2️⃣ *Reviewing Past Trips:* Select 'History' from the main menu to see where you've been.
                
                📝 *Useful Commands:*
                • `menu` — Return to the main menu
                • `reset` — Clear your current session and start fresh
                • `stop` — Halt an active navigation route
                • `help` — Show this detailed guide
                
                💡 *Pro Tip:* If you ever get stuck in a flow, simply type *reset* to clear the system's memory.
                
                📞 *Still need assistance?*
                Contact us at support@nexus-navigation.com""");
    }
}

