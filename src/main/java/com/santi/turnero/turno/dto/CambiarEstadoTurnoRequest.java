package com.santi.turnero.turno.dto;

import com.santi.turnero.turno.domain.TurnoEstado;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoTurnoRequest(
        @NotNull TurnoEstado estado
) {
}
