package com.santi.turnero.bloqueo.repository;

import com.santi.turnero.bloqueo.domain.BloqueoHorario;
import com.santi.turnero.shared.dto.RangoHorarioDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BloqueoHorarioRepository extends JpaRepository<BloqueoHorario, Long> {

    @Query("""
            select count(b) > 0
            from BloqueoHorario b
            where b.peluqueria.id = :peluqueriaId
              and (b.peluquero is null or b.peluquero.id = :peluqueroId)
              and b.fechaHoraInicio < :fechaHoraFin
              and b.fechaHoraFin > :fechaHoraInicio
            """)
    boolean existsBloqueoSolapado(
            @Param("peluqueriaId") Long peluqueriaId,
            @Param("peluqueroId") Long peluqueroId,
            @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
            @Param("fechaHoraFin") LocalDateTime fechaHoraFin
    );

    @Query("""
            select b
            from BloqueoHorario b
            where b.peluqueria.id = :peluqueriaId
              and (b.peluquero is null or b.peluquero.id = :peluqueroId)
              and b.fechaHoraInicio < :hasta
              and b.fechaHoraFin > :desde
            order by b.fechaHoraInicio asc
            """)
    List<BloqueoHorario> findBloqueosEnRango(
            @Param("peluqueriaId") Long peluqueriaId,
            @Param("peluqueroId") Long peluqueroId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );

    @Query("""
            select new com.santi.turnero.shared.dto.RangoHorarioDto(
                b.fechaHoraInicio,
                b.fechaHoraFin
            )
            from BloqueoHorario b
            where b.peluqueria.id = :peluqueriaId
              and (b.peluquero is null or b.peluquero.id = :peluqueroId)
              and b.fechaHoraInicio < :hasta
              and b.fechaHoraFin > :desde
            order by b.fechaHoraInicio asc
            """)
    List<RangoHorarioDto> findRangosBloqueadosEnRango(
            @Param("peluqueriaId") Long peluqueriaId,
            @Param("peluqueroId") Long peluqueroId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );
}
