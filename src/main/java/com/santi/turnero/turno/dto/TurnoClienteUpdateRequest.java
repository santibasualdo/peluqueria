package com.santi.turnero.turno.dto;

import jakarta.validation.constraints.Size;

public record TurnoClienteUpdateRequest(
        @Size(max = 120) String nombre,
        @Size(max = 30) String telefono,
        @Size(max = 500) String observaciones
) {
}
