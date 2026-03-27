package com.santi.turnero.servicio.dto;

public record ServicioResponse(
        Long id,
        String nombre,
        Integer duracionMinutos,
        Long peluqueriaId
) {
}
