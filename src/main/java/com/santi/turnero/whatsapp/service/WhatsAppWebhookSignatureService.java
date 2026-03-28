package com.santi.turnero.whatsapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Slf4j
@Service
public class WhatsAppWebhookSignatureService {

    private static final String SIGNATURE_PREFIX = "sha256=";
    private static final String HMAC_SHA256 = "HmacSHA256";

    private final String appSecret;

    public WhatsAppWebhookSignatureService(@Value("${whatsapp.webhook.app-secret:}") String appSecret) {
        this.appSecret = normalize(appSecret);
    }

    public boolean isValid(String signatureHeader, String rawPayload) {
        if (appSecret == null) {
            log.warn("No se puede validar la firma del webhook de WhatsApp porque falta whatsapp.webhook.app-secret.");
            return false;
        }

        String signature = normalize(signatureHeader);
        if (signature == null || !signature.regionMatches(true, 0, SIGNATURE_PREFIX, 0, SIGNATURE_PREFIX.length())) {
            return false;
        }

        String providedDigest = signature.substring(SIGNATURE_PREFIX.length()).toLowerCase();
        String expectedDigest = calculateDigest(rawPayload == null ? "" : rawPayload);

        return MessageDigest.isEqual(
                expectedDigest.getBytes(StandardCharsets.UTF_8),
                providedDigest.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String calculateDigest(String rawPayload) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] digest = mac.doFinal(rawPayload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo calcular la firma del webhook de WhatsApp.", exception);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
