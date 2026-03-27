package com.santi.turnero.whatsapp.service;

import com.santi.turnero.cliente.domain.Cliente;
import com.santi.turnero.cliente.service.ClienteService;
import com.santi.turnero.shared.exception.BusinessException;
import com.santi.turnero.shared.exception.ResourceNotFoundException;
import com.santi.turnero.turno.dto.CreateTurnoRequest;
import com.santi.turnero.turno.dto.DisponibilidadResponse;
import com.santi.turnero.turno.dto.FranjaDisponibleResponse;
import com.santi.turnero.turno.dto.ReprogramarTurnoRequest;
import com.santi.turnero.turno.dto.TurnoClienteRequest;
import com.santi.turnero.turno.dto.TurnoResponse;
import com.santi.turnero.turno.service.DisponibilidadService;
import com.santi.turnero.turno.service.TurnoService;
import com.santi.turnero.whatsapp.dto.IncomingWhatsAppMessage;
import com.santi.turnero.whatsapp.model.WhatsAppConversationResult;
import com.santi.turnero.whatsapp.model.WhatsAppConversationSession;
import com.santi.turnero.whatsapp.model.WhatsAppConversationStep;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class WhatsAppConversationService {

    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final LocalTime FRANJA_MANIANA_FIN_EXCLUSIVA = LocalTime.of(13, 0);
    private static final String MENU_DIAS = """
            ¿Para qué día querés reservar?

            1. Hoy
            2. Mañana
            3. Pasado mañana
            0. Empezar de nuevo
            """;

    private static final String MENU_FRANJAS = """
            ¿Qué franja preferís?

            1. Mañana
            2. Tarde
            0. Empezar de nuevo
            """;

    private final ConcurrentMap<String, WhatsAppConversationSession> sessions = new ConcurrentHashMap<>();
    private final ClienteService clienteService;
    private final DisponibilidadService disponibilidadService;
    private final TurnoService turnoService;
    private final Long peluqueriaId;
    private final Long peluqueroId;
    private final Long servicioId;
    private final Integer intervaloMinutos;

    public WhatsAppConversationService(
            ClienteService clienteService,
            DisponibilidadService disponibilidadService,
            TurnoService turnoService,
            @Value("${whatsapp.booking.peluqueria-id:1}") Long peluqueriaId,
            @Value("${whatsapp.booking.peluquero-id:1}") Long peluqueroId,
            @Value("${whatsapp.booking.servicio-id:1}") Long servicioId,
            @Value("${whatsapp.booking.intervalo-minutos:30}") Integer intervaloMinutos
    ) {
        this.clienteService = clienteService;
        this.disponibilidadService = disponibilidadService;
        this.turnoService = turnoService;
        this.peluqueriaId = peluqueriaId;
        this.peluqueroId = peluqueroId;
        this.servicioId = servicioId;
        this.intervaloMinutos = intervaloMinutos;
    }

    public WhatsAppConversationResult procesar(IncomingWhatsAppMessage incomingMessage) {
        String telefono = normalize(incomingMessage.telefono());
        String texto = normalize(incomingMessage.texto());

        if (telefono == null) {
            return new WhatsAppConversationResult(null, "No pude identificar el número del remitente.", null);
        }

        Optional<Cliente> clienteExistente = clienteService.buscarPorTelefono(telefono);

        if ("0".equals(texto) || isRestartRequest(texto)) {
            sessions.remove(telefono);
            return iniciarFlujo(telefono, clienteExistente);
        }

        WhatsAppConversationSession session = sessions.get(telefono);
        if (session == null) {
            return iniciarFlujo(telefono, clienteExistente);
        }

        return switch (session.getStep()) {
            case PENDIENTE_NOMBRE_COMPLETO -> manejarNombreCompleto(session, texto);
            case MENU_PRINCIPAL -> manejarMenuPrincipal(session, texto);
            case PENDIENTE_DIA -> manejarDia(session, texto);
            case PENDIENTE_FRANJA -> manejarFranja(session, texto);
            case PENDIENTE_ALTERNATIVA_SIN_DISPONIBILIDAD -> manejarAlternativaSinDisponibilidad(session, texto);
            case PENDIENTE_HORARIO -> manejarHorario(session, texto);
            case PENDIENTE_CONFIRMACION -> manejarConfirmacion(session, texto);
            case PENDIENTE_CONFIRMACION_CANCELACION -> manejarConfirmacionCancelacion(session, texto);
            case PENDIENTE_CANCELACION_TURNO -> new WhatsAppConversationResult(
                    session.getTelefono(),
                    "Volvamos al menú principal.\n\n" + buildMenuPrincipal(session),
                    WhatsAppConversationStep.MENU_PRINCIPAL
            );
        };
    }

    private WhatsAppConversationResult iniciarFlujo(String telefono, Optional<Cliente> clienteExistente) {
        if (clienteExistente.isPresent()) {
            Cliente cliente = clienteExistente.get();
            List<TurnoResponse> proximosTurnos = turnoService.listarProximosDelCliente(peluqueriaId, cliente.getId());

            WhatsAppConversationSession session = WhatsAppConversationSession.builder()
                    .telefono(telefono)
                    .clienteId(cliente.getId())
                    .nombre(cliente.getNombre())
                    .turnosParaCancelar(proximosTurnos)
                    .reprogramando(false)
                    .step(WhatsAppConversationStep.MENU_PRINCIPAL)
                    .build();

            sessions.put(telefono, session);
            return new WhatsAppConversationResult(telefono, buildMenuPrincipal(session), session.getStep());
        }

        WhatsAppConversationSession session = WhatsAppConversationSession.builder()
                .telefono(telefono)
                .reprogramando(false)
                .step(WhatsAppConversationStep.PENDIENTE_NOMBRE_COMPLETO)
                .build();

        sessions.put(telefono, session);
        return new WhatsAppConversationResult(
                telefono,
                "Perfecto. Voy a ayudarte a reservar un turno.\n¿Cuál es tu nombre y apellido?\n\n0. Empezar de nuevo",
                session.getStep()
        );
    }

    private WhatsAppConversationResult manejarNombreCompleto(WhatsAppConversationSession session, String texto) {
        if (texto == null || texto.isBlank()) {
            return new WhatsAppConversationResult(
                    session.getTelefono(),
                    "No entendí tu nombre y apellido. Escribilos nuevamente, por favor.\n\n0. Empezar de nuevo",
                    session.getStep()
            );
        }

        String nombreCompleto = normalizeSpaces(texto);
        String[] partes = nombreCompleto.split(" ", 2);
        if (partes.length < 2) {
            return new WhatsAppConversationResult(
                    session.getTelefono(),
                    "Necesito que me escribas nombre y apellido para seguir.\n\n0. Empezar de nuevo",
                    session.getStep()
            );
        }

        session.setNombre(capitalizeWords(partes[0]));
        session.setApellido(capitalizeWords(partes[1]));

        Cliente cliente = clienteService.registrarOActualizarDesdeWhatsApp(
                session.getTelefono(),
                session.getNombreCompleto()
        );
        session.setClienteId(cliente.getId());
        session.setTurnosParaCancelar(List.of());
        session.setReprogramando(false);
        session.setTurnoSeleccionadoId(null);
        session.setStep(WhatsAppConversationStep.PENDIENTE_DIA);

        return new WhatsAppConversationResult(
                session.getTelefono(),
                "Gracias, " + session.getNombreCompleto() + ".\n" + MENU_DIAS,
                session.getStep()
        );
    }

    private WhatsAppConversationResult manejarMenuPrincipal(WhatsAppConversationSession session, String texto) {
        boolean tieneProximoTurno = session.getTurnosParaCancelar() != null && !session.getTurnosParaCancelar().isEmpty();

        if (!tieneProximoTurno) {
            if ("1".equals(texto)) {
                session.setReprogramando(false);
                session.setTurnoSeleccionadoId(null);
                session.setStep(WhatsAppConversationStep.PENDIENTE_DIA);
                return new WhatsAppConversationResult(session.getTelefono(), MENU_DIAS, session.getStep());
            }

            return new WhatsAppConversationResult(
                    session.getTelefono(),
                    "No entendí la opción.\n\n¿Qué querés hacer?\n\n1. Reservar turno\n0. Empezar de nuevo",
                    session.getStep()
            );
        }

        TurnoResponse proximoTurno = session.getTurnosParaCancelar().get(0);
        return switch (texto) {
            case "1" -> {
                session.setReprogramando(true);
                session.setTurnoSeleccionadoId(proximoTurno.id());
                session.setStep(WhatsAppConversationStep.PENDIENTE_DIA);
                yield new WhatsAppConversationResult(
                        session.getTelefono(),
                        "Perfecto, vamos a modificar tu turno.\n" + MENU_DIAS,
                        session.getStep()
                );
            }
            case "2" -> {
                session.setReprogramando(false);
                session.setTurnoSeleccionadoId(proximoTurno.id());
                session.setStep(WhatsAppConversationStep.PENDIENTE_CONFIRMACION_CANCELACION);
                yield new WhatsAppConversationResult(
                        session.getTelefono(),
                        "Voy a cancelar tu turno del "
                                + formatFecha(proximoTurno.fechaHoraInicio())
                                + " a las " + formatHora(proximoTurno.fechaHoraInicio())
                                + ".\n\n1. Confirmar cancelación\n2. Volver\n0. Empezar de nuevo",
                        session.getStep()
                );
            }
            case "3" -> {
                session.setReprogramando(false);
                session.setTurnoSeleccionadoId(null);
                session.setStep(WhatsAppConversationStep.PENDIENTE_DIA);
                yield new WhatsAppConversationResult(
                        session.getTelefono(),
                        "Perfecto, busquemos un nuevo turno.\n" + MENU_DIAS,
                        session.getStep()
                );
            }
            default -> new WhatsAppConversationResult(
                    session.getTelefono(),
                    "No entendí la opción.\n\n" + buildMenuPrincipalOptions(session),
                    session.getStep()
            );
        };
    }

    private WhatsAppConversationResult manejarDia(WhatsAppConversationSession session, String texto) {
        String dia = switch (texto) {
            case "1" -> "Hoy";
            case "2" -> "Mañana";
            case "3" -> "Pasado mañana";
            default -> null;
        };

        if (dia == null) {
            return new WhatsAppConversationResult(
                    session.getTelefono(),
                    "No entendí la opción.\nRespondé con:\n1. Hoy\n2. Mañana\n3. Pasado mañana\n0. Empezar de nuevo",
                    session.getStep()
            );
        }

        session.setDiaSeleccionado(dia);
        session.setFechaSeleccionada(resolveFecha(texto));
        session.setHorariosOfrecidos(null);
        session.setHorarioSeleccionado(null);
        session.setStep(WhatsAppConversationStep.PENDIENTE_FRANJA);

        return new WhatsAppConversationResult(session.getTelefono(), "Bien.\n" + MENU_FRANJAS, session.getStep());
    }

    private WhatsAppConversationResult manejarFranja(WhatsAppConversationSession session, String texto) {
        String franja = switch (texto) {
            case "1" -> "Mañana";
            case "2" -> "Tarde";
            default -> null;
        };

        if (franja == null) {
            return new WhatsAppConversationResult(
                    session.getTelefono(),
                    "No entendí la opción.\nRespondé con:\n1. Mañana\n2. Tarde\n0. Empezar de nuevo",
                    session.getStep()
            );
        }

        session.setFranjaSeleccionada(franja);
        return ofrecerHorarios(session, franja);
    }

    private WhatsAppConversationResult manejarAlternativaSinDisponibilidad(WhatsAppConversationSession session, String texto) {
        return switch (texto) {
            case "1" -> {
                String nuevaFranja = "Mañana".equalsIgnoreCase(session.getFranjaSeleccionada()) ? "Tarde" : "Mañana";
                session.setFranjaSeleccionada(nuevaFranja);
                yield ofrecerHorarios(session, nuevaFranja);
            }
            case "2" -> {
                session.setHorariosOfrecidos(null);
                session.setHorarioSeleccionado(null);
                session.setStep(WhatsAppConversationStep.PENDIENTE_DIA);
                yield new WhatsAppConversationResult(session.getTelefono(), "Perfecto, elijamos otro día.\n" + MENU_DIAS, session.getStep());
            }
            case "3" -> cancelarConversacion(session, "Perfecto, cancelé esta solicitud. Si querés arrancar de nuevo, mandame cualquier mensaje.");
            default -> new WhatsAppConversationResult(
                    session.getTelefono(),
                    "No entendí la opción.\nRespondé con:\n1. Ver " + franjaAlternativa(session.getFranjaSeleccionada()).toLowerCase()
                            + "\n2. Elegir otro día\n3. Cancelar\n0. Empezar de nuevo",
                    session.getStep()
            );
        };
    }

    private WhatsAppConversationResult manejarHorario(WhatsAppConversationSession session, String texto) {
        List<LocalDateTime> horarios = session.getHorariosOfrecidos();
        if (horarios == null || horarios.isEmpty()) {
            session.setStep(WhatsAppConversationStep.PENDIENTE_FRANJA);
            return new WhatsAppConversationResult(
                    session.getTelefono(),
                    "No encontré horarios cargados para esta conversación. Volvamos a elegir la franja.\n" + MENU_FRANJAS,
                    session.getStep()
            );
        }

        Integer indice = parsePositiveInteger(texto);
        if (indice == null || indice < 1 || indice > horarios.size()) {
            return new WhatsAppConversationResult(
                    session.getTelefono(),
                    "No entendí la opción.\nRespondé con el número del horario que querés elegir.\n\n0. Empezar de nuevo",
                    session.getStep()
            );
        }

        LocalDateTime horarioSeleccionado = horarios.get(indice - 1);
        session.setHorarioSeleccionado(horarioSeleccionado);
        session.setStep(WhatsAppConversationStep.PENDIENTE_CONFIRMACION);

        return new WhatsAppConversationResult(
                session.getTelefono(),
                "Perfecto.\nVoy a " + (session.isReprogramando() ? "modificar" : "reservar") + " un turno para "
                        + session.getDiaSeleccionado().toLowerCase()
                        + " a las " + formatHora(horarioSeleccionado)
                        + " a nombre de " + session.getNombreCompleto()
                        + ".\n\n1. Confirmar\n2. Cancelar\n0. Empezar de nuevo",
                session.getStep()
        );
    }

    private WhatsAppConversationResult manejarConfirmacion(WhatsAppConversationSession session, String texto) {
        return switch (texto) {
            case "1" -> confirmarReserva(session);
            case "2" -> cancelarConversacion(session, "Perfecto, cancelé esta solicitud. Si querés arrancar de nuevo, mandame cualquier mensaje.");
            default -> new WhatsAppConversationResult(
                    session.getTelefono(),
                    "No entendí la opción.\nRespondé con:\n1. Confirmar\n2. Cancelar\n0. Empezar de nuevo",
                    session.getStep()
            );
        };
    }

    private WhatsAppConversationResult manejarConfirmacionCancelacion(WhatsAppConversationSession session, String texto) {
        return switch (texto) {
            case "1" -> confirmarCancelacion(session);
            case "2" -> {
                session.setTurnoSeleccionadoId(null);
                session.setStep(WhatsAppConversationStep.MENU_PRINCIPAL);
                yield new WhatsAppConversationResult(session.getTelefono(), buildMenuPrincipal(session), session.getStep());
            }
            default -> new WhatsAppConversationResult(
                    session.getTelefono(),
                    "No entendí la opción.\nRespondé con:\n1. Confirmar cancelación\n2. Volver\n0. Empezar de nuevo",
                    session.getStep()
            );
        };
    }

    private WhatsAppConversationResult ofrecerHorarios(WhatsAppConversationSession session, String franja) {
        List<LocalDateTime> horarios = consultarHorariosDisponibles(session.getFechaSeleccionada(), franja);

        if (horarios.isEmpty()) {
            session.setHorariosOfrecidos(null);
            session.setHorarioSeleccionado(null);
            session.setStep(WhatsAppConversationStep.PENDIENTE_ALTERNATIVA_SIN_DISPONIBILIDAD);

            return new WhatsAppConversationResult(
                    session.getTelefono(),
                    "No tengo horarios disponibles para "
                            + session.getDiaSeleccionado().toLowerCase()
                            + " por la " + franja.toLowerCase()
                            + ".\n\n1. Ver " + franjaAlternativa(franja).toLowerCase()
                            + "\n2. Elegir otro día\n3. Cancelar\n0. Empezar de nuevo",
                    session.getStep()
            );
        }

        session.setHorariosOfrecidos(horarios);
        session.setHorarioSeleccionado(null);
        session.setStep(WhatsAppConversationStep.PENDIENTE_HORARIO);

        return new WhatsAppConversationResult(
                session.getTelefono(),
                "Tengo estos horarios disponibles para "
                        + session.getDiaSeleccionado().toLowerCase()
                        + " por la " + franja.toLowerCase()
                        + ":\n\n"
                        + buildHorariosMenu(horarios)
                        + "\n\nRespondé con el número de opción.\n\n0. Empezar de nuevo",
                session.getStep()
        );
    }

    private List<LocalDateTime> consultarHorariosDisponibles(LocalDate fecha, String franja) {
        try {
            DisponibilidadResponse disponibilidad = disponibilidadService.consultar(
                    peluqueriaId,
                    peluqueroId,
                    servicioId,
                    fecha,
                    intervaloMinutos
            );

            return disponibilidad.franjas().stream()
                    .map(FranjaDisponibleResponse::fechaHoraInicio)
                    .filter(horario -> perteneceAFranja(horario.toLocalTime(), franja))
                    .limit(5)
                    .toList();
        } catch (BusinessException | ResourceNotFoundException exception) {
            return List.of();
        }
    }

    private boolean perteneceAFranja(LocalTime horario, String franja) {
        if ("Mañana".equalsIgnoreCase(franja)) {
            return horario.isBefore(FRANJA_MANIANA_FIN_EXCLUSIVA);
        }
        return !horario.isBefore(FRANJA_MANIANA_FIN_EXCLUSIVA);
    }

    private WhatsAppConversationResult confirmarReserva(WhatsAppConversationSession session) {
        if (session.getHorarioSeleccionado() == null) {
            session.setStep(WhatsAppConversationStep.PENDIENTE_HORARIO);
            return new WhatsAppConversationResult(
                    session.getTelefono(),
                    "No encontré el horario seleccionado. Volvamos a elegir una opción.\n\n0. Empezar de nuevo",
                    session.getStep()
            );
        }

        try {
            TurnoResponse turno;
            if (session.isReprogramando()) {
                turno = turnoService.reprogramar(
                        session.getTurnoSeleccionadoId(),
                        new ReprogramarTurnoRequest(
                                session.getHorarioSeleccionado(),
                                peluqueroId,
                                servicioId,
                                "Turno reprogramado desde flujo conversacional de WhatsApp"
                        )
                );
            } else {
                turno = turnoService.crear(new CreateTurnoRequest(
                        peluqueriaId,
                        peluqueroId,
                        servicioId,
                        new TurnoClienteRequest(session.getNombreCompleto(), session.getTelefono(), null),
                        session.getHorarioSeleccionado(),
                        "Reserva creada desde flujo conversacional de WhatsApp"
                ));
            }

            sessions.remove(session.getTelefono());
            return new WhatsAppConversationResult(
                    session.getTelefono(),
                    "Listo, tu turno fue " + (session.isReprogramando() ? "modificado" : "reservado")
                            + " para " + session.getDiaSeleccionado().toLowerCase()
                            + " a las " + formatHora(turno.fechaHoraInicio()) + ".",
                    null
            );
        } catch (BusinessException exception) {
            return manejarHorarioOcupado(session);
        }
    }

    private WhatsAppConversationResult confirmarCancelacion(WhatsAppConversationSession session) {
        if (session.getTurnoSeleccionadoId() == null) {
            session.setStep(WhatsAppConversationStep.MENU_PRINCIPAL);
            return new WhatsAppConversationResult(session.getTelefono(), buildMenuPrincipal(session), session.getStep());
        }

        turnoService.cancelar(session.getTurnoSeleccionadoId(), null);
        sessions.remove(session.getTelefono());
        return new WhatsAppConversationResult(session.getTelefono(), "Listo, cancelé tu turno correctamente.", null);
    }

    private WhatsAppConversationResult manejarHorarioOcupado(WhatsAppConversationSession session) {
        List<LocalDateTime> horariosActualizados = consultarHorariosDisponibles(
                session.getFechaSeleccionada(),
                session.getFranjaSeleccionada()
        );

        if (horariosActualizados.isEmpty()) {
            session.setHorariosOfrecidos(null);
            session.setHorarioSeleccionado(null);
            session.setStep(WhatsAppConversationStep.PENDIENTE_ALTERNATIVA_SIN_DISPONIBILIDAD);
            return new WhatsAppConversationResult(
                    session.getTelefono(),
                    "Ese horario acaba de ocuparse y ya no quedan opciones para "
                            + session.getDiaSeleccionado().toLowerCase()
                            + " por la " + session.getFranjaSeleccionada().toLowerCase()
                            + ".\n\n1. Ver " + franjaAlternativa(session.getFranjaSeleccionada()).toLowerCase()
                            + "\n2. Elegir otro día\n3. Cancelar\n0. Empezar de nuevo",
                    session.getStep()
            );
        }

        session.setHorariosOfrecidos(horariosActualizados);
        session.setHorarioSeleccionado(null);
        session.setStep(WhatsAppConversationStep.PENDIENTE_HORARIO);

        return new WhatsAppConversationResult(
                session.getTelefono(),
                "Ese horario acaba de ocuparse.\nTe muestro opciones actualizadas para "
                        + session.getDiaSeleccionado().toLowerCase()
                        + " por la " + session.getFranjaSeleccionada().toLowerCase()
                        + ":\n\n"
                        + buildHorariosMenu(horariosActualizados)
                        + "\n\nRespondé con el número de opción.\n\n0. Empezar de nuevo",
                session.getStep()
        );
    }

    private WhatsAppConversationResult cancelarConversacion(WhatsAppConversationSession session, String mensaje) {
        sessions.remove(session.getTelefono());
        return new WhatsAppConversationResult(session.getTelefono(), mensaje, null);
    }

    private String buildMenuPrincipal(WhatsAppConversationSession session) {
        StringBuilder builder = new StringBuilder("Hola, ")
                .append(session.getNombre())
                .append(".\n");

        if (session.getTurnosParaCancelar() != null && !session.getTurnosParaCancelar().isEmpty()) {
            TurnoResponse proximoTurno = session.getTurnosParaCancelar().get(0);
            builder.append("Tu próximo turno es el ")
                    .append(formatFecha(proximoTurno.fechaHoraInicio()))
                    .append(" a las ")
                    .append(formatHora(proximoTurno.fechaHoraInicio()))
                    .append(".\n\n");
        }

        builder.append(buildMenuPrincipalOptions(session));
        return builder.toString();
    }

    private String buildMenuPrincipalOptions(WhatsAppConversationSession session) {
        if (session.getTurnosParaCancelar() != null && !session.getTurnosParaCancelar().isEmpty()) {
            return """
                    ¿Qué querés hacer?

                    1. Modificar turno
                    2. Cancelar turno
                    3. Reservar otro turno
                    0. Empezar de nuevo
                    """;
        }

        return """
                ¿Qué querés hacer?

                1. Reservar turno
                0. Empezar de nuevo
                """;
    }

    private String buildHorariosMenu(List<LocalDateTime> horarios) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < horarios.size(); i++) {
            builder.append(i + 1).append(". ").append(formatHora(horarios.get(i)));
            if (i < horarios.size() - 1) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private String franjaAlternativa(String franjaActual) {
        return "Mañana".equalsIgnoreCase(franjaActual) ? "Tarde" : "Mañana";
    }

    private String formatHora(LocalDateTime fechaHora) {
        return fechaHora.format(HORA_FORMATTER);
    }

    private String formatFecha(LocalDateTime fechaHora) {
        return fechaHora.toLocalDate().toString();
    }

    private LocalDate resolveFecha(String option) {
        LocalDate hoy = LocalDate.now();
        return switch (option) {
            case "1" -> hoy;
            case "2" -> hoy.plusDays(1);
            case "3" -> hoy.plusDays(2);
            default -> hoy;
        };
    }

    private Integer parsePositiveInteger(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private boolean isRestartRequest(String texto) {
        if (texto == null) {
            return false;
        }

        String normalized = texto.toLowerCase();
        return normalized.contains("empezar de nuevo")
                || normalized.contains("reiniciar")
                || normalized.contains("arrancar de nuevo");
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    private String normalizeSpaces(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }

    private String capitalizeWords(String value) {
        String[] parts = normalizeSpaces(value).toLowerCase().split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            builder.append(part, 0, 1).append(part.substring(1));
            if (i < parts.length - 1) {
                builder.append(" ");
            }
        }
        return capitalizeFirstLetters(builder.toString());
    }

    private String capitalizeFirstLetters(String value) {
        String[] parts = value.split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            builder.append(part.substring(0, 1).toUpperCase()).append(part.substring(1));
            if (i < parts.length - 1) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }
}
