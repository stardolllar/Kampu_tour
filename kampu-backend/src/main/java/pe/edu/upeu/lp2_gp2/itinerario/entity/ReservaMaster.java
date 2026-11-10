package pe.edu.upeu.lp2_gp2.itinerario.entity;

import jakarta.persistence.*;
import pe.edu.upeu.lp2_gp2.entity.Lugar;
import pe.edu.upeu.lp2_gp2.entity.Reserva;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Reserva Master: el paquete vendido al cliente visto como unidad logística.
 * Comparte la clave con RESERVAS (relación 1 a 1, sin modificar esa tabla) y agrupa
 * sus segmentos operativos y boletos aéreos.
 */
@Entity
@Table(name = "RESERVAS_MASTER")
public class ReservaMaster {
    @Id
    @Column(name = "reserva_id")
    private Long reservaId;

    // Reserva comercial (solo lectura desde aquí: la gestiona el módulo de reservas)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", insertable = false, updatable = false)
    private Reserva reserva;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_logistico", nullable = false, length = 30)
    private EstadoLogistico estadoLogistico = EstadoLogistico.VUELO_PENDIENTE;

    // Hotel donde se aloja el pasajero (destino del Transfer In, recojo de tours y Transfer Out)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Lugar hotel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aeropuerto_id", nullable = false)
    private Lugar aeropuerto;

    @Column(name = "creado_por", length = 100)
    private String creadoPor;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @OneToMany(mappedBy = "reservaMaster")
    @OrderBy("horaInicio ASC")
    private List<SegmentoOperativo> segmentos = new ArrayList<>();

    @OneToMany(mappedBy = "reservaMaster")
    @OrderBy("tipoVuelo ASC")
    private List<BoletoAereo> boletos = new ArrayList<>();

    protected ReservaMaster() {
    }

    public ReservaMaster(Long reservaId, Lugar hotel, Lugar aeropuerto, String creadoPor) {
        this.reservaId = reservaId;
        this.hotel = hotel;
        this.aeropuerto = aeropuerto;
        this.creadoPor = creadoPor;
    }

    public void tocar() {
        this.actualizadoEn = LocalDateTime.now();
    }

    public Long getReservaId() { return reservaId; }
    public Reserva getReserva() { return reserva; }
    public EstadoLogistico getEstadoLogistico() { return estadoLogistico; }
    public void setEstadoLogistico(EstadoLogistico estadoLogistico) { this.estadoLogistico = estadoLogistico; }
    public Lugar getHotel() { return hotel; }
    public void setHotel(Lugar hotel) { this.hotel = hotel; }
    public Lugar getAeropuerto() { return aeropuerto; }
    public void setAeropuerto(Lugar aeropuerto) { this.aeropuerto = aeropuerto; }
    public String getCreadoPor() { return creadoPor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public List<SegmentoOperativo> getSegmentos() { return segmentos; }
    public List<BoletoAereo> getBoletos() { return boletos; }

    @Override
    public String toString() {
        return "ReservaMaster#" + reservaId;
    }
}
