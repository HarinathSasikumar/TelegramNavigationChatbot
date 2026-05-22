package com.navigation.chatbot.service;

import com.navigation.chatbot.model.*;
import com.navigation.chatbot.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock private WhatsAppMessageService whatsappService;
    @Mock private InstagramMessageService instagramService;
    @Mock private TelegramMessageService telegramService;
    @Mock private UserSessionRepository sessionRepository;
    @Mock private NavigationRequestRepository navigationRepository;
    @InjectMocks private ChatbotService chatbotService;

    private static final String PHONE = "919876543210";

    @BeforeEach
    void setUp() {
        when(sessionRepository.findByPhoneNumber(anyString())).thenReturn(Optional.empty());
        doNothing().when(sessionRepository).save(any());
        doNothing().when(sessionRepository).incrementMessageCount(anyString());
    }

    @Test @DisplayName("New user says 'hi' → receives welcome message and asked for destination")
    void testNewUserHi() {
        chatbotService.processMessage("whatsapp", PHONE, "Harinath", "text", "hi", null, null, null);
        verify(whatsappService).sendTextMessage(eq(PHONE), contains("Harinath"));
        verify(whatsappService).sendTextMessage(eq(PHONE), contains("Where would you like to go"));
        verify(sessionRepository, atLeastOnce()).save(argThat(s -> UserSession.State.AWAITING_DESTINATION.equals(s.getCurrentState())));
    }

    @Test @DisplayName("User in AWAITING_DESTINATION types destination → location requested")
    void testDestinationInput() {
        UserSession session = UserSession.newSession(PHONE, "Test");
        session.setCurrentState(UserSession.State.AWAITING_DESTINATION);
        when(sessionRepository.findByPhoneNumber(PHONE)).thenReturn(Optional.of(session));

        chatbotService.processMessage("whatsapp", PHONE, "Test", "text", "Eiffel Tower, Paris", null, null, null);

        verify(whatsappService).sendTextMessage(eq(PHONE), contains("Eiffel Tower"));
        verify(whatsappService).sendLocationRequest(PHONE);
    }

    @Test @DisplayName("'help' command works in any state")
    void testHelpCommand() {
        UserSession session = UserSession.newSession(PHONE, "Test");
        session.setCurrentState(UserSession.State.NAVIGATION_ACTIVE);
        when(sessionRepository.findByPhoneNumber(PHONE)).thenReturn(Optional.of(session));

        chatbotService.processMessage("whatsapp", PHONE, "Test", "text", "help", null, null, null);
        verify(whatsappService).sendTextMessage(eq(PHONE), contains("Troubleshooting"));
    }

    @Test @DisplayName("'reset' resets session to WELCOME")
    void testResetCommand() {
        UserSession session = UserSession.newSession(PHONE, "Test");
        session.setCurrentState(UserSession.State.NAVIGATION_ACTIVE);
        when(sessionRepository.findByPhoneNumber(PHONE)).thenReturn(Optional.of(session));

        chatbotService.processMessage("whatsapp", PHONE, "Test", "text", "reset", null, null, null);
        verify(sessionRepository, atLeastOnce()).save(argThat(s -> UserSession.State.WELCOME.equals(s.getCurrentState())));
    }

    @Test @DisplayName("Location message in AWAITING_LOCATION state → route options shown")
    void testLocationMessage() {
        UserSession session = UserSession.newSession(PHONE, "Test");
        session.setCurrentState(UserSession.State.AWAITING_LOCATION);
        session.setDestination("AIIMS Delhi");
        when(sessionRepository.findByPhoneNumber(PHONE)).thenReturn(Optional.of(session));

        chatbotService.processMessage("whatsapp", PHONE, "Test", "location", null, null, 28.5665, 77.2100);
        verify(whatsappService).sendButtonMessage(eq(PHONE), contains("Route"), contains("AIIMS Delhi"), anyList());
    }
}
