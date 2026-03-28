package com.santi.turnero.whatsapp.repository;

import com.santi.turnero.whatsapp.domain.WhatsAppProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhatsAppProcessedMessageRepository extends JpaRepository<WhatsAppProcessedMessage, Long> {
}
