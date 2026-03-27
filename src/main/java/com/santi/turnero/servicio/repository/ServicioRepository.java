package com.santi.turnero.servicio.repository;

import com.santi.turnero.servicio.domain.Servicio;
import com.santi.turnero.turno.dto.DisponibilidadContexto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    List<Servicio> findByPeluqueriaIdOrderByNombreAsc(Long peluqueriaId);

    @Query("""
            select new com.santi.turnero.turno.dto.DisponibilidadContexto(
                pel.id,
                p.id,
                s.id,
                pel.horaApertura,
                pel.horaCierre,
                s.duracionMinutos
            )
            from Peluquero p
            join p.peluqueria pel
            join Servicio s on s.peluqueria.id = pel.id
            where pel.id = :peluqueriaId
              and p.id = :peluqueroId
              and s.id = :servicioId
            """)
    Optional<DisponibilidadContexto> findDisponibilidadContexto(
            @Param("peluqueriaId") Long peluqueriaId,
            @Param("peluqueroId") Long peluqueroId,
            @Param("servicioId") Long servicioId
    );
}
