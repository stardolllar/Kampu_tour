package pe.edu.upeu.lp2_gp2.itinerario.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.lp2_gp2.entity.Lugar;
import pe.edu.upeu.lp2_gp2.entity.Reserva;
import pe.edu.upeu.lp2_gp2.exception.BusinessException;
import pe.edu.upeu.lp2_gp2.exception.ResourceNotFoundException;
import pe.edu.upeu.lp2_gp2.itinerario.almacenamiento.ArchivoGuardado;
import pe.edu.upeu.lp2_gp2.itinerario.almacenamiento.FileStorageService;
import pe.edu.upeu.lp2_gp2.itinerario.dto.*;
import pe.edu.upeu.lp2_gp2.itinerario.entity.*;
import pe.edu.upeu.lp2_gp2.itinerario.repository.BoletoAereoRepository;
import pe.edu.upeu.lp2_gp2.itinerario.repository.ReservaMasterRepository;
import pe.edu.upeu.lp2_gp2.itinerario.repository.SegmentoOperativoRepository;
import pe.edu.upeu.lp2_gp2.repository.LugarRepository;
import pe.edu.upeu.lp2_gp2.seguridad.SesionActual;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Módulo de Booking y Vuelos (ROL_VENTAS / ROL_ADMIN).
 *
 * Flujo manual de vuelos: el vuelo se compra fuera (LATAM / Sky / JetSMART); Ventas registra
 * PNR, aerolínea, horarios y sube el PDF. Al guardarlo:
 *   - la reserva master pasa de VUELO_PENDIENTE a CONFIRMADO_CON_VUELO;
 *   - LLEGADA genera o actualiza el TRANSFER_IN (aeropuerto -> hotel) a la hora de llegada;
 *   - SALIDA genera o actualiza el TRANSFER_OUT (hotel -> aeropuerto) 3 h antes del despegue.
 * Si el transfer ya estaba asignado y cambia la hora, se libera para que el Admin lo
 * vuelva a consolidar; si ya empezó, el vuelo no se puede cambiar.
 */
@Service
public class ItinerarioVentasService {
    static final long HORAS_ANTES_VUELO = 3;              // recojo en hotel antes del despegue
    static final LocalTime CHECK_IN_HOTEL = LocalTime.of(14, 0);

    private final ReservaMasterRepository masters;
    private final BoletoAereoRepository boletos;
    private final SegmentoOperativoRepository segmentos;
    private final LugarRepository lugares;
    private final ContextoReservas comercial;
    private final FileStorageService almacenamiento;

    public ItinerarioVentasService(ReservaMasterRepository masters, BoletoAereoRepository boletos,
                                   SegmentoOperativoRepository segmentos, LugarRepository lugares,
                                   ContextoReservas comercial, FileStorageService almacenamiento) {
        this.masters = masters;
        this.boletos = boletos;
        this.segmentos = segmentos;
        this.lugares = lugares;
        this.comercial = comercial;
        this.almacenamiento = almacenamiento;
    }

    // ------------------------------------------------------------------ consultas

    /** Reservas vigentes con su estado logístico (las que aún no tienen itinerario salen con estado null) */
    @Transactional(readOnly = true)
    public List<ItinerarioDTO> listar() {
        List<ItinerarioDTO> r = new ArrayList<>();
        for (Reserva res : comercial.todas()) {
            if ("ANULADA".equals(res.getEstadoReserva())) continue;
            ReservaMaster m = masters.findById(res.getReservaId()).orElse(null);
            r.add(dto(comercial.de(res), m, false));
        }
        return r;
    }

    @Transactional(readOnly = true)
    public ItinerarioDTO detalle(Long reservaId) {
        return dto(comercial.de(reservaId), masters.findById(reservaId).orElse(null), true);
    }

    // ------------------------------------------------------------------ itinerario

