package pe.edu.upeu.lp2_gp2.itinerario.dto;

import java.time.LocalDate;
import java.util.List;

// Reserva master con su itinerario (o reserva aún sin itinerario: estadoLogistico = null)
public record ItinerarioDTO(
        Long reservaId, String cliente, String paquete, LocalDate fechaInicio, LocalDate fechaFin, Integer pax,
        String estadoReserva, String estadoLogistico, Long hotelId, String hotel, Long aeropuertoId, String aeropuerto,
        List<BoletoDTO> boletos, List<SegmentoDTO> segmentos
) {
}
