package com.santi.turnero.whatsapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.santi.turnero.whatsapp.dto.WhatsAppWebhookPayload;
import com.santi.turnero.whatsapp.service.WhatsAppWebhookSignatureService;
import com.santi.turnero.whatsapp.service.WhatsAppWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks/whatsapp")
@RequiredArgsConstructor
public class WhatsAppWebhookController {

    private final WhatsAppWebhookService whatsAppWebhookService;
    private final WhatsAppWebhookSignatureService whatsAppWebhookSignatureService;
    private final ObjectMapper objectMapper;

    @Value("${whatsapp.webhook.verify-token}")
    private String verifyToken;

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge
    ) {
        if ("subscribe".equals(mode) && verifyToken.equals(token) && challenge != null) {
            return ResponseEntity.ok(challenge);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Webhook verification failed");
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> receiveWebhook(
            @RequestHeader(name = "X-Hub-Signature-256", required = false) String signatureHeader,
            @RequestBody(required = false) String rawPayload
    ) {
        if (!whatsAppWebhookSignatureService.isValid(signatureHeader, rawPayload)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WhatsAppWebhookPayload payload = parsePayload(rawPayload);
        whatsAppWebhookService.procesarWebhook(payload);
        return ResponseEntity.ok().build();
    }

    private WhatsAppWebhookPayload parsePayload(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(rawPayload, WhatsAppWebhookPayload.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("El payload del webhook de WhatsApp es invalido.", exception);
        }
    }
}
