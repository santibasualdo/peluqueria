package com.santi.turnero.whatsapp.model;

public record WhatsAppConversationResult(
        String telefono,
        String mensajeBot,
        WhatsAppConversationStep step
) {
}
