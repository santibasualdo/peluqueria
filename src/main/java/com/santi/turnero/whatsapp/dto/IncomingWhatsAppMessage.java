package com.santi.turnero.whatsapp.dto;

public record IncomingWhatsAppMessage(
        String messageId,
        String telefono,
        String texto,
        String nombreContacto
) {
}
