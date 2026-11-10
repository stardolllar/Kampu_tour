package pe.edu.upeu.lp2_gp2.services;

import jakarta.persistence.EntityManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.lp2_gp2.dto.PasajeroRequestDTO;
import pe.edu.upeu.lp2_gp2.dto.PasajeroReservaDTO;
import pe.edu.upeu.lp2_gp2.dto.Reglas;
import pe.edu.upeu.lp2_gp2.dto.ReservaDTO;
import pe.edu.upeu.lp2_gp2.dto.ReservaRequestDTO;
import pe.edu.upeu.lp2_gp2.entity.*;
import pe.edu.upeu.lp2_gp2.exception.BusinessException;
import pe.edu.upeu.lp2_gp2.exception.ResourceNotFoundException;
import pe.edu.upeu.lp2_gp2.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reservas v3: cabecera (RESERVAS) + detalle por salida (DETALLE_RESERVA) + pasajeros del detalle.
 * Los triggers de la base de datos hacen el trabajo pesado:
 *  - trg_detalle_reserva_biud: valida la salida, descuenta cupos, copia el precio base
 *    y calcula RESERVAS.monto_total (y, si la salida está CONFIRMADA, que el transporte alcance).
 *  - trg_pasajeros_cupo: no más pasajeros que la cantidad del detalle.
 *  - trg_reserva_estado_bu: al ANULAR devuelve los cupos; una anulada no se reactiva.
 */
@Service
public class ReservaServiceImpl implements IReservaService {
    private final ReservaRepository reservaRepository;
    private final DetalleReservaRepository detalleRepository;
    private final PasajeroReservaRepository pasajeroRepository;
    private final VistaReservaResumenRepository resumenRepository;
    private final SalidaProgramadaRepository salidaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioSistemaRepository usuarioRepository;
    private final PagoRepository pagoRepository;
    private final ApplicationEventPublisher eventos;
    private final EntityManager em;

