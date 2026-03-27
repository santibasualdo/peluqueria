package com.santi.turnero.whatsapp.dto;

import java.util.List;

public record WhatsAppWebhookPayload(
        String object,
        List<WhatsAppEntryDto> entry
) {
}
