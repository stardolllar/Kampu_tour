package pe.edu.upeu.lp2_gp2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LugarRequestDTO(
        @NotBlank(message = "El nombre del lugar es obligatorio.")
        @Size(max = 150, message = "El nombre admite máximo 150 caracteres.")
        @Pattern(regexp = Reglas.TEXTO, message = "El nombre del lugar contiene caracteres no permitidos.")
        String nombre,
        @NotNull(message = "El tipo de lugar es obligatorio.")
        @Pattern(regexp = "AEROPUERTO|TERMINAL|HOTEL|ATRACTIVO|CIUDAD|OTRO", message = "Tipo de lugar no válido.")
        String tipoLugar,
        @NotBlank(message = "La ciudad es obligatoria.")
        @Size(max = 100, message = "La ciudad admite máximo 100 caracteres.")
        @Pattern(regexp = Reglas.TEXTO, message = "La ciudad contiene caracteres no permitidos.")
        String ciudad
) {
}
