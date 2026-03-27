package com.santi.turnero.turno.dto;

import jakarta.validation.constraints.Size;

public record CancelTurnoRequest(
        @Size(max = 500) String observaciones
) {
}
