package pe.edu.upeu.lp2_gp2.dto;

import java.math.BigDecimal;

// Vehículo o conductor que podría atender una operación; motivo = null si es elegible
// (motivo calculado por PKG_ASIGNACION.motivo_vehiculo / motivo_conductor)
public record RecursoCandidatoDTO(
        String tipoRecurso,
        Long recursoId,
        Long empresaId,
        String empresa,
        String descripcion,
        Integer capacidadPax,
        BigDecimal horasDia,
        String motivo
) {
}
