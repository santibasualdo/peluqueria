package com.santi.turnero.whatsapp.service;

import com.santi.turnero.shared.util.SensitiveDataMasker;
import com.santi.turnero.whatsapp.dto.WhatsAppTextMessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
public class WhatsAppCloudApiService {

    private final RestClient restClient;
    private final String phoneNumberId;
    private final String accessToken;

    public WhatsAppCloudApiService(
            @Value("${whatsapp.cloud-api.base-url:https://graph.facebook.com/v22.0}") String baseUrl,
            @Value("${whatsapp.cloud-api.phone-number-id:}") String phoneNumberId,
            @Value("${whatsapp.cloud-api.access-token:}") String accessToken
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.phoneNumberId = normalize(phoneNumberId);
        this.accessToken = normalize(accessToken);
    }

    public boolean enviarTexto(String telefono, String mensaje) {
        String telefonoNormalizado = normalizePhoneForCloudApi(telefono);
        String mensajeNormalizado = normalize(mensaje);

        if (telefonoNormalizado == null || mensajeNormalizado == null) {
            log.warn("No se envio mensaje de WhatsApp porque faltan telefono o mensaje.");
            return false;
        }

        if (phoneNumberId == null || accessToken == null) {
            log.warn("No se envio mensaje de WhatsApp porque faltan whatsapp.cloud-api.phone-number-id o whatsapp.cloud-api.access-token.");
            return false;
        }

        try {
            log.info("Enviando mensaje saliente de WhatsApp. telefono={}", SensitiveDataMasker.maskPhone(telefonoNormalizado));

            restClient.post()
                    .uri("/{phoneNumberId}/messages", phoneNumberId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(WhatsAppTextMessageRequest.of(telefonoNormalizado, mensajeNormalizado))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Mensaje saliente de WhatsApp enviado. telefono={}", SensitiveDataMasker.maskPhone(telefonoNormalizado));
            return true;
        } catch (RestClientException exception) {
            log.error("Error enviando mensaje saliente de WhatsApp. telefono={}", SensitiveDataMasker.maskPhone(telefonoNormalizado), exception);
            return false;
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String normalizePhoneForCloudApi(String telefono) {
        String normalized = normalize(telefono);
        if (normalized == null) {
            return null;
        }

        String digitsOnly = normalized.replaceAll("\\D", "");

        // En el sandbox de WhatsApp Cloud API para Argentina, los destinatarios de prueba
        // suelen estar autorizados sin el 9 que aparece en el webhook entrante.
        if (digitsOnly.startsWith("549") && digitsOnly.length() > 3) {
            return "54" + digitsOnly.substring(3);
        }

        return digitsOnly;
    }
}
