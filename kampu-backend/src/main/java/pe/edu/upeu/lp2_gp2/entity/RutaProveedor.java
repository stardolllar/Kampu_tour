package pe.edu.upeu.lp2_gp2.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

// Ruta (origen -> destino) que opera una empresa de transporte
@Entity
@Table(name = "RUTAS_PROVEEDOR")
public class RutaProveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ruta_id")
    private Long rutaId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "origen_id", nullable = false)
    private Long origenId;

    @Column(name = "destino_id", nullable = false)
    private Long destinoId;

    @Column(name = "tipo_servicio", nullable = false, length = 20)
    private String tipoServicio;

    @Column(name = "duracion_estimada_min", nullable = false)
    private Integer duracionEstimadaMin;

    @Column(name = "tarifa_referencial", precision = 10, scale = 2)
    private BigDecimal tarifaReferencial;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    public RutaProveedor() {
    }

    public Long getRutaId() {
        return rutaId;
    }

    public void setRutaId(Long rutaId) {
        this.rutaId = rutaId;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public Long getOrigenId() {
        return origenId;
    }

    public void setOrigenId(Long origenId) {
        this.origenId = origenId;
    }

    public Long getDestinoId() {
        return destinoId;
    }

    public void setDestinoId(Long destinoId) {
        this.destinoId = destinoId;
    }

    public String getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(String tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public Integer getDuracionEstimadaMin() {
        return duracionEstimadaMin;
    }

    public void setDuracionEstimadaMin(Integer duracionEstimadaMin) {
        this.duracionEstimadaMin = duracionEstimadaMin;
    }

    public BigDecimal getTarifaReferencial() {
        return tarifaReferencial;
    }

    public void setTarifaReferencial(BigDecimal tarifaReferencial) {
        this.tarifaReferencial = tarifaReferencial;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
