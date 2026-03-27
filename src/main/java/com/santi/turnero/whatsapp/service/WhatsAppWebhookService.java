package com.santi.turnero.whatsapp.service;

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

    public void procesarWebhook(WhatsAppWebhookPayload payload) {
        List<IncomingWhatsAppMessage> messages = whatsAppMessageExtractor.extractMessages(payload);

        if (messages.isEmpty()) {
            log.info("Webhook de WhatsApp recibido sin entradas.");
            return;
        }

        for (IncomingWhatsAppMessage message : messages) {
            log.info(
                    "Mensaje de WhatsApp recibido. telefono={}, nombreContacto={}, texto={}",
                    message.telefono(),
                    message.nombreContacto(),
                    message.texto()
            );

            WhatsAppConversationResult conversationResult = whatsAppConversationService.procesar(message);
            log.info(
                    "Bot de WhatsApp responderia. telefono={}, step={}, mensaje={}",
                    conversationResult.telefono(),
                    conversationResult.step(),
                    conversationResult.mensajeBot()
            );
        }
    }
}