    /** Abre (o actualiza) el itinerario: hotel, aeropuerto, alojamiento y tours por día */
    @Transactional
    public ItinerarioDTO guardarItinerario(Long reservaId, ItinerarioRequestDTO req) {
        ContextoReservas.Comercial c = comercial.de(reservaId);
        exigirVigente(c.reserva());
        if (c.salida() == null) throw new BusinessException("La reserva no tiene salida programada.");
        Lugar hotel = lugar(req.hotelId(), "HOTEL", "El hotel");
        Lugar aeropuerto = lugar(req.aeropuertoId(), "AEROPUERTO", "El aeropuerto");

        ReservaMaster m = masters.bloquear(reservaId).orElse(null);
        if (m == null) {
            m = masters.save(new ReservaMaster(reservaId, hotel, aeropuerto, SesionActual.usuario()));
        } else {
            m.setHotel(hotel);
            m.setAeropuerto(aeropuerto);
            m.tocar();
        }
        final ReservaMaster master = m;

        // Hotel y aeropuerto nuevos en los segmentos que aún no empiezan
        for (SegmentoOperativo s : segmentos.deReserva(reservaId)) {
            if (s.getEstado() != EstadoSegmento.PROGRAMADO) continue;
            switch (s.getTipo()) {
                case TRANSFER_IN -> { s.setOrigen(aeropuerto); s.setDestino(hotel); }
                case TRANSFER_OUT -> { s.setOrigen(hotel); s.setDestino(aeropuerto); }
                case TOUR_DIARIO -> s.setOrigen(hotel);
                case ALOJAMIENTO -> { s.setOrigen(hotel); s.setDestino(hotel); }
            }
            s.setPax(c.pax());
        }

        // Alojamiento: check-in el día de inicio
        LocalDateTime checkIn = c.salida().getFechaInicio().atTime(CHECK_IN_HOTEL);
        if (segmentos.findFirstByReservaMasterReservaIdAndTipo(reservaId, TipoSegmento.ALOJAMIENTO).isEmpty()) {
            segmentos.save(new SegmentoOperativo(master, TipoSegmento.ALOJAMIENTO, checkIn, hotel, hotel, c.pax()));
        }

        if (req.tours() != null) sincronizarTours(master, c, hotel, req.tours());
        return detalle(reservaId);
    }

    private void sincronizarTours(ReservaMaster m, ContextoReservas.Comercial c, Lugar hotel,
                                  List<ItinerarioRequestDTO.TourDiaDTO> tours) {
        LocalDate ini = c.salida().getFechaInicio(), fin = c.salida().getFechaFin();
        Map<LocalDate, ItinerarioRequestDTO.TourDiaDTO> porDia = new HashMap<>();
        for (var t : tours) {
            LocalDate dia = t.horaRecojo().toLocalDate();
            if (dia.isBefore(ini) || dia.isAfter(fin)) {
                throw new BusinessException("El tour del " + dia + " está fuera del paquete (" + ini + " a " + fin + ").");
            }
            if (porDia.put(dia, t) != null) throw new BusinessException("Hay dos tours el " + dia + ": deja uno por día.");
        }
        List<SegmentoOperativo> actuales = segmentos.findByReservaMasterReservaIdAndTipo(m.getReservaId(), TipoSegmento.TOUR_DIARIO);
        for (SegmentoOperativo s : actuales) {
            var t = porDia.remove(s.getFecha());
            boolean cambia = t == null || !t.destinoId().equals(s.getDestino().getLugarId()) || !t.horaRecojo().equals(s.getHoraInicio());
            if (!cambia) continue;
            if (s.getEstado() != EstadoSegmento.PROGRAMADO) {
                throw new BusinessException("El tour del " + s.getFecha() + " ya está en curso: no se puede cambiar.");
            }
            if (t == null) {
                segmentos.delete(s);
            } else {
                s.setDestino(lugar(t.destinoId(), "ATRACTIVO", "El destino del tour"));
                s.programar(t.horaRecojo());
                if (s.asignado()) { s.liberar(); s.setObservacion("Tour reprogramado: volver a consolidar."); }
            }
        }
        for (var t : porDia.values()) {
            segmentos.save(new SegmentoOperativo(m, TipoSegmento.TOUR_DIARIO, t.horaRecojo(), hotel,
                    lugar(t.destinoId(), "ATRACTIVO", "El destino del tour"), c.pax()));
        }
    }

