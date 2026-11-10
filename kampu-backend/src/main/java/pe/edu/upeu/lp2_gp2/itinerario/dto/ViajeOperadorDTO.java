package pe.edu.upeu.lp2_gp2.itinerario.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Viaje del operador para HOY con su "Manifiesto de Pasajeros":
 * segmentos (una fila por reserva), pasajeros, vuelos y la ruta ordenada (OSRM o estimada).
 */
public record ViajeOperadorDTO(
        String grupoCodigo, String tipo, LocalDateTime horaInicio, String placa, Integer capacidad,
        String estado, String siguienteEstado, int paso, int pasos, int pax, List<String> vuelos,
        String fuenteRuta, Integer minutosTotales, List<ParadaDTO> ruta, List<ManifiestoDTO> manifiesto
) {
    public record ManifiestoDTO(Long segmentoId, Long reservaId, String cliente, String hotel, Integer pax,
                                List<String> pasajeros, String vuelo, String estado, String siguienteEstado) {
    }
}
