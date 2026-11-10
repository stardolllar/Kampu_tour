package pe.edu.upeu.lp2_gp2.itinerario.dto;

import pe.edu.upeu.lp2_gp2.itinerario.entity.EstadoSegmento;
import pe.edu.upeu.lp2_gp2.itinerario.entity.SegmentoOperativo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SegmentoDTO(
        Long segmentoId, Long reservaId, String tipo, LocalDate fecha, LocalDateTime horaInicio,
        Long origenId, String origen, Long destinoId, String destino, Integer pax,
        String estado, String siguienteEstado, int paso, int pasos, boolean choferEnCamino,
        String grupoCodigo, String placa, String operador, String vuelo, LocalDateTime estadoActualizadoEn
) {
    public static SegmentoDTO desde(SegmentoOperativo s) {
        EstadoSegmento sig = s.getTipo().siguiente(s.getEstado());
        return new SegmentoDTO(s.getSegmentoId(), s.getReservaMaster().getReservaId(), s.getTipo().name(), s.getFecha(),
                s.getHoraInicio(), s.getOrigen().getLugarId(), s.getOrigen().getNombre(), s.getDestino().getLugarId(),
                s.getDestino().getNombre(), s.getPax(), s.getEstado().name(), sig == null ? null : sig.name(),
                s.getTipo().orden(s.getEstado()), s.getTipo().flujo().size() - 1, s.getEstado().choferEnCamino(),
                s.getGrupoCodigo(), s.getVehiculo() == null ? null : s.getVehiculo().getPlaca(),
                s.getOperador() == null ? null : s.getOperador().getUsername(),
                s.getBoleto() == null ? null : s.getBoleto().vuelo(), s.getEstadoActualizadoEn());
    }
}
