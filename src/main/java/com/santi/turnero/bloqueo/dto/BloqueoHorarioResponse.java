package com.santi.turnero.bloqueo.dto;

import java.time.LocalDateTime;

public record BloqueoHorarioResponse(
        Long id,
        Long peluqueriaId,
        Long peluqueroId,
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        String motivo
) {
}
