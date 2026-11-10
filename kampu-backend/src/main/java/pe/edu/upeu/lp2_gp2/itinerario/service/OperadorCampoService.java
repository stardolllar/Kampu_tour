package pe.edu.upeu.lp2_gp2.itinerario.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.lp2_gp2.entity.Lugar;
import pe.edu.upeu.lp2_gp2.exception.BusinessException;
import pe.edu.upeu.lp2_gp2.exception.ResourceNotFoundException;
import pe.edu.upeu.lp2_gp2.itinerario.dto.EstadoRequestDTO;
import pe.edu.upeu.lp2_gp2.itinerario.dto.ParadaDTO;
import pe.edu.upeu.lp2_gp2.itinerario.dto.ViajeOperadorDTO;
import pe.edu.upeu.lp2_gp2.itinerario.entity.*;
import pe.edu.upeu.lp2_gp2.itinerario.repository.SegmentoOperativoRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * App del operador en campo (ROL_OPERADOR: chofer / guía).
 *  - Solo ve los viajes que el Admin le asignó para HOY.
 *  - Manifiesto de pasajeros con la ruta de hoteles optimizada (OSRM o estimada).
 *  - Avanza el estado del viaje completo (vehículo) o de un solo segmento (una reserva),
 *    SIEMPRE al siguiente paso de la secuencia del tipo (no se saltan ni retroceden pasos).
 * La propiedad del viaje la valida @PreAuthorize en el controlador y se vuelve a validar
 * aquí (defensa en profundidad) con los segmentos bloqueados.
 */
@Service
public class OperadorCampoService {
    private final SegmentoOperativoRepository segmentos;
    private final RutaOptimizadaService rutas;
    private final ContextoReservas comercial;

    public OperadorCampoService(SegmentoOperativoRepository segmentos, RutaOptimizadaService rutas,
                                ContextoReservas comercial) {
        this.segmentos = segmentos;
        this.rutas = rutas;
        this.comercial = comercial;
    }

    @Transactional(readOnly = true)
    public List<ViajeOperadorDTO> jornada(Long operadorId) {
        Map<String, List<SegmentoOperativo>> grupos = segmentos.jornada(operadorId, LocalDate.now()).stream()
                .collect(Collectors.groupingBy(SegmentoOperativo::getGrupoCodigo, LinkedHashMap::new, Collectors.toList()));
        return grupos.values().stream().map(this::viaje).toList();
    }

    /** Avanza TODO el viaje (todos sus pasajeros) al siguiente estado */
    @Transactional
    public ViajeOperadorDTO avanzarGrupo(String codigo, EstadoRequestDTO req, Long operadorId) {
        List<SegmentoOperativo> lista = segmentos.bloquearGrupo(codigo);
        if (lista.isEmpty()) throw new ResourceNotFoundException("No existe el viaje " + codigo + ".");
        exigirPropios(lista, operadorId);
        TipoSegmento tipo = lista.get(0).getTipo();
        EstadoSegmento actual = menosAvanzado(lista);
        EstadoSegmento esperado = tipo.siguiente(actual);
        validarPaso(tipo, actual, esperado, req.estado());
        for (SegmentoOperativo s : lista) {
            if (tipo.orden(s.getEstado()) < tipo.orden(req.estado())) aplicar(s, req);
        }
        segmentos.flush();
        return viaje(lista);
    }

    /** Avanza un solo segmento (p. ej. una familia ya abordó o ya se dejó en su hotel) */
    @Transactional
    public ViajeOperadorDTO avanzarSegmento(Long segmentoId, EstadoRequestDTO req, Long operadorId) {
        List<SegmentoOperativo> uno = segmentos.bloquear(List.of(segmentoId));
        if (uno.isEmpty()) throw new ResourceNotFoundException("Segmento no encontrado con Id: " + segmentoId);
        SegmentoOperativo s = uno.get(0);
        exigirPropios(uno, operadorId);
        validarPaso(s.getTipo(), s.getEstado(), s.getTipo().siguiente(s.getEstado()), req.estado());
        aplicar(s, req);
        segmentos.flush();
        return viaje(segmentos.findByGrupoCodigoOrderByHoraInicioAscSegmentoIdAsc(s.getGrupoCodigo()));
    }

    // ------------------------------------------------------------------ reglas

    private static void exigirPropios(List<SegmentoOperativo> lista, Long operadorId) {
        for (SegmentoOperativo s : lista) {
            if (s.getOperador() == null || !s.getOperador().getUsuarioId().equals(operadorId)) {
                throw new AccessDeniedException("Este viaje no está asignado a ti.");
            }
            if (!s.getFecha().equals(LocalDate.now())) {
                throw new BusinessException("Solo puedes actualizar los viajes de hoy (este es del " + s.getFecha() + ").");
            }
        }
    }

    private static void validarPaso(TipoSegmento tipo, EstadoSegmento actual, EstadoSegmento esperado, EstadoSegmento pedido) {
        if (esperado == null) throw new BusinessException("El servicio ya terminó (" + actual + ").");
        if (pedido != esperado) {
            throw new BusinessException("Después de " + actual + " sigue " + esperado + " (no " + pedido + "). Secuencia de "
                    + tipo + ": " + tipo.flujo());
        }
    }

