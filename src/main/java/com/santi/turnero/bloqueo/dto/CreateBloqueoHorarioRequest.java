package com.santi.turnero.bloqueo.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateBloqueoHorarioRequest(
        @NotNull Long peluqueriaId,
        Long peluqueroId,
        @NotNull @FutureOrPresent LocalDateTime fechaHoraInicio,
        @NotNull @FutureOrPresent LocalDateTime fechaHoraFin,
        @NotBlank @Size(max = 255) String motivo
) {
}
