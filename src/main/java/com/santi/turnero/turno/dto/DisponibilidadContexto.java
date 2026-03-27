package com.santi.turnero.turno.dto;

import java.time.LocalTime;

public record DisponibilidadContexto(
        Long peluqueriaId,
        Long peluqueroId,
        Long servicioId,
        LocalTime horaApertura,
        LocalTime horaCierre,
        Integer duracionMinutos
) {
}
