package com.santi.turnero.whatsapp.dto;

public record IncomingWhatsAppMessage(
        String telefono,
        String texto,
        String nombreContacto
) {
}
