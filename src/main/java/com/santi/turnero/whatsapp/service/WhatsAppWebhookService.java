package com.santi.turnero.whatsapp.service;

import com.santi.turnero.shared.util.SensitiveDataMasker;
import com.santi.turnero.whatsapp.domain.WhatsAppMessageDirection;
import com.santi.turnero.whatsapp.dto.IncomingWhatsAppMessage;
import com.santi.turnero.whatsapp.dto.WhatsAppWebhookPayload;
import com.santi.turnero.whatsapp.model.WhatsAppConversationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppWebhookService {

    private final WhatsAppMessageExtractor whatsAppMessageExtractor;
    private final WhatsAppConversationService whatsAppConversationService;
    private final WhatsAppCloudApiService whatsAppCloudApiService;
    private final WhatsAppConversationStoreService conversationStoreService;
    private final WhatsAppWebhookIdempotencyService whatsAppWebhookIdempotencyService;

    public void procesarWebhook(WhatsAppWebhookPayload payload) {
        List<IncomingWhatsAppMessage> messages = whatsAppMessageExtractor.extractMessages(payload);

        if (messages.isEmpty()) {
            log.info("Webhook de WhatsApp recibido sin entradas.");
            return;
        }

        for (IncomingWhatsAppMessage message : messages) {
            if (!whatsAppWebhookIdempotencyService.registrarSiEsNuevo(message.messageId(), message.telefono())) {
                log.info(
                        "Webhook de WhatsApp duplicado ignorado. messageId={}, telefono={}",
                        message.messageId(),
                        SensitiveDataMasker.maskPhone(message.telefono())
                );
                continue;
            }

            log.info(
                    "Mensaje de WhatsApp recibido. messageId={}, telefono={}, nombreContacto={}, texto={}",
                    message.messageId(),
                    SensitiveDataMasker.maskPhone(message.telefono()),
                    message.nombreContacto(),
                    SensitiveDataMasker.summarizeText(message.texto())
            );

            WhatsAppConversationResult conversationResult = whatsAppConversationService.procesar(message);
            Long conversationId = conversationResult.conversationId() != null
                    ? conversationResult.conversationId()
                    : conversationStoreService.findLatestConversationIdByTelefono(message.telefono());

            conversationStoreService.registrarMensaje(
                    conversationId,
                    message.telefono(),
                    WhatsAppMessageDirection.ENTRANTE,
                    message.texto()
            );

            log.info(
                    "Bot de WhatsApp responderia. telefono={}, step={}, mensaje={}",
                    SensitiveDataMasker.maskPhone(conversationResult.telefono()),
                    conversationResult.step(),
                    SensitiveDataMasker.summarizeText(conversationResult.mensajeBot())
            );

            whatsAppCloudApiService.enviarTexto(
                    conversationResult.telefono(),
                    conversationResult.mensajeBot()
            );

            conversationStoreService.registrarMensaje(
                    conversationId,
                    conversationResult.telefono(),
                    WhatsAppMessageDirection.SALIENTE,
                    conversationResult.mensajeBot()
            );
        }
    }
}
