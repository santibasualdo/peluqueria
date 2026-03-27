package com.santi.turnero.turno.service;

import com.santi.turnero.bloqueo.repository.BloqueoHorarioRepository;
import com.santi.turnero.peluqueria.domain.Peluqueria;
import com.santi.turnero.shared.dto.RangoHorarioDto;
import com.santi.turnero.shared.exception.BusinessException;
import com.santi.turnero.turno.domain.TurnoEstado;
import com.santi.turnero.turno.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AgendaValidationService {

    private static final Set<TurnoEstado> ESTADOS_QUE_OCUPAN_AGENDA =
            EnumSet.of(TurnoEstado.RESERVADO, TurnoEstado.EN_CURSO, TurnoEstado.COMPLETADO, TurnoEstado.AUSENTE);

    private final TurnoRepository turnoRepository;
    private final BloqueoHorarioRepository bloqueoHorarioRepository;

    @Transactional(readOnly = true)
    public void validarDisponibilidad(
            Peluqueria peluqueria,
            Long peluqueroId,
            LocalDateTime fechaHoraInicio,
            LocalDateTime fechaHoraFin,
            Long turnoIdIgnorado
    ) {
        validarRangoHorario(fechaHoraInicio, fechaHoraFin);
        validarHorarioDeAtencion(peluqueria, fechaHoraInicio, fechaHoraFin);
        validarSinSolapamiento(peluqueroId, fechaHoraInicio, fechaHoraFin, turnoIdIgnorado);
        validarSinBloqueos(peluqueria.getId(), peluqueroId, fechaHoraInicio, fechaHoraFin);
    }

    @Transactional(readOnly = true)
    public boolean estaDisponible(
            Peluqueria peluqueria,
            Long peluqueroId,
            LocalDateTime fechaHoraInicio,
            LocalDateTime fechaHoraFin
    ) {
        if (!esRangoValido(fechaHoraInicio, fechaHoraFin)) {
            return false;
        }

        if (!estaDentroDelHorario(peluqueria, fechaHoraInicio, fechaHoraFin)) {
            return false;
        }

        if (turnoRepository.existsSolapamiento(
                peluqueroId,
                fechaHoraInicio,
                fechaHoraFin,
                ESTADOS_QUE_OCUPAN_AGENDA,
                null
        )) {
            return false;
        }

        return !bloqueoHorarioRepository.existsBloqueoSolapado(
                peluqueria.getId(),
                peluqueroId,
                fechaHoraInicio,
                fechaHoraFin
        );
    }

    @Transactional(readOnly = true)
    public List<RangoHorarioDto> obtenerRangosDeTurnosQueOcupanAgenda(Long peluqueroId, LocalDateTime desde, LocalDateTime hasta) {
        return turnoRepository.findRangosOcupadosEnRango(peluqueroId, desde, hasta, ESTADOS_QUE_OCUPAN_AGENDA);
    }

    @Transactional(readOnly = true)
    public List<RangoHorarioDto> obtenerRangosDeBloqueos(Long peluqueriaId, Long peluqueroId, LocalDateTime desde, LocalDateTime hasta) {
        return bloqueoHorarioRepository.findRangosBloqueadosEnRango(peluqueriaId, peluqueroId, desde, hasta);
    }

    public boolean estaDisponibleEnMemoria(
            Peluqueria peluqueria,
            LocalDateTime fechaHoraInicio,
            LocalDateTime fechaHoraFin,
            Collection<RangoHorarioDto> turnosOcupados,
            Collection<RangoHorarioDto> bloqueos
    ) {
        if (!esRangoValido(fechaHoraInicio, fechaHoraFin)) {
            return false;
        }

        if (!estaDentroDelHorario(peluqueria, fechaHoraInicio, fechaHoraFin)) {
            return false;
        }

        return !haySolapamientoConTurnos(fechaHoraInicio, fechaHoraFin, turnosOcupados)
                && !haySolapamientoConBloqueos(fechaHoraInicio, fechaHoraFin, bloqueos);
    }

    private boolean haySolapamientoConTurnos(
            LocalDateTime fechaHoraInicio,
            LocalDateTime fechaHoraFin,
            Collection<RangoHorarioDto> turnosOcupados
    ) {
        return turnosOcupados.stream()
                .anyMatch(turno -> seSolapan(fechaHoraInicio, fechaHoraFin, turno.fechaHoraInicio(), turno.fechaHoraFin()));
    }

    private boolean haySolapamientoConBloqueos(
            LocalDateTime fechaHoraInicio,
            LocalDateTime fechaHoraFin,
            Collection<RangoHorarioDto> bloqueos
    ) {
        return bloqueos.stream()
                .anyMatch(bloqueo -> seSolapan(fechaHoraInicio, fechaHoraFin, bloqueo.fechaHoraInicio(), bloqueo.fechaHoraFin()));
    }

    private boolean seSolapan(
            LocalDateTime inicioA,
            LocalDateTime finA,
            LocalDateTime inicioB,
            LocalDateTime finB
    ) {
        return inicioA.isBefore(finB) && finA.isAfter(inicioB);
    }

    private void validarSinSolapamiento(
            Long peluqueroId,
            LocalDateTime fechaHoraInicio,
            LocalDateTime fechaHoraFin,
            Long turnoIdIgnorado
    ) {
        boolean existeSolapamiento = turnoRepository.existsSolapamiento(
                peluqueroId,
                fechaHoraInicio,
                fechaHoraFin,
                ESTADOS_QUE_OCUPAN_AGENDA,
                turnoIdIgnorado
        );

        if (existeSolapamiento) {
            throw new BusinessException("El peluquero ya tiene un turno asignado en ese horario.");
        }
    }

    private void validarSinBloqueos(
            Long peluqueriaId,
            Long peluqueroId,
            LocalDateTime fechaHoraInicio,
            LocalDateTime fechaHoraFin
    ) {
        boolean existeBloqueo = bloqueoHorarioRepository.existsBloqueoSolapado(
                peluqueriaId,
                peluqueroId,
                fechaHoraInicio,
                fechaHoraFin
        );

        if (existeBloqueo) {
            throw new BusinessException("El horario solicitado se encuentra bloqueado.");
        }
    }

    private void validarHorarioDeAtencion(Peluqueria peluqueria, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        if (!estaDentroDelHorario(peluqueria, fechaHoraInicio, fechaHoraFin)) {
            throw new BusinessException("El turno solicitado esta fuera del horario de atencion.");
        }
    }

    private void validarRangoHorario(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        if (!esRangoValido(fechaHoraInicio, fechaHoraFin)) {
            throw new BusinessException("La fecha y hora del turno son invalidas.");
        }
    }

    private boolean esRangoValido(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        return fechaHoraInicio != null
                && fechaHoraFin != null
                && fechaHoraFin.isAfter(fechaHoraInicio)
                && fechaHoraInicio.toLocalDate().equals(fechaHoraFin.toLocalDate());
    }

    private boolean estaDentroDelHorario(Peluqueria peluqueria, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        LocalTime inicio = fechaHoraInicio.toLocalTime();
        LocalTime fin = fechaHoraFin.toLocalTime();

        return !inicio.isBefore(peluqueria.getHoraApertura())
                && !fin.isAfter(peluqueria.getHoraCierre());
    }
}
