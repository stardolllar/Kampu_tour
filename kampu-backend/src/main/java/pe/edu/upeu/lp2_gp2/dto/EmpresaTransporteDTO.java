package pe.edu.upeu.lp2_gp2.dto;

import pe.edu.upeu.lp2_gp2.entity.EmpresaTransporte;
import pe.edu.upeu.lp2_gp2.entity.VistaEmpresaCapacidad;

import java.time.LocalDate;

// Empresa + capacidad calculada por la vista VW_EMPRESA_CAPACIDAD
public record EmpresaTransporteDTO(
        Long empresaId,
        String nombreComercial,
        String ruc,
        String contactoNombre,
        String contactoTelefono,
        String contactoEmail,
        String direccion,
        String estadoAfiliacion,
        String disponibilidadOperativa,
        LocalDate fechaAfiliacion,
        Integer totalVehiculos,
        Integer capacidadTotalTransporte,
        Integer conductoresDisponibles
) {
    public static EmpresaTransporteDTO desde(EmpresaTransporte e, VistaEmpresaCapacidad cap) {
        return new EmpresaTransporteDTO(e.getEmpresaId(), e.getNombreComercial(), e.getRuc(),
                e.getContactoNombre() == null ? "" : e.getContactoNombre(), e.getContactoTelefono(),
                e.getContactoEmail() == null ? "" : e.getContactoEmail(), e.getDireccion() == null ? "" : e.getDireccion(),
                e.getEstadoAfiliacion(), e.getDisponibilidadOperativa(), e.getFechaAfiliacion(),
                cap == null ? 0 : cap.getTotalVehiculos(), cap == null ? 0 : cap.getCapacidadTotalTransporte(),
                cap == null ? 0 : cap.getConductoresDisponibles());
    }
}
