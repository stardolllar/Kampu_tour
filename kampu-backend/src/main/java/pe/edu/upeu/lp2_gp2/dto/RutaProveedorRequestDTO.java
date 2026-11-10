package pe.edu.upeu.lp2_gp2.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RutaProveedorRequestDTO(
        @NotNull(message = "Selecciona la empresa.")
        Long empresaId,
        @NotNull(message = "Selecciona el origen.")
        Long origenId,
        // Destino: un lugar, o el lugar de un paquete (paqueteId). Si el paquete aún no tiene
        // lugar de destino, se le asigna este.
        Long destinoId,
        Long paqueteId,
        @NotNull(message = "El tipo de servicio es obligatorio.")
        @Pattern(regexp = "TRASLADO|TOUR|INTERPROVINCIAL", message = "Tipo de servicio no válido.")
        String tipoServicio,
        // Vacía: se calcula con datos reales (OSRM / coordenadas de los lugares)
        @Min(value = 1, message = "La duración debe estar entre 1 y 9999 minutos.")
        @Max(value = 9999, message = "La duración debe estar entre 1 y 9999 minutos.")
        Integer duracionEstimadaMin,
        @DecimalMin(value = "0", message = "La tarifa no puede ser negativa.")
        @Digits(integer = 8, fraction = 2, message = "La tarifa admite 8 enteros y 2 decimales.")
        BigDecimal tarifaReferencial,
        @NotNull(message = "El estado es obligatorio.")
        @Pattern(regexp = "ACTIVA|INACTIVA", message = "El estado debe ser ACTIVA o INACTIVA.")
        String estado,
        // Salida programada que se crea automáticamente al registrar una ruta hacia un paquete
        LocalDate fechaSalida,
        @Min(value = 1, message = "Los cupos de la salida deben estar entre 1 y 999.")
        @Max(value = 999, message = "Los cupos de la salida deben estar entre 1 y 999.")
        Integer cuposSalida,
        Long guiaSalidaId
) {
}