    // ------------------------------------------------------------------ vuelos

    @Transactional
    public ItinerarioDTO registrarBoleto(Long reservaId, BoletoRequestDTO req, MultipartFile archivo) {
        ContextoReservas.Comercial c = comercial.de(reservaId);
        exigirVigente(c.reserva());
        ReservaMaster m = masters.bloquear(reservaId).orElseThrow(() ->
                new BusinessException("Primero abre el itinerario de la reserva (hotel y aeropuerto)."));
        validarVuelo(req, c);

        BoletoAereo b = boletos.findByReservaMasterReservaIdAndTipoVuelo(reservaId, req.tipoVuelo())
                .orElseGet(() -> new BoletoAereo(m, req.tipoVuelo()));
        boolean tieneArchivo = archivo != null && !archivo.isEmpty();
        if (b.getArchivoPdf() == null && !tieneArchivo) {
            throw new BusinessException("Adjunta el PDF del boarding pass o e-ticket.");
        }

        // Transfer que depende del vuelo: no se cambia si ya está en curso
        TipoSegmento tipoTransfer = req.tipoVuelo() == TipoVuelo.LLEGADA ? TipoSegmento.TRANSFER_IN : TipoSegmento.TRANSFER_OUT;
        SegmentoOperativo transfer = segmentos.findFirstByReservaMasterReservaIdAndTipo(reservaId, tipoTransfer).orElse(null);
        if (transfer != null && transfer.getEstado() != EstadoSegmento.PROGRAMADO) {
            throw new BusinessException("El " + tipoTransfer.name().replace('_', ' ') + " ya está en curso ("
                    + transfer.getEstado() + "): no se puede cambiar el vuelo.");
        }

        b.setAerolinea(req.aerolinea());
        b.setNumeroVuelo(req.numeroVuelo().replace(" ", ""));
        b.setCodigoPnr(req.codigoPnr());
        b.setAeropuertoOrigen(req.aeropuertoOrigen());
        b.setAeropuertoDestino(req.aeropuertoDestino());
        b.setFechaHoraSalida(req.fechaHoraSalida());
        b.setFechaHoraLlegada(req.fechaHoraLlegada());
        b.setRegistradoPor(SesionActual.usuario());

        if (tieneArchivo) {
            ArchivoGuardado nuevo = almacenamiento.guardarPdf(archivo);
            String anterior = b.getArchivoPdf();
            b.adjuntar(nuevo.ruta(), nuevo.nombreOriginal(), nuevo.bytes());
            alTerminar(anterior, nuevo.ruta());
        }
        b = boletos.save(b);

        // Impacto automático: estado de la reserva y transfer
        if (m.getEstadoLogistico() == EstadoLogistico.VUELO_PENDIENTE) m.setEstadoLogistico(EstadoLogistico.CONFIRMADO_CON_VUELO);
        m.tocar();
        LocalDateTime hora = req.tipoVuelo() == TipoVuelo.LLEGADA
                ? req.fechaHoraLlegada() : req.fechaHoraSalida().minusHours(HORAS_ANTES_VUELO);
        if (transfer == null) {
            transfer = req.tipoVuelo() == TipoVuelo.LLEGADA
                    ? new SegmentoOperativo(m, tipoTransfer, hora, m.getAeropuerto(), m.getHotel(), c.pax())
                    : new SegmentoOperativo(m, tipoTransfer, hora, m.getHotel(), m.getAeropuerto(), c.pax());
        } else if (!hora.equals(transfer.getHoraInicio())) {
            transfer.programar(hora);
            if (transfer.asignado()) {
                transfer.liberar();
                transfer.setObservacion("Cambió el vuelo: volver a consolidar en el FIDS.");
            }
        }
        transfer.setBoleto(b);
        segmentos.save(transfer);
        return detalle(reservaId);
    }

    @Transactional(readOnly = true)
    public Archivo pdf(Long boletoId) {
        BoletoAereo b = boletos.findById(boletoId)
                .orElseThrow(() -> new ResourceNotFoundException("Boleto no encontrado con Id: " + boletoId));
        return archivo(b);
    }

