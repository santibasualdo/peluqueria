package com.santi.turnero.turno.dto;

import java.time.LocalDateTime;

public record FranjaDisponibleResponse(
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin
) {
}
