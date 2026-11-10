package pe.edu.upeu.lp2_gp2.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.Immutable;

// Vista VW_TABLERO_OPERACIONES: tablero tipo FIDS con horas reales y retrasos
@Entity
@Immutable
@Table(name = "VW_TABLERO_OPERACIONES")
public class VistaTableroOperacion {
    @Id
    @Column(name = "operacion_id")
    private Long operacionId;

    @Column(name = "codigo_operacion")
    private String codigoOperacion;

    @Column(name = "origen")
    private String origen;

    @Column(name = "destino")
    private String destino;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "placa")
    private String placa;

    @Column(name = "conductor")
    private String conductor;

    @Column(name = "pax_programados")
    private Integer paxProgramados;

    @Column(name = "hora_programada_salida")
    private LocalDateTime horaProgramadaSalida;

    @Column(name = "hora_estimada_llegada")
    private LocalDateTime horaEstimadaLlegada;

    @Column(name = "estado")
    private String estado;

    @Column(name = "orden_estado")
    private Integer ordenEstado;

    @Column(name = "hora_real_salida")
    private LocalDateTime horaRealSalida;

    @Column(name = "hora_real_llegada")
    private LocalDateTime horaRealLlegada;

    @Column(name = "min_retraso_salida")
    private Integer minRetrasoSalida;

    @Column(name = "min_retraso_llegada")
    private Integer minRetrasoLlegada;

    public VistaTableroOperacion() {
    }

    public Long getOperacionId() {
        return operacionId;
    }

    public void setOperacionId(Long operacionId) {
        this.operacionId = operacionId;
    }

    public String getCodigoOperacion() {
        return codigoOperacion;
    }

    public void setCodigoOperacion(String codigoOperacion) {
        this.codigoOperacion = codigoOperacion;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getConductor() {
        return conductor;
    }

    public void setConductor(String conductor) {
        this.conductor = conductor;
    }

    public Integer getPaxProgramados() {
        return paxProgramados;
    }

    public void setPaxProgramados(Integer paxProgramados) {
        this.paxProgramados = paxProgramados;
    }

    public LocalDateTime getHoraProgramadaSalida() {
        return horaProgramadaSalida;
    }

    public void setHoraProgramadaSalida(LocalDateTime horaProgramadaSalida) {
        this.horaProgramadaSalida = horaProgramadaSalida;
    }

    public LocalDateTime getHoraEstimadaLlegada() {
        return horaEstimadaLlegada;
    }

    public void setHoraEstimadaLlegada(LocalDateTime horaEstimadaLlegada) {
        this.horaEstimadaLlegada = horaEstimadaLlegada;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getOrdenEstado() {
        return ordenEstado;
    }

    public void setOrdenEstado(Integer ordenEstado) {
        this.ordenEstado = ordenEstado;
    }

    public LocalDateTime getHoraRealSalida() {
        return horaRealSalida;
    }

    public void setHoraRealSalida(LocalDateTime horaRealSalida) {
        this.horaRealSalida = horaRealSalida;
    }

    public LocalDateTime getHoraRealLlegada() {
        return horaRealLlegada;
    }

    public void setHoraRealLlegada(LocalDateTime horaRealLlegada) {
        this.horaRealLlegada = horaRealLlegada;
    }

    public Integer getMinRetrasoSalida() {
        return minRetrasoSalida;
    }

    public void setMinRetrasoSalida(Integer minRetrasoSalida) {
        this.minRetrasoSalida = minRetrasoSalida;
    }

    public Integer getMinRetrasoLlegada() {
        return minRetrasoLlegada;
    }

    public void setMinRetrasoLlegada(Integer minRetrasoLlegada) {
        this.minRetrasoLlegada = minRetrasoLlegada;
    }
}
