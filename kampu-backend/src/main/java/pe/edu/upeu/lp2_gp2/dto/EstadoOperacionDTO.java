package pe.edu.upeu.lp2_gp2.dto;

import java.util.List;

// DOMINIO_ESTADOS + transiciones permitidas (DOMINIO_TRANSICIONES)
public record EstadoOperacionDTO(
        String codigo,
        String nombre,
        Integer orden,
        boolean requiereRecursos,
        boolean ocupaRecursos,
        boolean esFinal,
        List<String> siguientes
) {
}
