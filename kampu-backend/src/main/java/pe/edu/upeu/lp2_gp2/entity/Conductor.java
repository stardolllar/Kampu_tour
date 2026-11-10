package pe.edu.upeu.lp2_gp2.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "CONDUCTOR")
public class Conductor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conductor_id")
    private Long conductorId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "nombres", nullable = false, length = 150)
    private String nombres;

    @Column(name = "numero_licencia", nullable = false, length = 20)
    private String numeroLicencia;

    @Column(name = "categoria_licencia", nullable = false, length = 10)
    private String categoriaLicencia;

    @Column(name = "fecha_venc_licencia", nullable = false)
    private LocalDate fechaVencLicencia;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "estado_disponibilidad", nullable = false, length = 20)
    private String estadoDisponibilidad;

    public Conductor() {
    }

    public Long getConductorId() {
        return conductorId;
    }

    public void setConductorId(Long conductorId) {
        this.conductorId = conductorId;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getNumeroLicencia() {
        return numeroLicencia;
    }

    public void setNumeroLicencia(String numeroLicencia) {
        this.numeroLicencia = numeroLicencia;
    }

    public String getCategoriaLicencia() {
        return categoriaLicencia;
    }

    public void setCategoriaLicencia(String categoriaLicencia) {
        this.categoriaLicencia = categoriaLicencia;
    }

    public LocalDate getFechaVencLicencia() {
        return fechaVencLicencia;
    }

    public void setFechaVencLicencia(LocalDate fechaVencLicencia) {
        this.fechaVencLicencia = fechaVencLicencia;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEstadoDisponibilidad() {
        return estadoDisponibilidad;
    }

    public void setEstadoDisponibilidad(String estadoDisponibilidad) {
        this.estadoDisponibilidad = estadoDisponibilidad;
    }
}
