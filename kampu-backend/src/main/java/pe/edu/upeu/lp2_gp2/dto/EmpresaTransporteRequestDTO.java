package pe.edu.upeu.lp2_gp2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EmpresaTransporteRequestDTO(
        @NotBlank(message = "El nombre comercial es obligatorio.")
        @Size(max = 150, message = "El nombre comercial admite máximo 150 caracteres.")
        @Pattern(regexp = Reglas.TEXTO, message = "El nombre comercial contiene caracteres no permitidos.")
        String nombreComercial,
        @NotBlank(message = "El RUC es obligatorio.")
        @Pattern(regexp = Reglas.RUC, message = Reglas.MSG_RUC)
        String ruc,
        @Size(max = 150, message = "El nombre del contacto admite máximo 150 caracteres.")
        @Pattern(regexp = Reglas.NOMBRE_PERSONA, message = "El nombre del contacto " + Reglas.MSG_NOMBRE_PERSONA)
        String contactoNombre,
        @NotBlank(message = "El teléfono de contacto es obligatorio.")
        @Size(max = 20, message = "El teléfono admite máximo 20 caracteres.")
        @Pattern(regexp = Reglas.TELEFONO, message = Reglas.MSG_TELEFONO)
        String contactoTelefono,
        @Size(max = 100, message = "El correo admite máximo 100 caracteres.")
        @Pattern(regexp = Reglas.EMAIL, message = "El correo de contacto no es válido.")
        String contactoEmail,
        @Size(max = 250, message = "La dirección admite máximo 250 caracteres.")
        @Pattern(regexp = Reglas.TEXTO, message = "La dirección contiene caracteres no permitidos.")
        String direccion,
        @NotNull(message = "El estado de afiliación es obligatorio.")
        @Pattern(regexp = "PENDIENTE|ACTIVA|SUSPENDIDA|RETIRADA", message = "Estado de afiliación no válido.")
        String estadoAfiliacion,
        @NotNull(message = "La disponibilidad operativa es obligatoria.")
        @Pattern(regexp = "DISPONIBLE|LIMITADA|NO_DISPONIBLE", message = "Disponibilidad operativa no válida.")
        String disponibilidadOperativa,
        LocalDate fechaAfiliacion
) {
}
