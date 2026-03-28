package com.santi.turnero.whatsapp.service;

import com.santi.turnero.whatsapp.domain.WhatsAppProcessedMessage;
import com.santi.turnero.whatsapp.repository.WhatsAppProcessedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppWebhookIdempotencyService {

    private final WhatsAppProcessedMessageRepository processedMessageRepository;

    @Transactional
    public boolean registrarSiEsNuevo(String messageId, String telefono) {
        if (messageId == null || messageId.isBlank()) {
            log.warn("Webhook de WhatsApp recibido sin messageId; no se puede deduplicar correctamente.");
            return true;
        }

        try {
            processedMessageRepository.saveAndFlush(WhatsAppProcessedMessage.builder()
                    .messageId(messageId.trim())
                    .telefono(telefono != null ? telefono.trim() : "desconocido")
                    .processedAt(LocalDateTime.now())
                    .build());
            return true;
        } catch (DataIntegrityViolationException exception) {
            return false;
        }
    }
}
