package com.santi.turnero.turno.dto;

import java.time.LocalDate;
import java.util.List;

public record DisponibilidadResponse(
        Long peluqueriaId,
        Long peluqueroId,
        Long servicioId,
        LocalDate fecha,
        Integer intervaloMinutos,
        List<FranjaDisponibleResponse> franjas
) {
}
