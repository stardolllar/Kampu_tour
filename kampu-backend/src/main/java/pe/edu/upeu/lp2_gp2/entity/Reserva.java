package pe.edu.upeu.lp2_gp2.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.DynamicUpdate;

// Cabecera de la reserva. monto_total = SUM(DETALLE_RESERVA.subtotal), lo mantiene
// trg_detalle_reserva_biud; al ANULAR, trg_reserva_estado_bu devuelve los cupos.
@Entity
@DynamicUpdate
@Table(name = "RESERVAS")
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reserva_id")
    private Long reservaId;

    // Nulo cuando la reserva es de un invitado (sin cliente registrado)
    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "fecha_reserva")
    private LocalDateTime fechaReserva;

    // Lo calcula el trigger del detalle: Hibernate nunca lo escribe
    @Column(name = "monto_total", insertable = false, updatable = false)
    private BigDecimal montoTotal;

    @Column(name = "monto_pagado", precision = 10, scale = 2)
    private BigDecimal montoPagado;

    @Column(name = "estado_reserva", length = 20)
    private String estadoReserva;

    @Column(name = "estado_pago", length = 20)
    private String estadoPago;

    @Column(name = "observaciones", length = 200)
    private String observaciones;

    @Column(name = "forma_reembolso", length = 50)
    private String formaReembolso;

    @Column(name = "usuario_anula_id")
    private Long usuarioAnulaId;

    // Datos de contacto del invitado (solo si cliente_id es nulo)
    @Column(name = "contacto_nombre", length = 150)
    private String contactoNombre;

    @Column(name = "contacto_email", length = 100)
    private String contactoEmail;

    @Column(name = "contacto_telefono", length = 20)
    private String contactoTelefono;

    @Column(name = "boleta_enviada_en")
    private LocalDateTime boletaEnviadaEn;

    public Reserva() {
    }

    public Long getReservaId() {
        return reservaId;
    }

    public void setReservaId(Long reservaId) {
        this.reservaId = reservaId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public BigDecimal getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(BigDecimal montoPagado) {
        this.montoPagado = montoPagado;
    }

    public String getEstadoReserva() {
        return estadoReserva;
    }

    public void setEstadoReserva(String estadoReserva) {
        this.estadoReserva = estadoReserva;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getFormaReembolso() {
        return formaReembolso;
    }

    public void setFormaReembolso(String formaReembolso) {
        this.formaReembolso = formaReembolso;
    }

    public Long getUsuarioAnulaId() {
        return usuarioAnulaId;
    }

    public void setUsuarioAnulaId(Long usuarioAnulaId) {
        this.usuarioAnulaId = usuarioAnulaId;
    }

    public String getContactoNombre() {
        return contactoNombre;
    }

    public void setContactoNombre(String contactoNombre) {
        this.contactoNombre = contactoNombre;
    }

    public String getContactoEmail() {
        return contactoEmail;
    }

    public void setContactoEmail(String contactoEmail) {
        this.contactoEmail = contactoEmail;
    }

    public String getContactoTelefono() {
        return contactoTelefono;
    }

    public void setContactoTelefono(String contactoTelefono) {
        this.contactoTelefono = contactoTelefono;
    }

    public LocalDateTime getBoletaEnviadaEn() {
        return boletaEnviadaEn;
    }

    public void setBoletaEnviadaEn(LocalDateTime boletaEnviadaEn) {
        this.boletaEnviadaEn = boletaEnviadaEn;
    }
}
