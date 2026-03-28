package com.santi.turnero.whatsapp.service;

import com.santi.turnero.whatsapp.domain.WhatsAppConversation;
import com.santi.turnero.whatsapp.domain.WhatsAppMessageLog;
import com.santi.turnero.whatsapp.dto.WhatsAppConversationTraceResponse;
import com.santi.turnero.whatsapp.dto.WhatsAppMessageTraceResponse;
import com.santi.turnero.whatsapp.repository.WhatsAppConversationRepository;
import com.santi.turnero.whatsapp.repository.WhatsAppMessageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WhatsAppTraceabilityService {

    private final WhatsAppConversationRepository conversationRepository;
    private final WhatsAppMessageLogRepository messageLogRepository;

    @Transactional(readOnly = true)
    public List<WhatsAppConversationTraceResponse> listarConversaciones(String telefono, Boolean soloActivas) {
        String telefonoNormalizado = normalize(telefono);

        List<WhatsAppConversation> conversaciones;
        if (telefonoNormalizado != null && soloActivas != null) {
            conversaciones = conversationRepository.findAllByTelefonoAndActivaOrderByUpdatedAtDesc(
                    telefonoNormalizado,
                    soloActivas
            );
        } else if (telefonoNormalizado != null) {
            conversaciones = conversationRepository.findAllByTelefonoOrderByUpdatedAtDesc(telefonoNormalizado);
        } else if (soloActivas != null) {
            conversaciones = conversationRepository.findAllByActivaOrderByUpdatedAtDesc(soloActivas);
        } else {
            conversaciones = conversationRepository.findAllByOrderByUpdatedAtDesc();
        }

        return conversaciones.stream()
                .map(this::toConversationTraceResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WhatsAppMessageTraceResponse> listarMensajes(Long conversationId) {
        return messageLogRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(this::toMessageTraceResponse)
                .toList();
    }

    private WhatsAppConversationTraceResponse toConversationTraceResponse(WhatsAppConversation conversation) {
        String nombreCompleto = buildNombreCompleto(conversation.getNombre(), conversation.getApellido());
        return new WhatsAppConversationTraceResponse(
                conversation.getId(),
                conversation.getTelefono(),
                conversation.getCliente() != null ? conversation.getCliente().getId() : null,
                conversation.getCliente() != null ? conversation.getCliente().getNombre() : null,
                nombreCompleto,
                conversation.isActiva(),
                conversation.getStep(),
                conversation.getDiaSeleccionado(),
                conversation.getFranjaSeleccionada(),
                conversation.getFechaSeleccionada(),
                conversation.getHorarioSeleccionado(),
                conversation.getTurnoSeleccionadoId(),
                conversation.isReprogramando(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                conversation.getLastInteractionAt()
        );
    }

    private WhatsAppMessageTraceResponse toMessageTraceResponse(WhatsAppMessageLog messageLog) {
        return new WhatsAppMessageTraceResponse(
                messageLog.getId(),
                messageLog.getConversation() != null ? messageLog.getConversation().getId() : null,
                messageLog.getTelefono(),
                messageLog.getDirection(),
                messageLog.getMensaje(),
                messageLog.getCreatedAt()
        );
    }

    private String buildNombreCompleto(String nombre, String apellido) {
        if (nombre == null && apellido == null) {
            return null;
        }
        if (apellido == null || apellido.isBlank()) {
            return nombre;
        }
        if (nombre == null || nombre.isBlank()) {
            return apellido;
        }
        return nombre + " " + apellido;
    }

    private String normalize(String telefono) {
        if (telefono == null) {
            return null;
        }

        String trimmed = telefono.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
