package pe.edu.upeu.lp2_gp2.dto;

import java.math.BigDecimal;

// Fila de la vista VW_RECURSOS_DISPONIBLES
public record RecursoDisponibleDTO(
        String tipoRecurso,
        Long recursoId,
        Long empresaId,
        String empresa,
        String descripcion,
        Integer capacidadPax,
        Integer holguraPax,
        BigDecimal horasDia
) {
}
