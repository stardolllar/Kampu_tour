package pe.edu.upeu.lp2_gp2.itinerario.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Grupo consolidado del panel FIDS: segmentos de distintas reservas que comparten tipo,
 * lugar clave (aeropuerto / atractivo) y ventana horaria.
 * grupoCodigo != null cuando el grupo ya es un viaje asignado; excedeCapacidad si los pax superan el vehículo.
 */
public record GrupoFidsDTO(
        String clave, String tipo, Long lugarId, String lugar, LocalDateTime desde, LocalDateTime hasta,
        int pax, int reservas, List<String> vuelos, String estado, boolean asignado, boolean excedeCapacidad,
        String grupoCodigo, Long vehiculoId, String placa, Integer capacidad, Long operadorId, String operador,
        List<SegmentoFidsFila> segmentos
) {
}
