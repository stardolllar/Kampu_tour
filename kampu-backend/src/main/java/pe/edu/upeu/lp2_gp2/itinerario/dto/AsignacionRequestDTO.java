package pe.edu.upeu.lp2_gp2.itinerario.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

// El Admin consolida segmentos (de una o varias reservas) en un viaje con vehículo y operador
public record AsignacionRequestDTO(
        @NotEmpty(message = "Elige al menos un segmento.") @Size(max = 60, message = "Máximo 60 segmentos por viaje.")
        List<@NotNull Long> segmentoIds,
        @NotNull(message = "Elige el vehículo.") Long vehiculoId,
        @NotNull(message = "Elige el operador (chofer / guía).") Long operadorId
) {
}
