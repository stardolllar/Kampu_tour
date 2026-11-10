package pe.edu.upeu.lp2_gp2.itinerario.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TableroFidsDTO(LocalDate fecha, LocalDateTime generadoEn, int segmentos, int pax, int sinAsignar,
                             List<GrupoFidsDTO> grupos) {
}
