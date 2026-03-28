package com.santi.turnero.whatsapp.dto;

public record WhatsAppTextMessageRequest(
        String messaging_product,
        String recipient_type,
        String to,
        String type,
        WhatsAppTextBody text
) {
    public static WhatsAppTextMessageRequest of(String telefono, String mensaje) {
        return new WhatsAppTextMessageRequest(
                "whatsapp",
                "individual",
                telefono,
                "text",
                new WhatsAppTextBody(false, mensaje)
        );
    }

    public record WhatsAppTextBody(
            boolean preview_url,
            String body
    ) {
    }
}
