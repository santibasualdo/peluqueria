package com.santi.turnero.servicio.domain;

import com.santi.turnero.peluqueria.domain.Peluqueria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "servicios")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "peluqueria_id", nullable = false)
    private Peluqueria peluqueria;

    @Builder.Default
    @OneToMany(mappedBy = "servicio")
    private List<com.santi.turnero.turno.domain.Turno> turnos = new ArrayList<>();
}
