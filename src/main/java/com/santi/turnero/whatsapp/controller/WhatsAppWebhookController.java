package com.santi.turnero.whatsapp.controller;

import com.santi.turnero.whatsapp.dto.WhatsAppWebhookPayload;
import com.santi.turnero.whatsapp.service.WhatsAppWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks/whatsapp")
@RequiredArgsConstructor
public class WhatsAppWebhookController {

    private final WhatsAppWebhookService whatsAppWebhookService;

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

    @PostMapping
    public ResponseEntity<Void> receiveWebhook(@RequestBody(required = false) WhatsAppWebhookPayload payload) {
        whatsAppWebhookService.procesarWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
