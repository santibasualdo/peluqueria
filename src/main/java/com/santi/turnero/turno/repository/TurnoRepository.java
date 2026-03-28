package com.santi.turnero.turno.repository;

import com.santi.turnero.shared.dto.RangoHorarioDto;
import com.santi.turnero.turno.domain.Turno;
import com.santi.turnero.turno.domain.TurnoEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    List<Turno> findByPeluqueriaIdAndFechaHoraInicioGreaterThanEqualAndFechaHoraInicioLessThanOrderByFechaHoraInicioAsc(
            Long peluqueriaId,
            LocalDateTime desde,
            LocalDateTime hasta
    );

    List<Turno> findByPeluqueroIdAndFechaHoraInicioGreaterThanEqualAndFechaHoraInicioLessThanOrderByFechaHoraInicioAsc(
            Long peluqueroId,
            LocalDateTime desde,
            LocalDateTime hasta
    );

    @Query("""
            select t
            from Turno t
            where t.peluquero.id = :peluqueroId
              and t.estado in :estados
              and t.fechaHoraInicio < :hasta
              and t.fechaHoraFin > :desde
            order by t.fechaHoraInicio asc
            """)
    List<Turno> findOcupadosEnRango(
            @Param("peluqueroId") Long peluqueroId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("estados") Collection<TurnoEstado> estados
    );

    @Query("""
            select new com.santi.turnero.shared.dto.RangoHorarioDto(
                t.fechaHoraInicio,
                t.fechaHoraFin
            )
            from Turno t
            where t.peluquero.id = :peluqueroId
              and t.estado in :estados
              and t.fechaHoraInicio < :hasta
              and t.fechaHoraFin > :desde
            order by t.fechaHoraInicio asc
            """)
    List<RangoHorarioDto> findRangosOcupadosEnRango(
            @Param("peluqueroId") Long peluqueroId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("estados") Collection<TurnoEstado> estados
    );

    @Query("""
            select count(t) > 0
            from Turno t
            where t.peluquero.id = :peluqueroId
              and t.estado in :estados
              and t.fechaHoraInicio < :fechaHoraFin
              and t.fechaHoraFin > :fechaHoraInicio
              and (:turnoIdIgnorado is null or t.id <> :turnoIdIgnorado)
            """)
    boolean existsSolapamiento(
            @Param("peluqueroId") Long peluqueroId,
            @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
            @Param("fechaHoraFin") LocalDateTime fechaHoraFin,
            @Param("estados") Collection<TurnoEstado> estados,
            @Param("turnoIdIgnorado") Long turnoIdIgnorado
    );

    @Query("""
            select t
            from Turno t
            where t.peluqueria.id = :peluqueriaId
              and t.cliente.id = :clienteId
              and t.estado in :estados
              and t.fechaHoraInicio >= :desde
            order by t.fechaHoraInicio asc
            """)
    List<Turno> findProximosDelCliente(
            @Param("peluqueriaId") Long peluqueriaId,
            @Param("clienteId") Long clienteId,
            @Param("desde") LocalDateTime desde,
            @Param("estados") Collection<TurnoEstado> estados
    );

    @Query("""
            select t
            from Turno t
            join fetch t.cliente c
            join fetch t.peluqueria p
            where t.estado = com.santi.turnero.turno.domain.TurnoEstado.RESERVADO
              and t.recordatorioEnviadoAt is null
              and t.fechaHoraInicio >= :desde
              and t.fechaHoraInicio <= :hasta
            order by t.fechaHoraInicio asc
            """)
    List<Turno> findPendientesDeRecordatorio(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );
}
