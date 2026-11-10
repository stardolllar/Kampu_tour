package pe.edu.upeu.lp2_gp2.dto;

import pe.edu.upeu.lp2_gp2.entity.OperacionTransporte;
import pe.edu.upeu.lp2_gp2.entity.VistaTableroOperacion;

import java.time.LocalDateTime;

// Operación de transporte + datos del tablero (vista VW_TABLERO_OPERACIONES)
public record OperacionDTO(
        Long operacionId,
        String codigoOperacion,
        Long salidaId,
        Long origenId,
        Long destinoId,
        Long empresaId,
        Long vehiculoId,
        Long conductorId,
        Integer paxProgramados,
        LocalDateTime horaProgramadaSalida,
        LocalDateTime horaEstimadaLlegada,
        String estadoActual,
        String observaciones,
        // Desde la vista
        String origen,
        String destino,
        String empresa,
        String placa,
        String conductor,
        String estadoNombre,
        Integer ordenEstado,
        LocalDateTime horaRealSalida,
        LocalDateTime horaRealLlegada,
        Integer minRetrasoSalida,
        Integer minRetrasoLlegada
) {
    public static OperacionDTO desde(OperacionTransporte o, VistaTableroOperacion t) {
        return new OperacionDTO(o.getOperacionId(), o.getCodigoOperacion(), o.getSalidaId(), o.getOrigenId(), o.getDestinoId(),
                o.getEmpresaId(), o.getVehiculoId(), o.getConductorId(), o.getPaxProgramados(), o.getHoraProgramadaSalida(),
                o.getHoraEstimadaLlegada(), o.getEstadoActual(), o.getObservaciones() == null ? "" : o.getObservaciones(),
                t == null ? null : t.getOrigen(), t == null ? null : t.getDestino(), t == null ? null : t.getEmpresa(),
                t == null ? null : t.getPlaca(), t == null ? null : t.getConductor(), t == null ? null : t.getEstado(),
                t == null ? null : t.getOrdenEstado(), t == null ? null : t.getHoraRealSalida(),
                t == null ? null : t.getHoraRealLlegada(), t == null ? null : t.getMinRetrasoSalida(),
                t == null ? null : t.getMinRetrasoLlegada());
    }
}
