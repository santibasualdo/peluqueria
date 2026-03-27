package com.santi.turnero.whatsapp.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.santi.turnero.turno.dto.TurnoResponse;

@Getter
@Setter
@Builder
public class WhatsAppConversationSession {

    private String telefono;
    private Long clienteId;
    private String nombre;
    private String apellido;
    private String diaSeleccionado;
    private String franjaSeleccionada;
    private LocalDate fechaSeleccionada;
    private List<LocalDateTime> horariosOfrecidos;
    private LocalDateTime horarioSeleccionado;
    private List<TurnoResponse> turnosParaCancelar;
    private Long turnoSeleccionadoId;
    private boolean reprogramando;
    private WhatsAppConversationStep step;

    public String getNombreCompleto() {
        if (apellido == null || apellido.isBlank()) {
            return nombre;
        }
        return nombre + " " + apellido;
    }
}
