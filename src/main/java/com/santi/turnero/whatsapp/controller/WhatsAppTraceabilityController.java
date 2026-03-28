package com.santi.turnero.whatsapp.controller;

import com.santi.turnero.whatsapp.dto.WhatsAppConversationTraceResponse;
import com.santi.turnero.whatsapp.dto.WhatsAppMessageTraceResponse;
import com.santi.turnero.whatsapp.service.WhatsAppTraceabilityAccessService;
import com.santi.turnero.whatsapp.service.WhatsAppTraceabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/whatsapp/conversations")
@RequiredArgsConstructor
public class WhatsAppTraceabilityController {

    private final WhatsAppTraceabilityService whatsAppTraceabilityService;
    private final WhatsAppTraceabilityAccessService whatsAppTraceabilityAccessService;

    @GetMapping
    public ResponseEntity<List<WhatsAppConversationTraceResponse>> listarConversaciones(
            @RequestHeader(name = "X-Traceability-Token", required = false) String traceabilityToken,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) Boolean soloActivas
    ) {
        if (!whatsAppTraceabilityAccessService.isAuthorized(traceabilityToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(whatsAppTraceabilityService.listarConversaciones(telefono, soloActivas));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<WhatsAppMessageTraceResponse>> listarMensajes(
            @RequestHeader(name = "X-Traceability-Token", required = false) String traceabilityToken,
            @PathVariable Long conversationId
    ) {
        if (!whatsAppTraceabilityAccessService.isAuthorized(traceabilityToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(whatsAppTraceabilityService.listarMensajes(conversationId));
    }
}
