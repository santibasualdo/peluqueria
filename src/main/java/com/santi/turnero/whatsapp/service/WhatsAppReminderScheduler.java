package com.santi.turnero.whatsapp.service;

import com.santi.turnero.shared.util.SensitiveDataMasker;
import com.santi.turnero.turno.dto.TurnoInternoResponse;
import com.santi.turnero.turno.service.TurnoService;
import com.santi.turnero.whatsapp.domain.WhatsAppMessageDirection;
import com.santi.turnero.whatsapp.model.WhatsAppConversationSession;
import com.santi.turnero.whatsapp.model.WhatsAppConversationStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class WhatsAppReminderScheduler {

    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final TurnoService turnoService;
    private final WhatsAppConversationStoreService conversationStoreService;
    private final WhatsAppCloudApiService whatsAppCloudApiService;
    private final Integer reminderLeadMinutes;
    private final boolean enabled;

    public WhatsAppReminderScheduler(
            TurnoService turnoService,
            WhatsAppConversationStoreService conversationStoreService,
            WhatsAppCloudApiService whatsAppCloudApiService,
            @Value("${whatsapp.reminders.lead-minutes:180}") Integer reminderLeadMinutes,
            @Value("${whatsapp.reminders.enabled:true}") boolean enabled
    ) {
        this.turnoService = turnoService;
        this.conversationStoreService = conversationStoreService;
        this.whatsAppCloudApiService = whatsAppCloudApiService;
        this.reminderLeadMinutes = reminderLeadMinutes;
        this.enabled = enabled;
    }

    @Scheduled(fixedDelayString = "${whatsapp.reminders.fixed-delay-ms:300000}")
    public void enviarRecordatoriosPendientes() {
        if (!enabled) {
            return;
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusMinutes(reminderLeadMinutes);

        List<TurnoInternoResponse> pendientes = turnoService.listarPendientesDeRecordatorio(ahora, limite);
        if (pendientes.isEmpty()) {
            return;
        }

        for (TurnoInternoResponse turno : pendientes) {
            procesarRecordatorio(turno);
        }
    }

    private void procesarRecordatorio(TurnoInternoResponse turno) {
        if (conversationStoreService.findActiveByTelefono(turno.clienteTelefono()).isPresent()) {
            log.info("No se envio recordatorio porque el cliente ya tiene una conversacion activa. telefono={}, turnoId={}",
                    SensitiveDataMasker.maskPhone(turno.clienteTelefono()), turno.id());
            return;
        }

        String mensaje = buildReminderMessage(turno);
        WhatsAppConversationSession session = WhatsAppConversationSession.builder()
                .telefono(turno.clienteTelefono())
                .clienteId(turno.clienteId())
                .nombre(turno.clienteNombre())
                .turnoSeleccionadoId(turno.id())
                .reprogramando(false)
                .step(WhatsAppConversationStep.PENDIENTE_RESPUESTA_RECORDATORIO)
                .build();

        WhatsAppConversationSession persisted = conversationStoreService.saveActive(session);
        boolean enviado = whatsAppCloudApiService.enviarTexto(turno.clienteTelefono(), mensaje);

        if (!enviado) {
            conversationStoreService.finalizeConversation(persisted);
            return;
        }

        turnoService.marcarRecordatorioEnviado(turno.id());
        conversationStoreService.registrarMensaje(
                persisted.getId(),
                turno.clienteTelefono(),
                WhatsAppMessageDirection.SALIENTE,
                mensaje
        );

        log.info("Recordatorio de WhatsApp enviado. telefono={}, turnoId={}", SensitiveDataMasker.maskPhone(turno.clienteTelefono()), turno.id());
    }

    private String buildReminderMessage(TurnoInternoResponse turno) {
        return "Hola, " + turno.clienteNombre() + ".\n"
                + "Te recordamos tu turno para "
                + turno.fechaHoraInicio().toLocalDate()
                + " a las " + turno.fechaHoraInicio().format(HORA_FORMATTER)
                + ".\n\n"
                + "¿Qué querés hacer?\n\n"
                + "1. Sí, voy a ir\n"
                + "2. Reprogramar turno\n"
                + "3. Cancelar turno\n"
                + "0. Empezar de nuevo";
    }
}
