package com.santi.turnero.whatsapp.dto;

import com.santi.turnero.whatsapp.model.WhatsAppConversationStep;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record WhatsAppConversationTraceResponse(
        Long id,
        String telefono,
        Long clienteId,
        String clienteNombre,
        String nombreCompleto,
        boolean activa,
        WhatsAppConversationStep step,
        String diaSeleccionado,
        String franjaSeleccionada,
        LocalDate fechaSeleccionada,
        LocalDateTime horarioSeleccionado,
        Long turnoSeleccionadoId,
        boolean reprogramando,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastInteractionAt
) {
}
