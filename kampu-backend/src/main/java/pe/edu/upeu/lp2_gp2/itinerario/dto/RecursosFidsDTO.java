package pe.edu.upeu.lp2_gp2.itinerario.dto;

import java.util.List;

// Vehículos operativos y operadores activos para asignar en el FIDS
public record RecursosFidsDTO(List<VehiculoItem> vehiculos, List<OperadorItem> operadores) {
    public record VehiculoItem(Long vehiculoId, String placa, String tipo, Integer capacidad) {
    }

    public record OperadorItem(Long usuarioId, String username) {
    }
}
