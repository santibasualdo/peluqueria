package com.santi.turnero.whatsapp.service;

import com.santi.turnero.whatsapp.dto.IncomingWhatsAppMessage;
import com.santi.turnero.whatsapp.dto.WhatsAppChangeDto;
import com.santi.turnero.whatsapp.dto.WhatsAppContactDto;
import com.santi.turnero.whatsapp.dto.WhatsAppEntryDto;
import com.santi.turnero.whatsapp.dto.WhatsAppMessageDto;
import com.santi.turnero.whatsapp.dto.WhatsAppWebhookPayload;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WhatsAppMessageExtractor {

    public List<IncomingWhatsAppMessage> extractMessages(WhatsAppWebhookPayload payload) {
        List<IncomingWhatsAppMessage> result = new ArrayList<>();

        if (payload == null || payload.entry() == null) {
            return result;
        }

        for (WhatsAppEntryDto entry : payload.entry()) {
            if (entry == null || entry.changes() == null) {
                continue;
            }

            for (WhatsAppChangeDto change : entry.changes()) {
                if (change == null || change.value() == null || change.value().messages() == null) {
                    continue;
                }

                for (WhatsAppMessageDto message : change.value().messages()) {
                    if (message == null) {
                        continue;
                    }

                    String messageId = message.id();
                    String telefono = message.from();
                    String texto = message.text() != null ? message.text().body() : null;
                    String nombreContacto = resolveContactName(change, telefono);

                    if (messageId != null || telefono != null || texto != null || nombreContacto != null) {
                        result.add(new IncomingWhatsAppMessage(messageId, telefono, texto, nombreContacto));
                    }
                }
            }
        }

        return result;
    }

    private String resolveContactName(WhatsAppChangeDto change, String telefono) {
        if (change == null || change.value() == null || change.value().contacts() == null) {
            return null;
        }

        for (WhatsAppContactDto contact : change.value().contacts()) {
            if (contact == null) {
                continue;
            }

            boolean sameNumber = telefono == null || telefono.equals(contact.waId());
            if (sameNumber && contact.profile() != null) {
                return contact.profile().name();
            }
        }

        return null;
    }
}
