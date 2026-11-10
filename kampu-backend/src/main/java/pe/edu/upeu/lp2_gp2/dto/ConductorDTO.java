package pe.edu.upeu.lp2_gp2.dto;

import pe.edu.upeu.lp2_gp2.entity.Conductor;

import java.time.LocalDate;

public record ConductorDTO(
        Long conductorId,
        Long empresaId,
        String nombres,
        String numeroLicencia,
        String categoriaLicencia,
        LocalDate fechaVencLicencia,
        String telefono,
        String estadoDisponibilidad
) {
    public static ConductorDTO desde(Conductor c) {
        return new ConductorDTO(c.getConductorId(), c.getEmpresaId(), c.getNombres(), c.getNumeroLicencia(),
                c.getCategoriaLicencia(), c.getFechaVencLicencia(), c.getTelefono() == null ? "" : c.getTelefono(),
                c.getEstadoDisponibilidad());
    }
}
