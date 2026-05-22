package com.navigation.chatbot.repository;

import com.google.cloud.firestore.*;
import com.navigation.chatbot.model.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class UserSessionRepository {

    private static final Logger log = LoggerFactory.getLogger(UserSessionRepository.class);
    private static final String COLLECTION = "user_sessions";

    private final Firestore firestore;
    private final boolean configured;

    public UserSessionRepository(ObjectProvider<Firestore> firestoreProvider) {
        this.firestore = firestoreProvider.getIfAvailable();
        this.configured = this.firestore != null;
    }

    public boolean isConfigured() {
        return configured;
    }

    private boolean isNotConfigured() {
        if (!configured) {
            // Only log if we haven't warned yet, or keep it quiet for the dashboard
            return true;
        }
        return false;
    }

    public Optional<UserSession> findByPhoneNumber(String phoneNumber) {
        if (isNotConfigured()) return Optional.empty();
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION).document(phoneNumber).get().get();
            if (doc.exists()) return Optional.ofNullable(doc.toObject(UserSession.class));
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error fetching session for {}: {}", phoneNumber, e.getMessage());
            Thread.currentThread().interrupt();
        }
        return Optional.empty();
    }

    public void save(UserSession session) {
        if (isNotConfigured()) return;
        session.setUpdatedAt(Instant.now().toString());
        firestore.collection(COLLECTION).document(session.getPhoneNumber()).set(session);
    }

    public void updateFields(String phoneNumber, Map<String, Object> updates) {
        if (isNotConfigured()) return;
        updates.put("updatedAt", Instant.now().toString());
        firestore.collection(COLLECTION).document(phoneNumber).update(updates);
    }

    public void incrementMessageCount(String phoneNumber) {
        if (isNotConfigured()) return;
        Map<String, Object> updates = new HashMap<>();
        updates.put("messageCount", FieldValue.increment(1));
        updates.put("updatedAt", Instant.now().toString());
        firestore.collection(COLLECTION).document(phoneNumber).update(updates);
    }

    public void delete(String phoneNumber) {
        if (isNotConfigured()) return;
        firestore.collection(COLLECTION).document(phoneNumber).delete();
    }

    public long getTotalUsersCount() {
        if (isNotConfigured()) return 0;
        try {
            log.info("Fetching aggregate user count from collection: {}", COLLECTION);
            return firestore.collection(COLLECTION).count().get().get().getCount();
        } catch (InterruptedException | ExecutionException e) {
            log.error("CRITICAL: Failed to count users in {}. Error: {}", COLLECTION, e.getMessage());
            Thread.currentThread().interrupt();
            return 0;
        } catch (Exception e) {
            log.error("UNEXPECTED ERROR: Counting users failed. {}", e.getMessage());
            return 0;
        }
    }

    public long getTotalMessagesCount() {
        if (isNotConfigured()) return 0;
        try {
            log.info("Calculating total messages across all users in collection: {}", COLLECTION);
            long total = 0;
            QuerySnapshot snapshot = firestore.collection(COLLECTION).get().get();
            for (QueryDocumentSnapshot doc : snapshot) {
                Long count = doc.getLong("messageCount");
                if (count != null) total += count;
            }
            log.info("Total messages calculated: {}", total);
            return total;
        } catch (InterruptedException | ExecutionException e) {
            log.error("CRITICAL: Failed to sum messages in {}. Error: {}", COLLECTION, e.getMessage());
            Thread.currentThread().interrupt();
            return 0;
        } catch (Exception e) {
            log.error("UNEXPECTED ERROR: Summing messages failed. {}", e.getMessage());
            return 0;
        }
    }
}
