package com.santi.turnero.whatsapp.dto;

public record WhatsAppContactDto(
        WhatsAppProfileDto profile,
        String waId
) {
}
