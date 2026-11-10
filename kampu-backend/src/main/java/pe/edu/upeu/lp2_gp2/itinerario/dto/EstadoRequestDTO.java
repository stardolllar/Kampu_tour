package pe.edu.upeu.lp2_gp2.itinerario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.upeu.lp2_gp2.itinerario.entity.EstadoSegmento;

// El operador avanza al siguiente estado de la secuencia (se valida contra TipoSegmento.flujo)
public record EstadoRequestDTO(
        @NotNull(message = "Indica el nuevo estado.") EstadoSegmento estado,
        @Size(max = 200, message = "La observación admite máximo 200 caracteres.") String observacion
) {
}
