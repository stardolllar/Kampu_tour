package pe.edu.upeu.lp2_gp2.itinerario.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.lp2_gp2.exception.AccesoDenegadoException;
import pe.edu.upeu.lp2_gp2.exception.ResourceNotFoundException;
import pe.edu.upeu.lp2_gp2.itinerario.dto.BoletoDTO;
import pe.edu.upeu.lp2_gp2.itinerario.dto.SegmentoDTO;
import pe.edu.upeu.lp2_gp2.itinerario.dto.SeguimientoClienteDTO;
import pe.edu.upeu.lp2_gp2.itinerario.entity.*;
import pe.edu.upeu.lp2_gp2.itinerario.repository.BoletoAereoRepository;
import pe.edu.upeu.lp2_gp2.itinerario.repository.ReservaMasterRepository;
import pe.edu.upeu.lp2_gp2.itinerario.repository.SegmentoOperativoRepository;
import pe.edu.upeu.lp2_gp2.services.IBoletaService;

import java.time.LocalDate;
import java.util.List;

/**
 * Portal del pasajero (ROL_CLIENTE): consulta pasiva de SU reserva.
 * Toda lectura comprueba que la reserva sea del cliente autenticado (si no, 403).
 */
@Service
public class SeguimientoClienteService {
    private final ReservaMasterRepository masters;
    private final SegmentoOperativoRepository segmentos;
    private final BoletoAereoRepository boletos;
    private final ContextoReservas comercial;
    private final ItinerarioVentasService ventas;
    private final IBoletaService boleta;

    public SeguimientoClienteService(ReservaMasterRepository masters, SegmentoOperativoRepository segmentos,
                                     BoletoAereoRepository boletos, ContextoReservas comercial,
                                     ItinerarioVentasService ventas, IBoletaService boleta) {
        this.masters = masters;
        this.segmentos = segmentos;
        this.boletos = boletos;
        this.comercial = comercial;
        this.ventas = ventas;
        this.boleta = boleta;
    }

    @Transactional(readOnly = true)
    public SeguimientoClienteDTO seguimiento(Long reservaId, Long clienteId) {
        ContextoReservas.Comercial c = propia(reservaId, clienteId);
        String paquete = c.paquete() == null ? "—" : c.paquete().getNombre();
        ReservaMaster m = masters.findById(reservaId).orElse(null);
        if (m == null) {
            return new SeguimientoClienteDTO(reservaId, paquete, null, null,
                    "La agencia aún está preparando tu itinerario.", List.of(), List.of());
        }
        List<SegmentoOperativo> ss = segmentos.deReserva(reservaId);
        return new SeguimientoClienteDTO(reservaId, paquete, m.getEstadoLogistico().name(), m.getHotel().getNombre(),
                mensaje(ss), ss.stream().map(SegmentoDTO::desde).toList(),
                boletos.findByReservaMasterReservaIdOrderByTipoVueloAsc(reservaId).stream().map(BoletoDTO::desde).toList());
    }

    @Transactional(readOnly = true)
    public ItinerarioVentasService.Archivo pdf(Long boletoId, Long clienteId) {
        BoletoAereo b = boletos.findById(boletoId)
                .orElseThrow(() -> new ResourceNotFoundException("Boleto no encontrado con Id: " + boletoId));
        propia(b.getReservaMaster().getReservaId(), clienteId);
        return ventas.archivo(b);
    }

    /** Comprobante de la reserva (la misma boleta HTML que envía la agencia) */
    @Transactional(readOnly = true)
    public String comprobante(Long reservaId, Long clienteId) {
        propia(reservaId, clienteId);
        return boleta.generarHtml(reservaId);
    }

    private ContextoReservas.Comercial propia(Long reservaId, Long clienteId) {
        ContextoReservas.Comercial c = comercial.de(reservaId);
        if (clienteId == null || !clienteId.equals(c.reserva().getClienteId())) {
            throw new AccesoDenegadoException("Esta reserva no pertenece a tu cuenta.");
        }
        return c;
    }

    // "¿Mi chofer ya viene?": mensaje del servicio de hoy que esté en marcha
    private static String mensaje(List<SegmentoOperativo> ss) {
        LocalDate hoy = LocalDate.now();
        for (SegmentoOperativo s : ss) {
            if (!s.getFecha().equals(hoy) || !s.getTipo().esOperable() || s.getTipo().esFinal(s.getEstado())) continue;
            String placa = s.getVehiculo() == null ? "" : " (placa " + s.getVehiculo().getPlaca() + ")";
            switch (s.getEstado()) {
                case EN_AEROPUERTO: return "Tu transfer" + placa + " ya te espera en el aeropuerto.";
                case PASAJERO_ABORDADO: case EN_TRANSITO_A_HOTEL: return "Vas camino a tu hotel" + placa + ".";
                case RECOGIENDO_HOTEL: return "Tu chofer" + placa + " está en camino a tu hotel para recogerte.";
                case RUMBO_AL_DESTINO: return "Van rumbo a " + s.getDestino().getNombre() + ".";
                case EN_ATRACTIVO: return "Están en " + s.getDestino().getNombre() + ".";
                case RETORNANDO: return "Regresando a tu hotel" + placa + ".";
                case EN_TRANSITO_A_AEROPUERTO: return "Vas camino al aeropuerto" + placa + ".";
                case PROGRAMADO: return s.asignado()
                        ? "Hoy " + s.getHoraInicio().toLocalTime() + ": " + s.getTipo().name().replace('_', ' ').toLowerCase() + placa + "."
                        : "Hoy " + s.getHoraInicio().toLocalTime() + ": la agencia está asignando tu movilidad.";
                default: break;
            }
        }
        return null;
    }
}
