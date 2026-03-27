package com.santi.turnero.peluqueria.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "peluquerias")
public class Peluqueria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 255)
    private String direccion;

    @Column(nullable = false, length = 30)
    private String telefono;

    @Column(name = "hora_apertura", nullable = false)
    private LocalTime horaApertura;

    @Column(name = "hora_cierre", nullable = false)
    private LocalTime horaCierre;

    @Builder.Default
    @OneToMany(mappedBy = "peluqueria")
    private List<com.santi.turnero.peluquero.domain.Peluquero> peluqueros = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "peluqueria")
    private List<com.santi.turnero.servicio.domain.Servicio> servicios = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "peluqueria")
    private List<com.santi.turnero.turno.domain.Turno> turnos = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "peluqueria")
    private List<com.santi.turnero.bloqueo.domain.BloqueoHorario> bloqueos = new ArrayList<>();
}
