package com.santi.turnero.whatsapp.domain;

import com.santi.turnero.cliente.domain.Cliente;
import com.santi.turnero.whatsapp.model.WhatsAppConversationStep;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "whatsapp_conversations")
public class WhatsAppConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String telefono;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(length = 120)
    private String nombre;

    @Column(length = 120)
    private String apellido;

    @Column(name = "dia_seleccionado", length = 30)
    private String diaSeleccionado;

    @Column(name = "franja_seleccionada", length = 30)
    private String franjaSeleccionada;

    @Column(name = "fecha_seleccionada")
    private LocalDate fechaSeleccionada;

    @Column(name = "horario_seleccionado")
    private LocalDateTime horarioSeleccionado;

    @Column(name = "turno_seleccionado_id")
    private Long turnoSeleccionadoId;

    @Column(name = "reprogramando", nullable = false)
    private boolean reprogramando;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private WhatsAppConversationStep step;

    @Column(name = "activa", nullable = false)
    private boolean activa;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_interaction_at", nullable = false)
    private LocalDateTime lastInteractionAt;
}
