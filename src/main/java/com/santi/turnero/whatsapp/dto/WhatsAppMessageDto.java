package com.santi.turnero.whatsapp.dto;

public record WhatsAppMessageDto(
        String from,
        String id,
        String timestamp,
        String type,
        WhatsAppTextDto text
) {
}
