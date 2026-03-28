package com.santi.turnero.turno.domain;

import com.santi.turnero.cliente.domain.Cliente;
import com.santi.turnero.peluqueria.domain.Peluqueria;
import com.santi.turnero.peluquero.domain.Peluquero;
import com.santi.turnero.servicio.domain.Servicio;
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

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "turnos")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "peluqueria_id", nullable = false)
    private Peluqueria peluqueria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "peluquero_id", nullable = false)
    private Peluquero peluquero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin", nullable = false)
    private LocalDateTime fechaHoraFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TurnoEstado estado;

    @Column(length = 500)
    private String observaciones;

    @Column(name = "recordatorio_enviado_at")
    private LocalDateTime recordatorioEnviadoAt;

    @Column(name = "asistencia_confirmada_at")
    private LocalDateTime asistenciaConfirmadaAt;
}
