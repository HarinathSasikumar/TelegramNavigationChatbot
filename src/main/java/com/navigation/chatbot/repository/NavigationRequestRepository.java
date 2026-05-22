package com.navigation.chatbot.repository;

import com.google.cloud.firestore.*;
import com.navigation.chatbot.model.NavigationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class NavigationRequestRepository {

    private static final Logger log = LoggerFactory.getLogger(NavigationRequestRepository.class);
    private static final String COLLECTION = "navigation_requests";

    private final Firestore firestore;
    private final boolean configured;

    public NavigationRequestRepository(ObjectProvider<Firestore> firestoreProvider) {
        this.firestore = firestoreProvider.getIfAvailable();
        this.configured = this.firestore != null;
    }

    private boolean isNotConfigured() {
        if (!configured) {
            return true;
        }
        return false;
    }

    public NavigationRequest save(NavigationRequest request) {
        if (isNotConfigured()) return request;
        request.setCreatedAt(Instant.now().toString());
        request.setStatus("PENDING");
        DocumentReference docRef = firestore.collection(COLLECTION).document();
        request.setId(docRef.getId());
        docRef.set(request);
        return request;
    }

    public List<NavigationRequest> findByPhoneNumber(String phoneNumber) {
        List<NavigationRequest> results = new ArrayList<>();
        if (isNotConfigured()) return results;
        try {
            List<QueryDocumentSnapshot> docs = firestore.collection(COLLECTION)
                    .whereEqualTo("phoneNumber", phoneNumber)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(10).get().get().getDocuments();
            for (QueryDocumentSnapshot doc : docs) {
                results.add(doc.toObject(NavigationRequest.class));
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error fetching nav history for {}: {}", phoneNumber, e.getMessage());
            Thread.currentThread().interrupt();
        }
        return results;
    }

    public void updateStatus(String requestId, String status) {
        if (isNotConfigured()) return;
        firestore.collection(COLLECTION)
                .document(requestId)
                .update("status", status, "updatedAt", Instant.now().toString());
    }
}
