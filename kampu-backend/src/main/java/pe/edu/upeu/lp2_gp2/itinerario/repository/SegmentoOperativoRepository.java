package pe.edu.upeu.lp2_gp2.itinerario.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.lp2_gp2.itinerario.dto.SegmentoFidsFila;
import pe.edu.upeu.lp2_gp2.itinerario.entity.SegmentoOperativo;
import pe.edu.upeu.lp2_gp2.itinerario.entity.TipoSegmento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SegmentoOperativoRepository extends JpaRepository<SegmentoOperativo, Long> {

    /**
     * Consolidación cruzada (Cross-Booking) para el panel FIDS: segmentos de TODAS las reservas
     * de una fecha, filtrables por tipo y por lugar (aeropuerto, atractivo u hotel), con el
     * cliente, el vuelo, el vehículo y el operador ya resueltos. Ordenados por hora para que
     * ConsolidacionService arme los grupos por ventana de tiempo en una sola pasada.
     * (Cliente no tiene relación JPA con Reserva: se une por id con JOIN ... ON.)
     */
    @Query("""
            SELECT new pe.edu.upeu.lp2_gp2.itinerario.dto.SegmentoFidsFila(
                   s.segmentoId, s.tipo, s.fecha, s.horaInicio, s.estado, s.pax, s.grupoCodigo,
                   m.reservaId, c.nombreCompleto,
                   o.lugarId, o.nombre, d.lugarId, d.nombre,
                   CONCAT(CONCAT(CAST(b.aerolinea AS string), ' '), b.numeroVuelo), b.codigoPnr,
                   v.vehiculoId, v.placa, v.capacidadPax,
                   u.usuarioId, u.username)
              FROM SegmentoOperativo s
              JOIN s.reservaMaster m
              JOIN m.reserva r
              JOIN Cliente c ON c.clienteId = r.clienteId
              JOIN s.origen o
              JOIN s.destino d
              LEFT JOIN s.boleto b
              LEFT JOIN s.vehiculo v
              LEFT JOIN s.operador u
             WHERE s.fecha = :fecha
               AND s.tipo IN :tipos
               AND (:lugarId IS NULL OR o.lugarId = :lugarId OR d.lugarId = :lugarId)
               AND r.estadoReserva <> 'ANULADA'
             ORDER BY s.tipo, s.horaInicio, s.segmentoId
            """)
    List<SegmentoFidsFila> paraConsolidar(@Param("fecha") LocalDate fecha,
                                          @Param("tipos") Collection<TipoSegmento> tipos,
                                          @Param("lugarId") Long lugarId);

    // Bloqueo de escritura al asignar o avanzar estados (evita asignaciones dobles concurrentes)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SegmentoOperativo s WHERE s.segmentoId IN :ids ORDER BY s.segmentoId")
    List<SegmentoOperativo> bloquear(@Param("ids") Collection<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SegmentoOperativo s WHERE s.grupoCodigo = :grupo ORDER BY s.horaInicio, s.segmentoId")
    List<SegmentoOperativo> bloquearGrupo(@Param("grupo") String grupo);

    List<SegmentoOperativo> findByGrupoCodigoOrderByHoraInicioAscSegmentoIdAsc(String grupoCodigo);

    // Jornada del operador
    @Query("""
            SELECT s FROM SegmentoOperativo s
              JOIN FETCH s.reservaMaster m JOIN FETCH s.origen JOIN FETCH s.destino
              LEFT JOIN FETCH s.vehiculo LEFT JOIN FETCH s.boleto
             WHERE s.operador.usuarioId = :operadorId AND s.fecha = :fecha
             ORDER BY s.horaInicio, s.grupoCodigo, s.segmentoId
            """)
    List<SegmentoOperativo> jornada(@Param("operadorId") Long operadorId, @Param("fecha") LocalDate fecha);

    // ¿El vehículo o el operador ya tienen otro viaje que se cruza en el horario?
    @Query("""
            SELECT COUNT(s) FROM SegmentoOperativo s
             WHERE s.fecha = :fecha AND s.grupoCodigo IS NOT NULL
               AND (:grupo IS NULL OR s.grupoCodigo <> :grupo)
               AND s.horaInicio BETWEEN :desde AND :hasta
               AND (s.vehiculo.vehiculoId = :vehiculoId OR s.operador.usuarioId = :operadorId)
            """)
    long cruces(@Param("fecha") LocalDate fecha, @Param("grupo") String grupo, @Param("desde") LocalDateTime desde,
                @Param("hasta") LocalDateTime hasta, @Param("vehiculoId") Long vehiculoId,
                @Param("operadorId") Long operadorId);

    @Query("""
            SELECT s FROM SegmentoOperativo s
              JOIN FETCH s.origen JOIN FETCH s.destino LEFT JOIN FETCH s.vehiculo LEFT JOIN FETCH s.operador
              LEFT JOIN FETCH s.boleto
             WHERE s.reservaMaster.reservaId = :reservaId
             ORDER BY s.horaInicio, s.segmentoId
            """)
    List<SegmentoOperativo> deReserva(@Param("reservaId") Long reservaId);

    Optional<SegmentoOperativo> findByReservaMasterReservaIdAndTipoAndFecha(Long reservaId, TipoSegmento tipo, LocalDate fecha);

    Optional<SegmentoOperativo> findFirstByReservaMasterReservaIdAndTipo(Long reservaId, TipoSegmento tipo);

    List<SegmentoOperativo> findByReservaMasterReservaIdAndTipo(Long reservaId, TipoSegmento tipo);

    long countByGrupoCodigoStartingWith(String prefijo);
}
