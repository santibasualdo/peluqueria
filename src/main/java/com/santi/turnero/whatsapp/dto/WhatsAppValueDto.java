package com.santi.turnero.whatsapp.dto;

import java.util.List;

public record WhatsAppValueDto(
        String messagingProduct,
        List<WhatsAppContactDto> contacts,
        List<WhatsAppMessageDto> messages
) {
}
