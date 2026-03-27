package com.santi.turnero.turno.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateTurnoRequest(
        @NotNull Long peluqueroId,
        @NotNull Long servicioId,
        @NotNull @Valid TurnoClienteRequest cliente,
        @NotNull @FutureOrPresent LocalDateTime fechaHoraInicio,
        @Size(max = 500) String observaciones
) {
}
