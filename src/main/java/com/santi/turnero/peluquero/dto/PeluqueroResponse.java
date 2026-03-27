package com.santi.turnero.peluquero.dto;

public record PeluqueroResponse(
        Long id,
        String nombre,
        String telefono,
        Long peluqueriaId
) {
}
