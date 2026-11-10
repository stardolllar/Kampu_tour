package pe.edu.upeu.lp2_gp2.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.Immutable;

// Tabla AUDITORIA (la llenan TRG_AUDITORIA_ESTADO_PAQUETE y SP_REGISTRAR_CLIENTE_EXTERNO)
@Entity
@Immutable
@Table(name = "AUDITORIA")
public class Auditoria {
    @Id
    @Column(name = "auditoria_id")
    private Long auditoriaId;

    @Column(name = "entidad", length = 30)
    private String entidad;

    @Column(name = "entidad_id")
    private Long entidadId;

    @Column(name = "campo", length = 30)
    private String campo;

    @Column(name = "valor_anterior", length = 100)
    private String valorAnterior;

    @Column(name = "valor_nuevo", length = 100)
    private String valorNuevo;

    @Column(name = "usuario", length = 100)
    private String usuario;

    // Clave de partición de AUDITORIA (03_particionamiento_roles.sql): Oracle no deja cambiar su tamaño (ORA-14060)
    @Column(name = "rol_usuario", length = 20)
    private String rolUsuario;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    public Auditoria() {
    }

    public Long getAuditoriaId() {
        return auditoriaId;
    }

    public void setAuditoriaId(Long auditoriaId) {
        this.auditoriaId = auditoriaId;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(Long entidadId) {
        this.entidadId = entidadId;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getValorAnterior() {
        return valorAnterior;
    }

    public void setValorAnterior(String valorAnterior) {
        this.valorAnterior = valorAnterior;
    }

    public String getValorNuevo() {
        return valorNuevo;
    }

    public void setValorNuevo(String valorNuevo) {
        this.valorNuevo = valorNuevo;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getRolUsuario() {
        return rolUsuario;
    }

    public void setRolUsuario(String rolUsuario) {
        this.rolUsuario = rolUsuario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
