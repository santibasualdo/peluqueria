package com.santi.turnero.whatsapp.repository;

import com.santi.turnero.whatsapp.domain.WhatsAppMessageLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WhatsAppMessageLogRepository extends JpaRepository<WhatsAppMessageLog, Long> {

    List<WhatsAppMessageLog> findAllByConversationIdOrderByCreatedAtAsc(Long conversationId);
}
