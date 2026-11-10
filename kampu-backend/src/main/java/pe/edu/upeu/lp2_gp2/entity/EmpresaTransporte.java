package pe.edu.upeu.lp2_gp2.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;

@Entity
@Table(name = "EMPRESA_TRANSPORTE")
public class EmpresaTransporte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "empresa_id")
    private Long empresaId;

    @Column(name = "nombre_comercial", nullable = false, length = 150)
    private String nombreComercial;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "ruc", nullable = false, length = 11)
    private String ruc;

    @Column(name = "contacto_nombre", length = 150)
    private String contactoNombre;

    @Column(name = "contacto_telefono", nullable = false, length = 20)
    private String contactoTelefono;

    @Column(name = "contacto_email", length = 100)
    private String contactoEmail;

    @Column(name = "direccion", length = 250)
    private String direccion;

    @Column(name = "estado_afiliacion", nullable = false, length = 20)
    private String estadoAfiliacion;

    @Column(name = "disponibilidad_operativa", nullable = false, length = 20)
    private String disponibilidadOperativa;

    @Column(name = "fecha_afiliacion")
    private LocalDate fechaAfiliacion;

    public EmpresaTransporte() {
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getContactoNombre() {
        return contactoNombre;
    }

    public void setContactoNombre(String contactoNombre) {
        this.contactoNombre = contactoNombre;
    }

    public String getContactoTelefono() {
        return contactoTelefono;
    }

    public void setContactoTelefono(String contactoTelefono) {
        this.contactoTelefono = contactoTelefono;
    }

    public String getContactoEmail() {
        return contactoEmail;
    }

    public void setContactoEmail(String contactoEmail) {
        this.contactoEmail = contactoEmail;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEstadoAfiliacion() {
        return estadoAfiliacion;
    }

    public void setEstadoAfiliacion(String estadoAfiliacion) {
        this.estadoAfiliacion = estadoAfiliacion;
    }

    public String getDisponibilidadOperativa() {
        return disponibilidadOperativa;
    }

    public void setDisponibilidadOperativa(String disponibilidadOperativa) {
        this.disponibilidadOperativa = disponibilidadOperativa;
    }

    public LocalDate getFechaAfiliacion() {
        return fechaAfiliacion;
    }

    public void setFechaAfiliacion(LocalDate fechaAfiliacion) {
        this.fechaAfiliacion = fechaAfiliacion;
    }
}
