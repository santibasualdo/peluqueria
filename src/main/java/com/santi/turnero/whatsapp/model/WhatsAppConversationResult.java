package com.santi.turnero.whatsapp.model;

public record WhatsAppConversationResult(
        Long conversationId,
        String telefono,
        String mensajeBot,
        WhatsAppConversationStep step
) {
}
