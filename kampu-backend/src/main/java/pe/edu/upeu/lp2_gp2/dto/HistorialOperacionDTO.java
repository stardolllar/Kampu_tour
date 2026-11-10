package pe.edu.upeu.lp2_gp2.dto;

import pe.edu.upeu.lp2_gp2.entity.HistorialTraficoOperacion;

import java.time.LocalDateTime;

public record HistorialOperacionDTO(
        Long historialId,
        Long operacionId,
        String estadoAnterior,
        String estadoNuevo,
        LocalDateTime fechaCambio,
        String usuarioRegistro,
        String observacion
) {
    public static HistorialOperacionDTO desde(HistorialTraficoOperacion h) {
        return new HistorialOperacionDTO(h.getHistorialId(), h.getOperacionId(), h.getEstadoAnterior(), h.getEstadoNuevo(),
                h.getFechaCambio(), h.getUsuarioRegistro(), h.getObservacion() == null ? "" : h.getObservacion());
    }
}
