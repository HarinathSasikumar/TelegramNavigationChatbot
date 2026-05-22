package com.navigation.chatbot.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials.path:firebase-service-account.json}")
    private String credentialsPath;

    @Value("${firebase.project-id:}")
    private String projectId;

    @Bean
    public FirebaseApp firebaseApp() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }
        try {
            InputStream credentialsStream = getCredentialsStream();
            if (credentialsStream == null) {
                log.warn("#######################################################");
                log.warn("# SETUP REQUIRED: Firebase credentials missing!       #");
                log.warn("# Download 'firebase-service-account.json' and place   #");
                log.warn("# it in the project root to enable Chatbot features.  #");
                log.warn("#######################################################");
                return null;
            }
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .setProjectId(projectId.isEmpty() ? null : projectId)
                    .build();
            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("Firebase initialized. Project: {}", projectId);
            return app;
        } catch (IOException e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage());
            return null;
        }
    }

    @Bean
    public Firestore firestore(org.springframework.beans.factory.ObjectProvider<FirebaseApp> firebaseAppProvider) {
        FirebaseApp firebaseApp = firebaseAppProvider.getIfAvailable();
        if (firebaseApp == null) {
            log.warn("Firestore bean disabled - FirebaseApp is null.");
            return null;
        }
        return FirestoreClient.getFirestore(firebaseApp);
    }

    private InputStream getCredentialsStream() {
        try {
            String credentialsJson = System.getenv("FIREBASE_CREDENTIALS_JSON");
            if (credentialsJson != null && !credentialsJson.isBlank()) {
                log.info("Loading Firebase credentials from env variable.");
                return new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
            }
            log.info("Checking for Firebase credentials at: {}", credentialsPath);
            return new FileInputStream(credentialsPath);
        } catch (IOException e) {
            return null;
        }
    }
}
