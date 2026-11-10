package pe.edu.upeu.lp2_gp2.itinerario.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

// Abrir o actualizar el itinerario de una reserva (Ventas): hotel, aeropuerto y tours por día
public record ItinerarioRequestDTO(
        @NotNull(message = "Elige el hotel del pasajero.") Long hotelId,
        @NotNull(message = "Elige el aeropuerto de llegada.") Long aeropuertoId,
        @Size(max = 30, message = "Máximo 30 tours por itinerario.") List<@Valid TourDiaDTO> tours
) {
    public record TourDiaDTO(
            @NotNull(message = "Elige el atractivo del tour.") Long destinoId,
            @NotNull(message = "Indica fecha y hora de recojo del tour.") LocalDateTime horaRecojo
    ) {
    }
}
