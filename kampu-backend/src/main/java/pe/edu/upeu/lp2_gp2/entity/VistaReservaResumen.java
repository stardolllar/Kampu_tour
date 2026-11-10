package pe.edu.upeu.lp2_gp2.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import org.hibernate.annotations.Immutable;

// Vista V_RESERVAS_RESUMEN: número de viajeros y saldo calculados desde el detalle
@Entity
@Immutable
@Table(name = "V_RESERVAS_RESUMEN")
public class VistaReservaResumen {
    @Id
    @Column(name = "reserva_id")
    private Long reservaId;

    @Column(name = "salidas_reservadas")
    private Integer salidasReservadas;

    @Column(name = "numero_viajeros")
    private Integer numeroViajeros;

    @Column(name = "saldo")
    private BigDecimal saldo;

    public VistaReservaResumen() {
    }

    public Long getReservaId() {
        return reservaId;
    }

    public void setReservaId(Long reservaId) {
        this.reservaId = reservaId;
    }

    public Integer getSalidasReservadas() {
        return salidasReservadas;
    }

    public void setSalidasReservadas(Integer salidasReservadas) {
        this.salidasReservadas = salidasReservadas;
    }

    public Integer getNumeroViajeros() {
        return numeroViajeros;
    }

    public void setNumeroViajeros(Integer numeroViajeros) {
        this.numeroViajeros = numeroViajeros;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }
}
