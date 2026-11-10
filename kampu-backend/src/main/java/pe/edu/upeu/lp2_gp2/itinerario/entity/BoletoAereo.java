package pe.edu.upeu.lp2_gp2.itinerario.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Boleto aéreo cargado a mano por Ventas: el vuelo se compra fuera del sistema
 * (LATAM / Sky / JetSMART) y aquí se registra el PNR, los horarios y el PDF del
 * boarding pass / e-ticket (archivoPdf = ruta relativa dentro del almacenamiento).
 * Un boleto de LLEGADA y uno de SALIDA por reserva (uq_ba_tipo).
 */
@Entity
@Table(name = "BOLETOS_AEREOS")
public class BoletoAereo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "boleto_id")
    private Long boletoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private ReservaMaster reservaMaster;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vuelo", nullable = false, length = 10)
    private TipoVuelo tipoVuelo;

    @Enumerated(EnumType.STRING)
    @Column(name = "aerolinea", nullable = false, length = 20)
    private Aerolinea aerolinea;

    @Column(name = "numero_vuelo", nullable = false, length = 10)
    private String numeroVuelo;

    @Column(name = "codigo_pnr", nullable = false, length = 6)
    private String codigoPnr;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "aeropuerto_origen", nullable = false, length = 3)
    private String aeropuertoOrigen;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "aeropuerto_destino", nullable = false, length = 3)
    private String aeropuertoDestino;

    @Column(name = "fecha_hora_salida", nullable = false)
    private LocalDateTime fechaHoraSalida;

    @Column(name = "fecha_hora_llegada", nullable = false)
    private LocalDateTime fechaHoraLlegada;

    @Column(name = "archivo_pdf", length = 300)
    private String archivoPdf;

    @Column(name = "nombre_archivo", length = 200)
    private String nombreArchivo;

    @Column(name = "tamanio_bytes")
    private Long tamanioBytes;

    @Column(name = "registrado_por", length = 100)
    private String registradoPor;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    protected BoletoAereo() {
    }

    public BoletoAereo(ReservaMaster reservaMaster, TipoVuelo tipoVuelo) {
        this.reservaMaster = reservaMaster;
        this.tipoVuelo = tipoVuelo;
    }

    /** Hora que dispara la logística: llegada a destino (LLEGADA) o despegue (SALIDA) */
    public LocalDateTime horaClave() {
        return tipoVuelo == TipoVuelo.LLEGADA ? fechaHoraLlegada : fechaHoraSalida;
    }

    public String vuelo() {
        return aerolinea + " " + numeroVuelo;
    }

    public Long getBoletoId() { return boletoId; }
    public ReservaMaster getReservaMaster() { return reservaMaster; }
    public TipoVuelo getTipoVuelo() { return tipoVuelo; }
    public Aerolinea getAerolinea() { return aerolinea; }
    public void setAerolinea(Aerolinea aerolinea) { this.aerolinea = aerolinea; }
    public String getNumeroVuelo() { return numeroVuelo; }
    public void setNumeroVuelo(String numeroVuelo) { this.numeroVuelo = numeroVuelo; }
    public String getCodigoPnr() { return codigoPnr; }
    public void setCodigoPnr(String codigoPnr) { this.codigoPnr = codigoPnr; }
    public String getAeropuertoOrigen() { return aeropuertoOrigen; }
    public void setAeropuertoOrigen(String aeropuertoOrigen) { this.aeropuertoOrigen = aeropuertoOrigen; }
    public String getAeropuertoDestino() { return aeropuertoDestino; }
    public void setAeropuertoDestino(String aeropuertoDestino) { this.aeropuertoDestino = aeropuertoDestino; }
    public LocalDateTime getFechaHoraSalida() { return fechaHoraSalida; }
    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) { this.fechaHoraSalida = fechaHoraSalida; }
    public LocalDateTime getFechaHoraLlegada() { return fechaHoraLlegada; }
    public void setFechaHoraLlegada(LocalDateTime fechaHoraLlegada) { this.fechaHoraLlegada = fechaHoraLlegada; }
    public String getArchivoPdf() { return archivoPdf; }
    public String getNombreArchivo() { return nombreArchivo; }
    public Long getTamanioBytes() { return tamanioBytes; }
    public String getRegistradoPor() { return registradoPor; }
    public void setRegistradoPor(String registradoPor) { this.registradoPor = registradoPor; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }

    public void adjuntar(String ruta, String nombre, long bytes) {
        this.archivoPdf = ruta;
        this.nombreArchivo = nombre;
        this.tamanioBytes = bytes;
    }

    @Override
    public String toString() {
        return "BoletoAereo#" + boletoId;
    }
}
