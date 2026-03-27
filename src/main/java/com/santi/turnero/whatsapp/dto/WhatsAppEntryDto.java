package com.santi.turnero.whatsapp.dto;

import java.util.List;

public record WhatsAppEntryDto(
        String id,
        List<WhatsAppChangeDto> changes
) {
}
