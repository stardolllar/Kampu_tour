package pe.edu.upeu.lp2_gp2.itinerario.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Parada de la ruta optimizada del operador (orden de visita).
 * tipo: INICIO (aeropuerto / base), RECOJO (hotel), DESTINO (atractivo / aeropuerto) o ENTREGA (hotel).
 */
public record ParadaDTO(int orden, String tipo, Long lugarId, String lugar, BigDecimal latitud, BigDecimal longitud,
                        Integer minutosDesdeAnterior, List<Long> segmentoIds, List<String> pasajeros) {
}
