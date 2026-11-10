package pe.edu.upeu.lp2_gp2.itinerario.dto;

import pe.edu.upeu.lp2_gp2.itinerario.entity.EstadoSegmento;
import pe.edu.upeu.lp2_gp2.itinerario.entity.TipoSegmento;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Proyección plana de un segmento para el panel FIDS (una fila por reserva y tarea).
 * La arma la consulta JPQL de SegmentoOperativoRepository.paraConsolidar, sin cargar entidades.
 */
public record SegmentoFidsFila(
        Long segmentoId,
        TipoSegmento tipo,
        LocalDate fecha,
        LocalDateTime horaInicio,
        EstadoSegmento estado,
        Integer pax,
        String grupoCodigo,
        Long reservaId,
        String cliente,
        Long origenId,
        String origen,
        Long destinoId,
        String destino,
        String vuelo,
        String pnr,
        Long vehiculoId,
        String placa,
        Integer capacidad,
        Long operadorId,
        String operador
) {
    /** Lugar que agrupa: el aeropuerto en transfers, el atractivo en tours, el hotel en alojamiento */
    public Long lugarClaveId() {
        return tipo == TipoSegmento.TRANSFER_IN ? origenId : destinoId;
    }

    public String lugarClave() {
        return tipo == TipoSegmento.TRANSFER_IN ? origen : destino;
    }
}
