package pe.edu.upeu.lp2_gp2.dto;

import pe.edu.upeu.lp2_gp2.entity.DetalleReserva;
import pe.edu.upeu.lp2_gp2.entity.Reserva;
import pe.edu.upeu.lp2_gp2.entity.VistaReservaResumen;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Cabecera (RESERVAS) + detalle de la salida reservada (DETALLE_RESERVA)
// + número de viajeros y saldo de la vista V_RESERVAS_RESUMEN
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
        String contactoNombre,
        String contactoEmail,
        String contactoTelefono,
        LocalDateTime boletaEnviadaEn
) {
    public static ReservaDTO desde(Reserva r, DetalleReserva d, VistaReservaResumen resumen) {
        BigDecimal pagado = r.getMontoPagado() == null ? BigDecimal.ZERO : r.getMontoPagado();
        BigDecimal total = r.getMontoTotal() == null ? BigDecimal.ZERO : r.getMontoTotal();
        return new ReservaDTO(r.getReservaId(), r.getClienteId(),
                d == null ? null : d.getDetalleId(), d == null ? null : d.getSalidaId(), r.getFechaReserva(),
                resumen == null ? (d == null ? 0 : d.getCantidad()) : resumen.getNumeroViajeros(),
                d == null ? null : d.getPrecioUnitario(), d == null ? BigDecimal.ZERO : d.getDescuento(),
                total, pagado, resumen == null ? total.subtract(pagado) : resumen.getSaldo(),
                r.getEstadoReserva(), r.getEstadoPago(), r.getObservaciones() == null ? "" : r.getObservaciones(),
                r.getFormaReembolso(), r.getUsuarioAnulaId(),
                r.getContactoNombre(), r.getContactoEmail(), r.getContactoTelefono(), r.getBoletaEnviadaEn());
    }
}
