package com.santi.turnero.whatsapp.repository;

import com.santi.turnero.whatsapp.domain.WhatsAppConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WhatsAppConversationRepository extends JpaRepository<WhatsAppConversation, Long> {

    Optional<WhatsAppConversation> findFirstByTelefonoAndActivaTrueOrderByUpdatedAtDesc(String telefono);

    Optional<WhatsAppConversation> findFirstByTelefonoOrderByUpdatedAtDesc(String telefono);

    List<WhatsAppConversation> findAllByOrderByUpdatedAtDesc();

    List<WhatsAppConversation> findAllByActivaOrderByUpdatedAtDesc(boolean activa);

    List<WhatsAppConversation> findAllByTelefonoOrderByUpdatedAtDesc(String telefono);

    List<WhatsAppConversation> findAllByTelefonoAndActivaOrderByUpdatedAtDesc(String telefono, boolean activa);
}
