package pe.edu.upeu.lp2_gp2.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

// Vista VW_EMPRESA_CAPACIDAD: capacidad total (vehículos operativos) por empresa
@Entity
@Immutable
@Table(name = "VW_EMPRESA_CAPACIDAD")
public class VistaEmpresaCapacidad {
    @Id
    @Column(name = "empresa_id")
    private Long empresaId;

    @Column(name = "total_vehiculos")
    private Integer totalVehiculos;

    @Column(name = "capacidad_total_transporte")
    private Integer capacidadTotalTransporte;

    @Column(name = "conductores_disponibles")
    private Integer conductoresDisponibles;

    public VistaEmpresaCapacidad() {
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public Integer getTotalVehiculos() {
        return totalVehiculos;
    }

    public void setTotalVehiculos(Integer totalVehiculos) {
        this.totalVehiculos = totalVehiculos;
    }

    public Integer getCapacidadTotalTransporte() {
        return capacidadTotalTransporte;
    }

    public void setCapacidadTotalTransporte(Integer capacidadTotalTransporte) {
        this.capacidadTotalTransporte = capacidadTotalTransporte;
    }

    public Integer getConductoresDisponibles() {
        return conductoresDisponibles;
    }

    public void setConductoresDisponibles(Integer conductoresDisponibles) {
        this.conductoresDisponibles = conductoresDisponibles;
    }
}
