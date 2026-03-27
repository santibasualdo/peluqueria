package com.santi.turnero.turno.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ReprogramarTurnoRequest(
        @NotNull @FutureOrPresent LocalDateTime nuevaFechaHoraInicio,
        Long nuevoPeluqueroId,
        Long nuevoServicioId,
        @Size(max = 500) String observaciones
) {
}
