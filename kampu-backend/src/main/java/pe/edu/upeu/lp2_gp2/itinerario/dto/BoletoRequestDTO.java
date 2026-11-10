package pe.edu.upeu.lp2_gp2.itinerario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import pe.edu.upeu.lp2_gp2.itinerario.entity.Aerolinea;
import pe.edu.upeu.lp2_gp2.itinerario.entity.TipoVuelo;

import java.time.LocalDateTime;

// Parte "datos" (JSON) del multipart que envía Ventas junto con el PDF
public record BoletoRequestDTO(
        @NotNull(message = "Indica si es el vuelo de llegada o de salida.")
        TipoVuelo tipoVuelo,
        @NotNull(message = "Elige la aerolínea.")
        Aerolinea aerolinea,
        @NotNull(message = "Ingresa el número de vuelo.")
        @Pattern(regexp = "^[A-Z0-9]{2} ?\\d{1,4}$", message = "El número de vuelo es el código de la aerolínea y 1 a 4 dígitos (ej. LA2380).")
        String numeroVuelo,
        @NotNull(message = "Ingresa el código de reserva (PNR).")
        @Pattern(regexp = "^[A-Z0-9]{6}$", message = "El PNR tiene 6 letras o números (ej. QX7LMB).")
        String codigoPnr,
        @NotNull(message = "Ingresa el aeropuerto de origen.")
        @Pattern(regexp = "^[A-Z]{3}$", message = "El aeropuerto de origen es un código IATA de 3 letras (ej. LIM).")
        String aeropuertoOrigen,
        @NotNull(message = "Ingresa el aeropuerto de destino.")
        @Pattern(regexp = "^[A-Z]{3}$", message = "El aeropuerto de destino es un código IATA de 3 letras (ej. TPP).")
        String aeropuertoDestino,
        @NotNull(message = "Ingresa la fecha y hora de salida del vuelo.")
        LocalDateTime fechaHoraSalida,
        @NotNull(message = "Ingresa la fecha y hora de llegada del vuelo.")
        LocalDateTime fechaHoraLlegada
) {
}
