package com.santi.turnero.turno.service;

import com.santi.turnero.peluqueria.domain.Peluqueria;
import com.santi.turnero.servicio.repository.ServicioRepository;
import com.santi.turnero.shared.dto.RangoHorarioDto;
import com.santi.turnero.shared.exception.BusinessException;
import com.santi.turnero.shared.exception.ResourceNotFoundException;
import com.santi.turnero.turno.dto.DisponibilidadContexto;
import com.santi.turnero.turno.dto.DisponibilidadResponse;
import com.santi.turnero.turno.dto.FranjaDisponibleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DisponibilidadService {

    private static final int INTERVALO_DEFAULT_MINUTOS = 30;

    private final ServicioRepository servicioRepository;
    private final AgendaValidationService agendaValidationService;

    @Transactional(readOnly = true)
    public DisponibilidadResponse consultar(
            Long peluqueriaId,
            Long peluqueroId,
            Long servicioId,
            LocalDate fecha,
            Integer intervaloMinutos
    ) {
        int intervalo = intervaloMinutos == null ? INTERVALO_DEFAULT_MINUTOS : intervaloMinutos;
        if (intervalo <= 0) {
            throw new BusinessException("El intervalo de disponibilidad debe ser mayor a cero.");
        }

        DisponibilidadContexto contexto = servicioRepository.findDisponibilidadContexto(peluqueriaId, peluqueroId, servicioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro una combinacion valida de peluqueria, peluquero y servicio."
                ));

        if (contexto.duracionMinutos() == null || contexto.duracionMinutos() <= 0) {
            throw new BusinessException("La duracion del servicio debe ser mayor a cero.");
        }

        Peluqueria peluqueria = Peluqueria.builder()
                .id(contexto.peluqueriaId())
                .horaApertura(contexto.horaApertura())
                .horaCierre(contexto.horaCierre())
                .build();

        List<FranjaDisponibleResponse> franjas = new ArrayList<>();
        LocalDateTime cursor = calcularInicioBusqueda(fecha, contexto.horaApertura(), intervalo);
        LocalDateTime cierre = fecha.atTime(contexto.horaCierre());
        LocalDateTime ahora = LocalDateTime.now();
        List<RangoHorarioDto> turnosOcupados = agendaValidationService.obtenerRangosDeTurnosQueOcupanAgenda(
                peluqueroId,
                cursor,
                cierre
        );
        List<RangoHorarioDto> bloqueos = agendaValidationService.obtenerRangosDeBloqueos(
                peluqueriaId,
                peluqueroId,
                cursor,
                cierre
        );

        while (!cursor.plusMinutes(contexto.duracionMinutos()).isAfter(cierre)) {
            LocalDateTime fin = cursor.plusMinutes(contexto.duracionMinutos());

            if ((fecha.isAfter(ahora.toLocalDate()) || !cursor.isBefore(ahora))
                    && agendaValidationService.estaDisponibleEnMemoria(
                            peluqueria,
                            cursor,
                            fin,
                            turnosOcupados,
                            bloqueos
                    )) {
                franjas.add(new FranjaDisponibleResponse(cursor, fin));
            }

            cursor = cursor.plusMinutes(intervalo);
        }

        return new DisponibilidadResponse(peluqueriaId, peluqueroId, servicioId, fecha, intervalo, franjas);
    }

    private LocalDateTime calcularInicioBusqueda(LocalDate fecha, LocalTime horaApertura, int intervaloMinutos) {
        LocalDateTime apertura = fecha.atTime(horaApertura);
        LocalDateTime ahora = LocalDateTime.now();

        if (!fecha.equals(ahora.toLocalDate())) {
            return apertura;
        }

        if (ahora.isBefore(apertura)) {
            return apertura;
        }

        LocalDateTime base = ahora.withSecond(0).withNano(0);
        int minutoActual = base.getMinute();
        int resto = minutoActual % intervaloMinutos;

        if (resto != 0) {
            base = base.plusMinutes(intervaloMinutos - resto);
        }

        return base.isBefore(apertura) ? apertura : base;
    }
}
