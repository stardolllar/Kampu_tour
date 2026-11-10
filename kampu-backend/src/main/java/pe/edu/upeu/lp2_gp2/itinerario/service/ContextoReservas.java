package pe.edu.upeu.lp2_gp2.itinerario.service;

import org.springframework.stereotype.Component;
import pe.edu.upeu.lp2_gp2.entity.*;
import pe.edu.upeu.lp2_gp2.exception.ResourceNotFoundException;
import pe.edu.upeu.lp2_gp2.repository.*;

import java.util.*;

/**
 * Lectura del lado comercial que necesita el motor de itinerarios (reserva, cliente,
 * salida, paquete y pasajeros) usando los repositorios existentes, sin modificarlos.
 */
@Component
public class ContextoReservas {
    public record Comercial(Reserva reserva, Cliente cliente, SalidaProgramada salida, PaqueteTour paquete, int pax) {
    }

    private final ReservaRepository reservas;
    private final ClienteRepository clientes;
    private final DetalleReservaRepository detalles;
    private final SalidaProgramadaRepository salidas;
    private final PaqueteTourRepository paquetes;
    private final PasajeroReservaRepository pasajeros;

    public ContextoReservas(ReservaRepository reservas, ClienteRepository clientes, DetalleReservaRepository detalles,
                            SalidaProgramadaRepository salidas, PaqueteTourRepository paquetes,
                            PasajeroReservaRepository pasajeros) {
        this.reservas = reservas;
        this.clientes = clientes;
        this.detalles = detalles;
        this.salidas = salidas;
        this.paquetes = paquetes;
        this.pasajeros = pasajeros;
    }

    public Comercial de(Long reservaId) {
        Reserva r = reservas.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con Id: " + reservaId));
        return de(r);
    }

    public Comercial de(Reserva r) {
        Cliente c = clientes.findById(r.getClienteId()).orElse(null);
        List<DetalleReserva> ds = detalles.findByReservaIdOrderByDetalleIdAsc(r.getReservaId());
        DetalleReserva d = ds.isEmpty() ? null : ds.get(0);
        SalidaProgramada s = d == null ? null : salidas.findById(d.getSalidaId()).orElse(null);
        PaqueteTour p = s == null ? null : paquetes.findById(s.getPaqueteId()).orElse(null);
        int pax = ds.stream().mapToInt(DetalleReserva::getCantidad).sum();
        return new Comercial(r, c, s, p, pax);
    }

    public List<Reserva> todas() {
        return reservas.findAllByOrderByReservaIdAsc();
    }

    /** Nombres de los pasajeros de cada reserva (reservaId -> nombres) */
    public Map<Long, List<String>> pasajeros(Collection<Long> reservaIds) {
        Map<Long, Long> reservaDeDetalle = new HashMap<>();
        for (Long id : reservaIds) {
            for (DetalleReserva d : detalles.findByReservaIdOrderByDetalleIdAsc(id)) reservaDeDetalle.put(d.getDetalleId(), id);
        }
        Map<Long, List<String>> r = new HashMap<>();
        if (reservaDeDetalle.isEmpty()) return r;
        for (PasajeroReserva p : pasajeros.findByDetalleIdInOrderByPasajeroIdAsc(reservaDeDetalle.keySet())) {
            r.computeIfAbsent(reservaDeDetalle.get(p.getDetalleId()), k -> new ArrayList<>()).add(p.getNombreCompleto());
        }
        return r;
    }

    public String nombreCliente(Long reservaId) {
        return reservas.findById(reservaId).flatMap(r -> clientes.findById(r.getClienteId()))
                .map(Cliente::getNombreCompleto).orElse("—");
    }
}
