package pe.edu.upeu.lp2_gp2.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.DynamicUpdate;

// Tráfico de transporte. trg_operacion_transporte genera el código KMP-###, calcula
// la llegada estimada con la ruta, valida transiciones (DOMINIO_TRANSICIONES),
// cruces de horario, descanso del conductor, mantenimiento y capacidad de la salida,
// y registra cada cambio de estado en HISTORIAL_TRAFICO_OPERACION.
@Entity
@DynamicUpdate
@Table(name = "OPERACION_TRANSPORTE")
public class OperacionTransporte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operacion_id")
    private Long operacionId;

    // Lo genera el trigger (KMP-001, KMP-002...)
    @Column(name = "codigo_operacion", insertable = false, updatable = false)
    private String codigoOperacion;

    @Column(name = "salida_id")
    private Long salidaId;

    @Column(name = "origen_id", nullable = false)
    private Long origenId;

    @Column(name = "destino_id", nullable = false)
    private Long destinoId;

    @Column(name = "empresa_id")
    private Long empresaId;

    @Column(name = "vehiculo_id")
    private Long vehiculoId;

    @Column(name = "conductor_id")
    private Long conductorId;

    @Column(name = "pax_programados", nullable = false)
    private Integer paxProgramados;

    @Column(name = "hora_programada_salida", nullable = false)
    private LocalDateTime horaProgramadaSalida;

    // Si va nula, el trigger la calcula con la duración de la ruta
    @Column(name = "hora_estimada_llegada")
    private LocalDateTime horaEstimadaLlegada;

    @Column(name = "estado_actual", nullable = false, length = 20)
    private String estadoActual;

    @Column(name = "observaciones", length = 200)
    private String observaciones;

    public OperacionTransporte() {
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

    public Long getSalidaId() {
        return salidaId;
    }

    public void setSalidaId(Long salidaId) {
        this.salidaId = salidaId;
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

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public Long getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Long vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public Long getConductorId() {
        return conductorId;
    }

    public void setConductorId(Long conductorId) {
        this.conductorId = conductorId;
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

    public String getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(String estadoActual) {
        this.estadoActual = estadoActual;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