    public record Archivo(Resource recurso, String nombre) {
    }

    Archivo archivo(BoletoAereo b) {
        if (b.getArchivoPdf() == null) throw new ResourceNotFoundException("El boleto no tiene PDF adjunto.");
        String nombre = b.getTipoVuelo().name().toLowerCase() + "-" + b.getCodigoPnr() + ".pdf";
        return new Archivo(almacenamiento.cargar(b.getArchivoPdf()), nombre);
    }

    // ------------------------------------------------------------------ apoyo

    private void validarVuelo(BoletoRequestDTO r, ContextoReservas.Comercial c) {
        if (!r.fechaHoraLlegada().isAfter(r.fechaHoraSalida())) {
            throw new BusinessException("La llegada del vuelo debe ser posterior a su salida.");
        }
        if (r.aeropuertoOrigen().equals(r.aeropuertoDestino())) {
            throw new BusinessException("El aeropuerto de origen y el de destino no pueden ser el mismo.");
        }
        if (c.salida() == null) return;
        LocalDate ini = c.salida().getFechaInicio(), fin = c.salida().getFechaFin();
        if (r.tipoVuelo() == TipoVuelo.LLEGADA) {
            LocalDate d = r.fechaHoraLlegada().toLocalDate();
            if (d.isBefore(ini.minusDays(1)) || d.isAfter(fin)) {
                throw new BusinessException("El vuelo de llegada (" + d + ") no coincide con el paquete (" + ini + " a " + fin + ").");
            }
        } else {
            LocalDate d = r.fechaHoraSalida().toLocalDate();
            if (d.isBefore(ini) || d.isAfter(fin.plusDays(1))) {
                throw new BusinessException("El vuelo de regreso (" + d + ") no coincide con el paquete (" + ini + " a " + fin + ").");
            }
        }
    }

    // Archivo nuevo/viejo según termine la transacción: no quedan PDFs huérfanos ni se pierde el anterior
    private void alTerminar(String anterior, String nuevo) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int estado) {
                if (estado == STATUS_COMMITTED) almacenamiento.eliminar(anterior);
                else almacenamiento.eliminar(nuevo);
            }
        });
    }

    private static void exigirVigente(Reserva r) {
        if ("ANULADA".equals(r.getEstadoReserva()) || "COMPLETADA".equals(r.getEstadoReserva())) {
            throw new BusinessException("La reserva está " + r.getEstadoReserva() + ": no admite cambios de itinerario.");
        }
    }

    private Lugar lugar(Long id, String tipo, String campo) {
        Lugar l = lugares.findById(id).orElseThrow(() -> new ResourceNotFoundException(campo + " no existe (Id " + id + ")."));
        if (!tipo.equals(l.getTipoLugar())) {
            throw new BusinessException(campo + " debe ser un lugar de tipo " + tipo + " (\"" + l.getNombre() + "\" es " + l.getTipoLugar() + ").");
        }
        return l;
    }

    private ItinerarioDTO dto(ContextoReservas.Comercial c, ReservaMaster m, boolean completo) {
        Long id = c.reserva().getReservaId();
        List<BoletoDTO> bs = m == null ? List.of()
                : boletos.findByReservaMasterReservaIdOrderByTipoVueloAsc(id).stream().map(BoletoDTO::desde).toList();
        List<SegmentoDTO> ss = m == null || !completo ? List.of()
                : segmentos.deReserva(id).stream().map(SegmentoDTO::desde).toList();
        return new ItinerarioDTO(id, c.cliente() == null ? "—" : c.cliente().getNombreCompleto(),
                c.paquete() == null ? "—" : c.paquete().getNombre(),
                c.salida() == null ? null : c.salida().getFechaInicio(), c.salida() == null ? null : c.salida().getFechaFin(),
                c.pax(), c.reserva().getEstadoReserva(), m == null ? null : m.getEstadoLogistico().name(),
                m == null ? null : m.getHotel().getLugarId(), m == null ? null : m.getHotel().getNombre(),
                m == null ? null : m.getAeropuerto().getLugarId(), m == null ? null : m.getAeropuerto().getNombre(),
                bs, ss);
    }
}
