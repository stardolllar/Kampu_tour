package pe.edu.upeu.lp2_gp2.itinerario.entity;

import jakarta.persistence.*;
import pe.edu.upeu.lp2_gp2.entity.Lugar;
import pe.edu.upeu.lp2_gp2.entity.UsuarioSistema;
import pe.edu.upeu.lp2_gp2.entity.Vehiculo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Segmento operativo: una tarea de campo de la reserva master (Transfer In, Tour diario,
 * Transfer Out o Alojamiento). El Admin consolida segmentos de distintas reservas en un
 * mismo viaje (grupoCodigo) y le asigna vehículo y operador; el operador avanza su estado.
 * version: bloqueo optimista (dos operadores no pisan el mismo cambio).
 */
@Entity
@Table(name = "SEGMENTOS_OPERATIVOS")
public class SegmentoOperativo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "segmento_id")
    private Long segmentoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private ReservaMaster reservaMaster;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoSegmento tipo;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalDateTime horaInicio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origen_id", nullable = false)
    private Lugar origen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destino_id", nullable = false)
    private Lugar destino;

    @Column(name = "pax", nullable = false)
    private Integer pax;

    // Vuelo que origina el transfer (solo TRANSFER_IN / TRANSFER_OUT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boleto_id")
    private BoletoAereo boleto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    // Usuario con rol OPERADOR (chofer / guía) que ejecuta el servicio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operador_id")
    private UsuarioSistema operador;

    @Column(name = "grupo_codigo", length = 30)
    private String grupoCodigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoSegmento estado = EstadoSegmento.PROGRAMADO;

    @Column(name = "estado_actualizado_en")
    private LocalDateTime estadoActualizadoEn;

    @Column(name = "observacion", length = 200)
    private String observacion;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected SegmentoOperativo() {
    }

    public SegmentoOperativo(ReservaMaster reservaMaster, TipoSegmento tipo, LocalDateTime horaInicio,
                             Lugar origen, Lugar destino, int pax) {
        this.reservaMaster = reservaMaster;
        this.tipo = tipo;
        this.origen = origen;
        this.destino = destino;
        this.pax = pax;
        programar(horaInicio);
    }

    public final void programar(LocalDateTime hora) {
        this.horaInicio = hora;
        this.fecha = hora.toLocalDate();
    }

    public boolean asignado() {
        return grupoCodigo != null;
    }

    public void asignar(String grupo, Vehiculo vehiculo, UsuarioSistema operador) {
        this.grupoCodigo = grupo;
        this.vehiculo = vehiculo;
        this.operador = operador;
    }

    public void liberar() {
        asignar(null, null, null);
    }

    public void cambiarEstado(EstadoSegmento nuevo) {
        this.estado = nuevo;
        this.estadoActualizadoEn = LocalDateTime.now();
    }

    public Long getSegmentoId() { return segmentoId; }
    public ReservaMaster getReservaMaster() { return reservaMaster; }
    public TipoSegmento getTipo() { return tipo; }
    public LocalDate getFecha() { return fecha; }
    public LocalDateTime getHoraInicio() { return horaInicio; }
    public Lugar getOrigen() { return origen; }
    public void setOrigen(Lugar origen) { this.origen = origen; }
    public Lugar getDestino() { return destino; }
    public void setDestino(Lugar destino) { this.destino = destino; }
    public Integer getPax() { return pax; }
    public void setPax(Integer pax) { this.pax = pax; }
    public BoletoAereo getBoleto() { return boleto; }
    public void setBoleto(BoletoAereo boleto) { this.boleto = boleto; }
    public Vehiculo getVehiculo() { return vehiculo; }
    public UsuarioSistema getOperador() { return operador; }
    public String getGrupoCodigo() { return grupoCodigo; }
    public EstadoSegmento getEstado() { return estado; }
    public LocalDateTime getEstadoActualizadoEn() { return estadoActualizadoEn; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public Long getVersion() { return version; }

    @Override
    public String toString() {
        return "SegmentoOperativo#" + segmentoId;
    }
}
