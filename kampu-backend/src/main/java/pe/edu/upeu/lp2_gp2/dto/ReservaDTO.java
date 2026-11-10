package pe.edu.upeu.lp2_gp2.dto;

import pe.edu.upeu.lp2_gp2.entity.DetalleReserva;
import pe.edu.upeu.lp2_gp2.entity.Reserva;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Cabecera (RESERVAS) + detalle de la salida reservada (DETALLE_RESERVA)
public record ReservaDTO(
        Long reservaId,
        Long clienteId,
        Long detalleId,
        Long salidaId,
        LocalDateTime fechaReserva,
        Integer numeroViajeros,
        BigDecimal precioUnitario,
        BigDecimal descuento,
        BigDecimal montoTotal,
        BigDecimal montoPagado,
        BigDecimal saldo,
        String estadoReserva,
        String estadoPago,
        String observaciones,
        String formaReembolso,
        Long usuarioAnulaId,
        LocalDateTime boletaEnviadaEn
) {
    public static ReservaDTO desde(Reserva r, DetalleReserva d) {
        BigDecimal pagado = r.getMontoPagado() == null ? BigDecimal.ZERO : r.getMontoPagado();
        BigDecimal total = r.getMontoTotal() == null ? BigDecimal.ZERO : r.getMontoTotal();
        return new ReservaDTO(r.getReservaId(), r.getClienteId(),
                d == null ? null : d.getDetalleId(), d == null ? null : d.getSalidaId(), r.getFechaReserva(),
                d == null ? 0 : d.getCantidad(), d == null ? null : d.getPrecioUnitario(),
                d == null ? BigDecimal.ZERO : d.getDescuento(), total, pagado, total.subtract(pagado).max(BigDecimal.ZERO),
                r.getEstadoReserva(), r.getEstadoPago(), r.getObservaciones() == null ? "" : r.getObservaciones(),
                r.getFormaReembolso(), r.getUsuarioAnulaId(), r.getBoletaEnviadaEn());
    }
}