    public ReservaServiceImpl(ReservaRepository reservaRepository, DetalleReservaRepository detalleRepository,
                              PasajeroReservaRepository pasajeroRepository, VistaReservaResumenRepository resumenRepository,
                              SalidaProgramadaRepository salidaRepository, ClienteRepository clienteRepository,
                              UsuarioSistemaRepository usuarioRepository, PagoRepository pagoRepository,
                              ApplicationEventPublisher eventos, EntityManager em) {
        this.reservaRepository = reservaRepository;
        this.detalleRepository = detalleRepository;
        this.pasajeroRepository = pasajeroRepository;
        this.resumenRepository = resumenRepository;
        this.salidaRepository = salidaRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.pagoRepository = pagoRepository;
        this.eventos = eventos;
        this.em = em;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaDTO> listarTodo() {
        Map<Long, DetalleReserva> detalles = new HashMap<>();
        for (DetalleReserva d : detalleRepository.findAllByOrderByDetalleIdAsc()) detalles.putIfAbsent(d.getReservaId(), d);
        Map<Long, VistaReservaResumen> resumen = resumenRepository.findAll().stream()
                .collect(Collectors.toMap(VistaReservaResumen::getReservaId, Function.identity()));
        return reservaRepository.findAllByOrderByReservaIdAsc().stream()
                .map(r -> ReservaDTO.desde(r, detalles.get(r.getReservaId()), resumen.get(r.getReservaId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaDTO buscarPorId(Long id) {
        return dto(obtener(id));
    }

    @Override
    @Transactional
    public ReservaDTO crear(ReservaRequestDTO dto) {
        for (PasajeroRequestDTO p : dto.pasajeros()) {
            if (!Reglas.documentoValido(p.tipoDocumento(), p.numeroDocumento())) throw new BusinessException(Reglas.MSG_DOCUMENTO);
            if (p.fechaNacimiento() != null && p.fechaNacimiento().isBefore(Reglas.FECHA_MINIMA)) {
                throw new BusinessException("La fecha de nacimiento no puede ser anterior a 1900.");
            }
        }
        validarCliente(dto);
        SalidaProgramada salida = salidaRepository.findById(dto.salidaId())
                .orElseThrow(() -> new BusinessException("Selecciona una salida."));
        LocalDate hoy = LocalDate.now();
        // El trigger valida estado y cupos; que la salida no haya iniciado es regla de la aplicación
        if (salida.getFechaInicio().isBefore(hoy)) throw new BusinessException("La salida ya inició.");
        Set<String> documentos = new HashSet<>();
        for (PasajeroRequestDTO p : dto.pasajeros()) {
            if (p.fechaNacimiento() != null && !p.fechaNacimiento().isBefore(hoy)) {
                throw new BusinessException("La fecha de nacimiento de " + p.nombreCompleto().trim() + " debe ser anterior a hoy.");
            }
            if (!documentos.add(p.tipoDocumento() + ":" + p.numeroDocumento().trim().toUpperCase())) {
                throw new BusinessException("El documento " + p.numeroDocumento().trim() + " está repetido en la reserva.");
            }
        }

        // Un pasajero no viaja dos veces en la misma salida (trg_pasajeros_doc_salida)
        for (PasajeroRequestDTO p : dto.pasajeros()) {
            if (pasajeroRepository.contarEnSalida(salida.getSalidaId(), p.tipoDocumento(), p.numeroDocumento().trim()) > 0) {
                throw new BusinessException("El pasajero " + p.nombreCompleto().trim() + " (" + p.tipoDocumento() + " "
                        + p.numeroDocumento().trim() + ") ya tiene una reserva activa en esta salida.");
            }
        }

        // 1) Cabecera (monto_total empieza en 0)
        Reserva r = new Reserva();
        r.setClienteId(dto.clienteId());
        if (dto.clienteId() == null) {
            r.setContactoNombre(dto.contacto().nombre().trim());
            r.setContactoEmail(dto.contacto().email().trim().toLowerCase());
            r.setContactoTelefono(dto.contacto().telefono().trim());
        }
        r.setFechaReserva(LocalDateTime.now());
        r.setMontoPagado(BigDecimal.ZERO);
        r.setEstadoReserva("PENDIENTE");
        r.setEstadoPago("PENDIENTE");
        r.setObservaciones(dto.observaciones() == null || dto.observaciones().isBlank() ? null : dto.observaciones().trim());
        Reserva guardada = reservaRepository.saveAndFlush(r);

        // 2) Detalle: trg_detalle_reserva_biud descuenta cupos, congela el precio y suma monto_total
        DetalleReserva d = new DetalleReserva();
        d.setReservaId(guardada.getReservaId());
        d.setSalidaId(salida.getSalidaId());
        d.setCantidad(dto.pasajeros().size());
        d.setPrecioUnitario(null);   // el trigger copia PAQUETES_TOUR.precio_base
        d.setDescuento(BigDecimal.ZERO);
        DetalleReserva detalle = detalleRepository.saveAndFlush(d);

        // 3) Pasajeros del detalle (trg_pasajeros_cupo)
        for (PasajeroRequestDTO p : dto.pasajeros()) {
            PasajeroReserva pr = new PasajeroReserva();
            pr.setDetalleId(detalle.getDetalleId());
            pr.setNombreCompleto(p.nombreCompleto().trim());
            pr.setTipoDocumento(p.tipoDocumento());
            pr.setNumeroDocumento(p.numeroDocumento().trim());
            pr.setNacionalidad(p.nacionalidad() == null || p.nacionalidad().isBlank() ? null : p.nacionalidad().trim());
            pr.setFechaNacimiento(p.fechaNacimiento());
            pasajeroRepository.save(pr);
        }
        pasajeroRepository.flush();

        em.refresh(guardada);   // monto_total
        em.refresh(detalle);    // precio_unitario y subtotal
        return dto(guardada);
    }

    @Override
    @Transactional
    public ReservaDTO confirmar(Long id) {
        Reserva r = obtener(id);
        if (!"PENDIENTE".equals(r.getEstadoReserva())) throw new BusinessException("Solo se confirman reservas pendientes.");
        r.setEstadoReserva("CONFIRMADA");
        reservaRepository.saveAndFlush(r);
        eventos.publishEvent(new BoletaEvento(id, "reserva confirmada"));
        return dto(r);
    }

    @Override
    @Transactional
    public ReservaDTO completar(Long id) {
        Reserva r = obtener(id);
        if (!"CONFIRMADA".equals(r.getEstadoReserva())) throw new BusinessException("Solo se completan reservas confirmadas.");
        r.setEstadoReserva("COMPLETADA");
        reservaRepository.saveAndFlush(r);
        return dto(r);
    }

    @Override
    @Transactional
    public ReservaDTO anular(Long id, Long usuarioId, String formaReembolso) {
        Reserva r = reservaRepository.findByIdParaActualizar(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con Id: " + id));
        if (!"PENDIENTE".equals(r.getEstadoReserva()) && !"CONFIRMADA".equals(r.getEstadoReserva())) {
            throw new BusinessException("Solo se anulan reservas pendientes o confirmadas.");
        }
        // trg_reserva_estado_bu exige el usuario que anula (ORA-20031)
        if (usuarioId == null) throw new BusinessException("Indica el usuario que anula la reserva (inicia sesión de nuevo).");
        if (!usuarioRepository.existsById(usuarioId)) throw new BusinessException("El usuario que anula no existe.");
        boolean hayReembolso = r.getMontoPagado() != null && r.getMontoPagado().signum() > 0;
        String forma = formaReembolso == null || formaReembolso.isBlank() ? null : formaReembolso.trim();
        if (hayReembolso && forma == null) throw new BusinessException("Indica la forma de reembolso.");

        for (Pago p : pagoRepository.findByReservaIdAndEstadoTransaccion(id, "APROBADO")) {
            p.setEstadoTransaccion("REEMBOLSADO");
            pagoRepository.save(p);
        }
        r.setEstadoReserva("ANULADA");
        r.setMontoPagado(BigDecimal.ZERO);
        r.setEstadoPago(hayReembolso ? "REEMBOLSADO" : "PENDIENTE");
        r.setFormaReembolso(hayReembolso ? forma : null);
        r.setUsuarioAnulaId(usuarioId);
        // Los cupos los devuelve trg_reserva_estado_bu
        reservaRepository.saveAndFlush(r);
        return dto(r);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PasajeroReservaDTO> listarPasajeros(Long reservaId) {
        Map<Long, Long> reservaDeDetalle = new HashMap<>();
        List<PasajeroReserva> lista;
        if (reservaId == null) {
            for (DetalleReserva d : detalleRepository.findAll()) reservaDeDetalle.put(d.getDetalleId(), d.getReservaId());
            lista = pasajeroRepository.findAllByOrderByPasajeroIdAsc();
        } else {
            for (DetalleReserva d : detalleRepository.findByReservaIdOrderByDetalleIdAsc(reservaId)) {
                reservaDeDetalle.put(d.getDetalleId(), d.getReservaId());
            }
            lista = reservaDeDetalle.isEmpty() ? List.of()
                    : pasajeroRepository.findByDetalleIdInOrderByPasajeroIdAsc(reservaDeDetalle.keySet());
        }
        return lista.stream().map(p -> PasajeroReservaDTO.desde(p, reservaDeDetalle.get(p.getDetalleId()))).toList();
    }

    // Exactamente uno: un cliente registrado o los datos de contacto del invitado.
    private void validarCliente(ReservaRequestDTO dto) {
        if (dto.clienteId() != null && dto.contacto() != null) {
            throw new BusinessException("Elige un cliente registrado o ingresa los datos del invitado, no ambos.");
        }
        if (dto.clienteId() == null && dto.contacto() == null) {
            throw new BusinessException("Selecciona un cliente o ingresa los datos del invitado.");
        }
        if (dto.clienteId() != null && !clienteRepository.existsById(dto.clienteId())) {
            throw new BusinessException("El cliente seleccionado no existe.");
        }
        if (dto.contacto() != null) {
            String errorTelefono = Reglas.errorTelefonoPais(dto.contacto().telefono());
            if (errorTelefono != null) throw new BusinessException(errorTelefono);
        }
    }

    private ReservaDTO dto(Reserva r) {
        List<DetalleReserva> detalles = detalleRepository.findByReservaIdOrderByDetalleIdAsc(r.getReservaId());
        return ReservaDTO.desde(r, detalles.isEmpty() ? null : detalles.get(0),
                resumenRepository.findById(r.getReservaId()).orElse(null));
    }

    private Reserva obtener(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con Id: " + id));
    }
}
