package com.santi.turnero.shared.dto;

import java.time.LocalDateTime;

public record RangoHorarioDto(
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin
) {
}
