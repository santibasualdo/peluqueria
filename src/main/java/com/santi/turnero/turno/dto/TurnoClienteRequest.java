package com.santi.turnero.turno.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TurnoClienteRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Size(max = 30) String telefono,
        @Size(max = 500) String observaciones
) {
}
