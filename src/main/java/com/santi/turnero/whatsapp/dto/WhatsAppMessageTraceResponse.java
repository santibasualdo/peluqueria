package com.santi.turnero.whatsapp.dto;

import com.santi.turnero.whatsapp.domain.WhatsAppMessageDirection;

import java.time.LocalDateTime;

public record WhatsAppMessageTraceResponse(
        Long id,
        Long conversationId,
        String telefono,
        WhatsAppMessageDirection direction,
        String mensaje,
        LocalDateTime createdAt
) {
}
