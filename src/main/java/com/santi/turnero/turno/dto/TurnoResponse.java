package com.santi.turnero.turno.dto;

import com.santi.turnero.turno.domain.TurnoEstado;

import java.time.LocalDateTime;

public record TurnoResponse(
        Long id,
        Long peluqueriaId,
        String peluqueriaNombre,
        Long peluqueroId,
        String peluqueroNombre,
        Long clienteId,
        String clienteNombre,
        String clienteTelefono,
        Long servicioId,
        String servicioNombre,
        Integer servicioDuracionMinutos,
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        TurnoEstado estado,
        String observaciones
) {
}