    private static void aplicar(SegmentoOperativo s, EstadoRequestDTO req) {
        s.cambiarEstado(req.estado());
        if (req.observacion() != null && !req.observacion().isBlank()) s.setObservacion(req.observacion().trim());
        // Estado logístico de la reserva master
        ReservaMaster m = s.getReservaMaster();
        if (m.getEstadoLogistico() == EstadoLogistico.CONFIRMADO_CON_VUELO || m.getEstadoLogistico() == EstadoLogistico.VUELO_PENDIENTE) {
            m.setEstadoLogistico(EstadoLogistico.EN_CURSO);
        }
        if (s.getTipo() == TipoSegmento.TRANSFER_OUT && s.getTipo().esFinal(s.getEstado())) {
            m.setEstadoLogistico(EstadoLogistico.FINALIZADO);
        }
        m.tocar();
    }

    private static EstadoSegmento menosAvanzado(List<SegmentoOperativo> lista) {
        TipoSegmento t = lista.get(0).getTipo();
        return lista.stream().map(SegmentoOperativo::getEstado).min(Comparator.comparingInt(t::orden)).orElseThrow();
    }

    // ------------------------------------------------------------------ manifiesto y ruta

    private ViajeOperadorDTO viaje(List<SegmentoOperativo> lista) {
        SegmentoOperativo p = lista.get(0);
        TipoSegmento tipo = p.getTipo();
        EstadoSegmento estado = menosAvanzado(lista);
        EstadoSegmento siguiente = tipo.siguiente(estado);
        Map<Long, List<String>> pasajeros = comercial.pasajeros(
                lista.stream().map(s -> s.getReservaMaster().getReservaId()).collect(Collectors.toSet()));

        List<ViajeOperadorDTO.ManifiestoDTO> manifiesto = new ArrayList<>();
        for (SegmentoOperativo s : lista) {
            Long rid = s.getReservaMaster().getReservaId();
            EstadoSegmento sig = tipo.siguiente(s.getEstado());
            manifiesto.add(new ViajeOperadorDTO.ManifiestoDTO(s.getSegmentoId(), rid, comercial.nombreCliente(rid),
                    hotelDe(s).getNombre(), s.getPax(), pasajeros.getOrDefault(rid, List.of()),
                    s.getBoleto() == null ? null : s.getBoleto().vuelo() + " · " + s.getBoleto().horaClave().toLocalTime(),
                    s.getEstado().name(), sig == null ? null : sig.name()));
        }

        // Ruta: Transfer In aeropuerto -> hoteles; Tour hoteles -> atractivo; Transfer Out hoteles -> aeropuerto
        List<Lugar> hoteles = lista.stream().map(OperadorCampoService::hotelDe).toList();
        RutaOptimizadaService.Resultado r = switch (tipo) {
            case TRANSFER_IN -> rutas.optimizar(p.getOrigen(), hoteles, null);
            default -> rutas.optimizar(null, hoteles, p.getDestino());
        };
        List<ParadaDTO> paradas = new ArrayList<>();
        for (int i = 0; i < r.orden().size(); i++) {
            Lugar l = r.orden().get(i);
            List<SegmentoOperativo> aqui = lista.stream().filter(s -> hotelDe(s).getLugarId().equals(l.getLugarId())).toList();
            String tipoParada = !aqui.isEmpty() ? (tipo == TipoSegmento.TRANSFER_IN ? "ENTREGA" : "RECOJO")
                    : (i == 0 ? "INICIO" : "DESTINO");
            paradas.add(new ParadaDTO(i + 1, tipoParada, l.getLugarId(), l.getNombre(), l.getLatitud(), l.getLongitud(),
                    r.minutosTramo().get(i), aqui.stream().map(SegmentoOperativo::getSegmentoId).toList(),
                    aqui.stream().flatMap(s -> pasajeros.getOrDefault(s.getReservaMaster().getReservaId(), List.of()).stream()).toList()));
        }
        List<String> vuelos = lista.stream().map(SegmentoOperativo::getBoleto).filter(Objects::nonNull)
                .map(b -> b.vuelo() + " " + b.horaClave().toLocalTime()).distinct().toList();
        return new ViajeOperadorDTO(p.getGrupoCodigo(), tipo.name(), lista.stream().map(SegmentoOperativo::getHoraInicio)
                .min(Comparator.naturalOrder()).orElseThrow(), p.getVehiculo() == null ? null : p.getVehiculo().getPlaca(),
                p.getVehiculo() == null ? null : p.getVehiculo().getCapacidadPax(), estado.name(),
                siguiente == null ? null : siguiente.name(), tipo.orden(estado), tipo.flujo().size() - 1,
                lista.stream().mapToInt(SegmentoOperativo::getPax).sum(), vuelos, r.fuente(), r.total(), paradas, manifiesto);
    }

    // Hotel del pasajero en ese segmento
    private static Lugar hotelDe(SegmentoOperativo s) {
        return s.getTipo() == TipoSegmento.TRANSFER_IN ? s.getDestino() : s.getOrigen();
    }
}
