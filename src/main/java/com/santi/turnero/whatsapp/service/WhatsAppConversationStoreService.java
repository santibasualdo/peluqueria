package com.santi.turnero.whatsapp.service;

import com.santi.turnero.cliente.domain.Cliente;
import com.santi.turnero.cliente.repository.ClienteRepository;
import com.santi.turnero.whatsapp.domain.WhatsAppConversation;
import com.santi.turnero.whatsapp.domain.WhatsAppMessageDirection;
import com.santi.turnero.whatsapp.domain.WhatsAppMessageLog;
import com.santi.turnero.whatsapp.model.WhatsAppConversationSession;
import com.santi.turnero.whatsapp.repository.WhatsAppConversationRepository;
import com.santi.turnero.whatsapp.repository.WhatsAppMessageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WhatsAppConversationStoreService {

    private final WhatsAppConversationRepository conversationRepository;
    private final WhatsAppMessageLogRepository messageLogRepository;
    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public Optional<WhatsAppConversationSession> findActiveByTelefono(String telefono) {
        return conversationRepository.findFirstByTelefonoAndActivaTrueOrderByUpdatedAtDesc(telefono)
                .map(this::toSession);
    }

    @Transactional
    public WhatsAppConversationSession saveActive(WhatsAppConversationSession session) {
        LocalDateTime now = LocalDateTime.now();

        WhatsAppConversation entity = session.getId() != null
                ? conversationRepository.findById(session.getId()).orElseGet(WhatsAppConversation::new)
                : conversationRepository.findFirstByTelefonoAndActivaTrueOrderByUpdatedAtDesc(session.getTelefono())
                        .orElseGet(WhatsAppConversation::new);

        if (entity.getId() == null) {
            entity.setCreatedAt(now);
        }

        entity.setTelefono(session.getTelefono());
        entity.setCliente(resolveCliente(session.getClienteId()));
        entity.setNombre(session.getNombre());
        entity.setApellido(session.getApellido());
        entity.setDiaSeleccionado(session.getDiaSeleccionado());
        entity.setFranjaSeleccionada(session.getFranjaSeleccionada());
        entity.setFechaSeleccionada(session.getFechaSeleccionada());
        entity.setHorarioSeleccionado(session.getHorarioSeleccionado());
        entity.setTurnoSeleccionadoId(session.getTurnoSeleccionadoId());
        entity.setReprogramando(session.isReprogramando());
        entity.setStep(session.getStep());
        entity.setActiva(true);
        entity.setUpdatedAt(now);
        entity.setLastInteractionAt(now);

        WhatsAppConversation saved = conversationRepository.save(entity);
        return toSession(saved, session);
    }

    @Transactional
    public Long finalizeConversation(WhatsAppConversationSession session) {
        if (session == null) {
            return null;
        }

        WhatsAppConversation entity = null;
        if (session.getId() != null) {
            entity = conversationRepository.findById(session.getId()).orElse(null);
        }

        if (entity == null && session.getTelefono() != null) {
            entity = conversationRepository.findFirstByTelefonoAndActivaTrueOrderByUpdatedAtDesc(session.getTelefono())
                    .orElse(null);
        }

        if (entity == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        entity.setActiva(false);
        entity.setUpdatedAt(now);
        entity.setLastInteractionAt(now);
        return conversationRepository.save(entity).getId();
    }

    @Transactional(readOnly = true)
    public Long findLatestConversationIdByTelefono(String telefono) {
        return conversationRepository.findFirstByTelefonoOrderByUpdatedAtDesc(telefono)
                .map(WhatsAppConversation::getId)
                .orElse(null);
    }

    @Transactional
    public void registrarMensaje(Long conversationId, String telefono, WhatsAppMessageDirection direction, String mensaje) {
        if (telefono == null || mensaje == null || mensaje.isBlank()) {
            return;
        }

        messageLogRepository.save(WhatsAppMessageLog.builder()
                .conversation(resolveConversation(conversationId))
                .telefono(telefono)
                .direction(direction)
                .mensaje(mensaje)
                .createdAt(LocalDateTime.now())
                .build());
    }

    private Cliente resolveCliente(Long clienteId) {
        if (clienteId == null) {
            return null;
        }
        return clienteRepository.findById(clienteId).orElse(null);
    }

    private WhatsAppConversation resolveConversation(Long conversationId) {
        if (conversationId == null) {
            return null;
        }
        return conversationRepository.findById(conversationId).orElse(null);
    }

    private WhatsAppConversationSession toSession(WhatsAppConversation entity) {
        return toSession(entity, null);
    }

    private WhatsAppConversationSession toSession(WhatsAppConversation entity, WhatsAppConversationSession currentState) {
        return WhatsAppConversationSession.builder()
                .id(entity.getId())
                .telefono(entity.getTelefono())
                .clienteId(entity.getCliente() != null ? entity.getCliente().getId() : null)
                .nombre(entity.getNombre())
                .apellido(entity.getApellido())
                .diaSeleccionado(entity.getDiaSeleccionado())
                .franjaSeleccionada(entity.getFranjaSeleccionada())
                .fechaSeleccionada(entity.getFechaSeleccionada())
                .horarioSeleccionado(entity.getHorarioSeleccionado())
                .lastInteractionAt(entity.getLastInteractionAt())
                .turnoSeleccionadoId(entity.getTurnoSeleccionadoId())
                .reprogramando(entity.isReprogramando())
                .step(entity.getStep())
                .horariosOfrecidos(currentState != null ? currentState.getHorariosOfrecidos() : null)
                .turnosParaCancelar(currentState != null ? currentState.getTurnosParaCancelar() : null)
                .build();
    }
}
