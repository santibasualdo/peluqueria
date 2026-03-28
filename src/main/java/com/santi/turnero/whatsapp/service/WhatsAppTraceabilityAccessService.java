package com.santi.turnero.whatsapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class WhatsAppTraceabilityAccessService {

    private final String adminToken;

    public WhatsAppTraceabilityAccessService(@Value("${whatsapp.traceability.admin-token:}") String adminToken) {
        this.adminToken = normalize(adminToken);
    }

    public boolean isAuthorized(String providedToken) {
        String normalizedProvidedToken = normalize(providedToken);
        if (adminToken == null || normalizedProvidedToken == null) {
            return false;
        }

        return MessageDigest.isEqual(
                adminToken.getBytes(StandardCharsets.UTF_8),
                normalizedProvidedToken.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
