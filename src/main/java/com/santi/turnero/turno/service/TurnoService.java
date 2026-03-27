package com.santi.turnero.turno.service;

import com.santi.turnero.cliente.domain.Cliente;
import com.santi.turnero.cliente.service.ClienteService;
import com.santi.turnero.peluqueria.domain.Peluqueria;
import com.santi.turnero.peluqueria.repository.PeluqueriaRepository;
import com.santi.turnero.peluquero.domain.Peluquero;
import com.santi.turnero.peluquero.repository.PeluqueroRepository;
import com.santi.turnero.servicio.domain.Servicio;
import com.santi.turnero.servicio.repository.ServicioRepository;
import com.santi.turnero.shared.exception.BusinessException;
import com.santi.turnero.shared.exception.ResourceNotFoundException;
import com.santi.turnero.turno.domain.Turno;
import com.santi.turnero.turno.domain.TurnoEstado;
import com.santi.turnero.turno.dto.CambiarEstadoTurnoRequest;
import com.santi.turnero.turno.dto.CancelTurnoRequest;
import com.santi.turnero.turno.dto.CreateTurnoRequest;
import com.santi.turnero.turno.dto.ReprogramarTurnoRequest;
import com.santi.turnero.turno.dto.TurnoResponse;
import com.santi.turnero.turno.dto.UpdateTurnoRequest;
import com.santi.turnero.turno.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TurnoService {

    private static final Set<TurnoEstado> ESTADOS_EDITABLES =
            Set.of(TurnoEstado.RESERVADO, TurnoEstado.EN_CURSO);

    private static final Set<TurnoEstado> ESTADOS_MANUALES_PERMITIDOS =
            Set.of(TurnoEstado.RESERVADO, TurnoEstado.EN_CURSO, TurnoEstado.COMPLETADO, TurnoEstado.AUSENTE);

    private final TurnoRepository turnoRepository;
    private final PeluqueriaRepository peluqueriaRepository;
    private final PeluqueroRepository peluqueroRepository;
    private final ServicioRepository servicioRepository;
    private final ClienteService clienteService;
    private final AgendaValidationService agendaValidationService;

    @Transactional
    public TurnoResponse crear(CreateTurnoRequest request) {
        Peluqueria peluqueria = obtenerPeluqueria(request.peluqueriaId());
        Peluquero peluquero = obtenerPeluquero(request.peluqueroId(), peluqueria.getId());
        Servicio servicio = obtenerServicio(request.servicioId(), peluqueria.getId());
        Cliente cliente = clienteService.obtenerOCrear(request.cliente());

        LocalDateTime fechaHoraFin = calcularFechaHoraFin(request.fechaHoraInicio(), servicio);
        agendaValidationService.validarDisponibilidad(
                peluqueria,
                peluquero.getId(),
                request.fechaHoraInicio(),
                fechaHoraFin,
                null
        );

        Turno turno = Turno.builder()
                .peluqueria(peluqueria)
                .peluquero(peluquero)
                .cliente(cliente)
                .servicio(servicio)
                .fechaHoraInicio(request.fechaHoraInicio())
                .fechaHoraFin(fechaHoraFin)
                .estado(TurnoEstado.RESERVADO)
                .observaciones(request.observaciones())
                .build();

        return toResponse(turnoRepository.save(turno));
    }

    @Transactional(readOnly = true)
    public List<TurnoResponse> listarPorFecha(Long peluqueriaId, LocalDate fecha) {
        LocalDateTime desde = fecha.atStartOfDay();
        LocalDateTime hasta = desde.plusDays(1);

        return turnoRepository.findByPeluqueriaIdAndFechaHoraInicioGreaterThanEqualAndFechaHoraInicioLessThanOrderByFechaHoraInicioAsc(
                        peluqueriaId,
                        desde,
                        hasta
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TurnoResponse> listarPorPeluquero(Long peluqueroId, LocalDate fecha) {
        LocalDateTime desde = fecha.atStartOfDay();
        LocalDateTime hasta = desde.plusDays(1);

        return turnoRepository.findByPeluqueroIdAndFechaHoraInicioGreaterThanEqualAndFechaHoraInicioLessThanOrderByFechaHoraInicioAsc(
                        peluqueroId,
                        desde,
                        hasta
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TurnoResponse> listarProximosDelCliente(Long peluqueriaId, Long clienteId) {
        return turnoRepository.findProximosDelCliente(
                        peluqueriaId,
                        clienteId,
                        LocalDateTime.now(),
                        Set.of(TurnoEstado.RESERVADO)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TurnoResponse editar(Long turnoId, UpdateTurnoRequest request) {
        Turno turno = obtenerTurno(turnoId);
        validarTurnoEditable(turno);

        Peluquero peluquero = obtenerPeluquero(request.peluqueroId(), turno.getPeluqueria().getId());
        Servicio servicio = obtenerServicio(request.servicioId(), turno.getPeluqueria().getId());
        Cliente cliente = clienteService.obtenerOCrear(request.cliente());
        LocalDateTime fechaHoraFin = calcularFechaHoraFin(request.fechaHoraInicio(), servicio);

        agendaValidationService.validarDisponibilidad(
                turno.getPeluqueria(),
                peluquero.getId(),
                request.fechaHoraInicio(),
                fechaHoraFin,
                turno.getId()
        );

        turno.setPeluquero(peluquero);
        turno.setServicio(servicio);
        turno.setCliente(cliente);
        turno.setFechaHoraInicio(request.fechaHoraInicio());
        turno.setFechaHoraFin(fechaHoraFin);
        turno.setObservaciones(request.observaciones());

        return toResponse(turnoRepository.save(turno));
    }

    @Transactional
    public TurnoResponse cancelar(Long turnoId, CancelTurnoRequest request) {
        Turno turno = obtenerTurno(turnoId);

        if (turno.getEstado() == TurnoEstado.CANCELADO) {
            throw new BusinessException("El turno ya se encuentra cancelado.");
        }

        turno.setEstado(TurnoEstado.CANCELADO);
        if (request != null && request.observaciones() != null && !request.observaciones().isBlank()) {
            turno.setObservaciones(request.observaciones().trim());
        }

        return toResponse(turnoRepository.save(turno));
    }

    @Transactional
    public TurnoResponse reactivar(Long turnoId) {
        Turno turno = obtenerTurno(turnoId);

        if (turno.getEstado() != TurnoEstado.CANCELADO) {
            throw new BusinessException("Solo se pueden reactivar turnos cancelados.");
        }

        agendaValidationService.validarDisponibilidad(
                turno.getPeluqueria(),
                turno.getPeluquero().getId(),
                turno.getFechaHoraInicio(),
                turno.getFechaHoraFin(),
                turno.getId()
        );

        turno.setEstado(TurnoEstado.RESERVADO);
        return toResponse(turnoRepository.save(turno));
    }

    @Transactional
    public TurnoResponse reprogramar(Long turnoId, ReprogramarTurnoRequest request) {
        Turno turno = obtenerTurno(turnoId);
        validarTurnoEditable(turno);

        Peluquero peluquero = request.nuevoPeluqueroId() != null
                ? obtenerPeluquero(request.nuevoPeluqueroId(), turno.getPeluqueria().getId())
                : turno.getPeluquero();

        Servicio servicio = request.nuevoServicioId() != null
                ? obtenerServicio(request.nuevoServicioId(), turno.getPeluqueria().getId())
                : turno.getServicio();

        LocalDateTime fechaHoraFin = calcularFechaHoraFin(request.nuevaFechaHoraInicio(), servicio);
        agendaValidationService.validarDisponibilidad(
                turno.getPeluqueria(),
                peluquero.getId(),
                request.nuevaFechaHoraInicio(),
                fechaHoraFin,
                turno.getId()
        );

        turno.setPeluquero(peluquero);
        turno.setServicio(servicio);
        turno.setFechaHoraInicio(request.nuevaFechaHoraInicio());
        turno.setFechaHoraFin(fechaHoraFin);
        turno.setEstado(TurnoEstado.RESERVADO);

        if (request.observaciones() != null && !request.observaciones().isBlank()) {
            turno.setObservaciones(request.observaciones().trim());
        }

        return toResponse(turnoRepository.save(turno));
    }

    @Transactional
    public TurnoResponse cambiarEstado(Long turnoId, CambiarEstadoTurnoRequest request) {
        Turno turno = obtenerTurno(turnoId);

        if (!ESTADOS_MANUALES_PERMITIDOS.contains(request.estado())) {
            throw new BusinessException("El estado solicitado no puede modificarse manualmente desde este endpoint.");
        }

        if (turno.getEstado() == TurnoEstado.CANCELADO) {
            throw new BusinessException("No es posible cambiar el estado de un turno cancelado.");
        }

        turno.setEstado(request.estado());
        return toResponse(turnoRepository.save(turno));
    }

    private Peluqueria obtenerPeluqueria(Long peluqueriaId) {
        return peluqueriaRepository.findById(peluqueriaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la peluqueria solicitada."));
    }

    private Peluquero obtenerPeluquero(Long peluqueroId, Long peluqueriaId) {
        Peluquero peluquero = peluqueroRepository.findById(peluqueroId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el peluquero solicitado."));

        if (!peluquero.getPeluqueria().getId().equals(peluqueriaId)) {
            throw new BusinessException("El peluquero no pertenece a la peluqueria indicada.");
        }
        return peluquero;
    }

    private Servicio obtenerServicio(Long servicioId, Long peluqueriaId) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el servicio solicitado."));

        if (!servicio.getPeluqueria().getId().equals(peluqueriaId)) {
            throw new BusinessException("El servicio no pertenece a la peluqueria indicada.");
        }

        if (servicio.getDuracionMinutos() == null || servicio.getDuracionMinutos() <= 0) {
            throw new BusinessException("La duracion del servicio debe ser mayor a cero.");
        }

        return servicio;
    }

    private Turno obtenerTurno(Long turnoId) {
        return turnoRepository.findById(turnoId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el turno solicitado."));
    }

    private void validarTurnoEditable(Turno turno) {
        if (!ESTADOS_EDITABLES.contains(turno.getEstado())) {
            throw new BusinessException("Solo se pueden editar o reprogramar turnos reservados o en curso.");
        }
    }

    private LocalDateTime calcularFechaHoraFin(LocalDateTime fechaHoraInicio, Servicio servicio) {
        return fechaHoraInicio.plusMinutes(servicio.getDuracionMinutos());
    }

    private TurnoResponse toResponse(Turno turno) {
        return new TurnoResponse(
                turno.getId(),
                turno.getPeluqueria().getId(),
                turno.getPeluqueria().getNombre(),
                turno.getPeluquero().getId(),
                turno.getPeluquero().getNombre(),
                turno.getCliente().getId(),
                turno.getCliente().getNombre(),
                turno.getCliente().getTelefono(),
                turno.getServicio().getId(),
                turno.getServicio().getNombre(),
                turno.getServicio().getDuracionMinutos(),
                turno.getFechaHoraInicio(),
                turno.getFechaHoraFin(),
                turno.getEstado(),
                turno.getObservaciones()
        );
    }
}
