package pe.edu.upeu.lp2_gp2.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "VEHICULO")
public class Vehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehiculo_id")
    private Long vehiculoId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "placa", nullable = false, length = 10)
    private String placa;

    @Column(name = "tipo_vehiculo", nullable = false, length = 20)
    private String tipoVehiculo;

    @Column(name = "marca_modelo", length = 100)
    private String marcaModelo;

    @Column(name = "capacidad_pax", nullable = false)
    private Integer capacidadPax;

    @Column(name = "fecha_prox_mantenimiento", nullable = false)
    private LocalDate fechaProxMantenimiento;

    @Column(name = "estado_operativo", nullable = false, length = 20)
    private String estadoOperativo;

    public Vehiculo() {
    }

    public Long getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Long vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getTipoVehiculo() {
        return tipoVehiculo;
    }

    public void setTipoVehiculo(String tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }

    public String getMarcaModelo() {
        return marcaModelo;
    }

    public void setMarcaModelo(String marcaModelo) {
        this.marcaModelo = marcaModelo;
    }

    public Integer getCapacidadPax() {
        return capacidadPax;
    }

    public void setCapacidadPax(Integer capacidadPax) {
        this.capacidadPax = capacidadPax;
    }

    public LocalDate getFechaProxMantenimiento() {
        return fechaProxMantenimiento;
    }

    public void setFechaProxMantenimiento(LocalDate fechaProxMantenimiento) {
        this.fechaProxMantenimiento = fechaProxMantenimiento;
    }

    public String getEstadoOperativo() {
        return estadoOperativo;
    }

    public void setEstadoOperativo(String estadoOperativo) {
        this.estadoOperativo = estadoOperativo;
    }
}
