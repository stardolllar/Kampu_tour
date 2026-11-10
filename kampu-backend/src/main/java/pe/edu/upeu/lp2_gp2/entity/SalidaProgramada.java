package pe.edu.upeu.lp2_gp2.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import org.hibernate.annotations.DynamicUpdate;

// trg_salida_biu calcula fecha_fin (si va nula) y cupos_disponibles;
// trg_detalle_reserva_biud y trg_reserva_estado_bu mantienen los cupos.
// @DynamicUpdate: el UPDATE solo lleva las columnas cambiadas (así los triggers
// "UPDATE OF ..." solo se disparan cuando corresponde).
@Entity
@DynamicUpdate
@Table(name = "SALIDAS_PROGRAMADAS")
public class SalidaProgramada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salida_id")
    private Long salidaId;

    @Column(name = "paquete_id", nullable = false)
    private Long paqueteId;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "cupos_maximos", nullable = false)
    private Integer cuposMaximos;

    // Lo mantienen los triggers: Hibernate nunca lo escribe
    @Column(name = "cupos_disponibles", insertable = false, updatable = false)
    private Integer cuposDisponibles;

    @Column(name = "guia_id")
    private Long guiaId;

    @Column(name = "estado", length = 20)
    private String estado;

    public SalidaProgramada() {
    }

    public Long getSalidaId() {
        return salidaId;
    }

    public void setSalidaId(Long salidaId) {
        this.salidaId = salidaId;
    }

    public Long getPaqueteId() {
        return paqueteId;
    }

    public void setPaqueteId(Long paqueteId) {
        this.paqueteId = paqueteId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Integer getCuposMaximos() {
        return cuposMaximos;
    }

    public void setCuposMaximos(Integer cuposMaximos) {
        this.cuposMaximos = cuposMaximos;
    }

    public Integer getCuposDisponibles() {
        return cuposDisponibles;
    }

    public void setCuposDisponibles(Integer cuposDisponibles) {
        this.cuposDisponibles = cuposDisponibles;
    }

    public Long getGuiaId() {
        return guiaId;
    }

    public void setGuiaId(Long guiaId) {
        this.guiaId = guiaId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
