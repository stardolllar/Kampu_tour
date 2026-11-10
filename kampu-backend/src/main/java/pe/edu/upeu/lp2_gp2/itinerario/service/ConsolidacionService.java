package pe.edu.upeu.lp2_gp2.itinerario.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.lp2_gp2.entity.UsuarioSistema;
import pe.edu.upeu.lp2_gp2.entity.Vehiculo;
import pe.edu.upeu.lp2_gp2.exception.BusinessException;
import pe.edu.upeu.lp2_gp2.exception.ResourceNotFoundException;
import pe.edu.upeu.lp2_gp2.itinerario.dto.*;
import pe.edu.upeu.lp2_gp2.itinerario.entity.EstadoSegmento;
import pe.edu.upeu.lp2_gp2.itinerario.entity.SegmentoOperativo;
import pe.edu.upeu.lp2_gp2.itinerario.entity.TipoSegmento;
import pe.edu.upeu.lp2_gp2.itinerario.repository.SegmentoOperativoRepository;
import pe.edu.upeu.lp2_gp2.repository.UsuarioSistemaRepository;
import pe.edu.upeu.lp2_gp2.repository.VehiculoRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Torre de control (ROL_ADMIN): consolidación cruzada de segmentos de DISTINTAS reservas.
 *
 * Algoritmo (sobre la consulta JPQL paraConsolidar, ya ordenada por tipo y hora):
 *   1. Los segmentos que ya son un viaje (grupo_codigo) se muestran juntos tal cual.
 *   2. Los demás se agrupan por (tipo, lugar clave) -- aeropuerto en transfers, atractivo
 *      en tours -- y dentro de cada uno por VENTANA de tiempo desde el primero del grupo:
 *      60 min en transfers (vuelos que llegan/salen parecido) y 3 h en tours.
 *   Cada grupo sugiere el vehículo mínimo que alcanza para sus pasajeros.
 * Asignar = poner el mismo grupo_codigo, vehículo y operador a los segmentos elegidos.
 */
@Service
public class ConsolidacionService {
    static final Duration VENTANA_TRANSFER = Duration.ofMinutes(60);
    static final Duration VENTANA_TOUR = Duration.ofHours(3);
    static final Duration HOLGURA_CRUCE = Duration.ofHours(2);   // un vehículo/operador no hace dos viajes tan seguidos
    private static final Set<TipoSegmento> OPERABLES = EnumSet.of(TipoSegmento.TRANSFER_IN, TipoSegmento.TOUR_DIARIO, TipoSegmento.TRANSFER_OUT);
    private static final DateTimeFormatter AAAAMMDD = DateTimeFormatter.BASIC_ISO_DATE;

    private final SegmentoOperativoRepository segmentos;
    private final VehiculoRepository vehiculos;
    private final UsuarioSistemaRepository usuarios;

    public ConsolidacionService(SegmentoOperativoRepository segmentos, VehiculoRepository vehiculos,
                                UsuarioSistemaRepository usuarios) {
        this.segmentos = segmentos;
        this.vehiculos = vehiculos;
        this.usuarios = usuarios;
    }

    // ------------------------------------------------------------------ panel

    @Transactional(readOnly = true)
    public TableroFidsDTO tablero(LocalDate fecha, TipoSegmento tipo, Long lugarId) {
        Set<TipoSegmento> tipos = tipo == null ? OPERABLES : EnumSet.of(tipo);
        if (tipo != null && !tipo.esOperable()) throw new BusinessException("El alojamiento no se consolida en el FIDS.");
        List<SegmentoFidsFila> filas = segmentos.paraConsolidar(fecha, tipos, lugarId);

        List<GrupoFidsDTO> grupos = new ArrayList<>();
        // 1) Viajes ya armados
        Map<String, List<SegmentoFidsFila>> asignados = filas.stream().filter(f -> f.grupoCodigo() != null)
                .collect(Collectors.groupingBy(SegmentoFidsFila::grupoCodigo, LinkedHashMap::new, Collectors.toList()));
        asignados.forEach((codigo, lista) -> grupos.add(grupo(codigo, lista)));
        // 2) Pendientes: por tipo + lugar clave y ventana horaria
        Map<String, List<SegmentoFidsFila>> porLugar = filas.stream().filter(f -> f.grupoCodigo() == null)
                .collect(Collectors.groupingBy(f -> f.tipo() + "|" + f.lugarClaveId(), LinkedHashMap::new, Collectors.toList()));
        for (List<SegmentoFidsFila> lista : porLugar.values()) {
            List<SegmentoFidsFila> actual = new ArrayList<>();
            for (SegmentoFidsFila f : lista) {   // ya vienen por hora
                if (!actual.isEmpty() && Duration.between(actual.get(0).horaInicio(), f.horaInicio()).compareTo(ventana(f.tipo())) > 0) {
                    grupos.add(grupo(null, actual));
                    actual = new ArrayList<>();
                }
                actual.add(f);
            }
            if (!actual.isEmpty()) grupos.add(grupo(null, actual));
        }
        grupos.sort(Comparator.comparing(GrupoFidsDTO::desde).thenComparing(GrupoFidsDTO::tipo));
        int pax = filas.stream().mapToInt(SegmentoFidsFila::pax).sum();
        int sinAsignar = (int) filas.stream().filter(f -> f.grupoCodigo() == null).count();
        return new TableroFidsDTO(fecha, LocalDateTime.now(), filas.size(), pax, sinAsignar, grupos);
    }

    private GrupoFidsDTO grupo(String codigo, List<SegmentoFidsFila> lista) {
        SegmentoFidsFila p = lista.get(0);
        int pax = lista.stream().mapToInt(SegmentoFidsFila::pax).sum();
        int reservas = (int) lista.stream().map(SegmentoFidsFila::reservaId).distinct().count();
        List<String> vuelos = lista.stream().map(SegmentoFidsFila::vuelo).filter(Objects::nonNull).distinct().toList();
        // Estado del grupo = el segmento menos avanzado
        EstadoSegmento estado = lista.stream().map(SegmentoFidsFila::estado)
                .min(Comparator.comparingInt(e -> p.tipo().orden(e))).orElse(EstadoSegmento.PROGRAMADO);
        LocalDateTime desde = lista.stream().map(SegmentoFidsFila::horaInicio).min(Comparator.naturalOrder()).orElseThrow();
        LocalDateTime hasta = lista.stream().map(SegmentoFidsFila::horaInicio).max(Comparator.naturalOrder()).orElseThrow();
        String clave = codigo != null ? codigo
                : p.tipo() + "|" + p.lugarClaveId() + "|" + desde.toLocalTime();
        boolean excede = codigo != null && p.capacidad() != null && pax > p.capacidad();
        return new GrupoFidsDTO(clave, p.tipo().name(), p.lugarClaveId(), p.lugarClave(), desde, hasta, pax, reservas,
                vuelos, estado.name(), codigo != null, excede, codigo,
                codigo == null ? null : p.vehiculoId(), codigo == null ? null : p.placa(),
                codigo == null ? null : p.capacidad(), codigo == null ? null : p.operadorId(),
                codigo == null ? null : p.operador(), lista);
    }

    private static Duration ventana(TipoSegmento t) {
        return t == TipoSegmento.TOUR_DIARIO ? VENTANA_TOUR : VENTANA_TRANSFER;
    }

    @Transactional(readOnly = true)
    public RecursosFidsDTO recursos() {
        var vs = vehiculos.findAllByOrderByVehiculoIdAsc().stream().filter(v -> "OPERATIVO".equals(v.getEstado()))
                .sorted(Comparator.comparing(Vehiculo::getCapacidadPax))
                .map(v -> new RecursosFidsDTO.VehiculoItem(v.getVehiculoId(), v.getPlaca(), v.getTipoVehiculo(), v.getCapacidadPax()))
                .toList();
        var os = usuarios.findAllByOrderByUsuarioIdAsc().stream().filter(this::esOperadorActivo)
                .map(u -> new RecursosFidsDTO.OperadorItem(u.getUsuarioId(), u.getUsername())).toList();
        return new RecursosFidsDTO(vs, os);
    }

    // ------------------------------------------------------------------ asignación

    @Transactional
    public List<SegmentoDTO> asignar(AsignacionRequestDTO req) {
        Set<Long> ids = new LinkedHashSet<>(req.segmentoIds());
        List<SegmentoOperativo> lista = segmentos.bloquear(ids);
        if (lista.size() != ids.size()) throw new ResourceNotFoundException("Alguno de los segmentos ya no existe.");

        SegmentoOperativo primero = lista.get(0);
        for (SegmentoOperativo s : lista) {
            if (!s.getTipo().esOperable()) throw new BusinessException("El alojamiento no lleva vehículo ni operador.");
            if (s.getTipo() != primero.getTipo()) throw new BusinessException("Un viaje solo agrupa segmentos del mismo tipo.");
            if (!s.getFecha().equals(primero.getFecha())) throw new BusinessException("Un viaje solo agrupa segmentos del mismo día.");
            if (s.getEstado() != EstadoSegmento.PROGRAMADO) {
                throw new BusinessException("El segmento " + s.getSegmentoId() + " ya está en curso (" + s.getEstado() + ").");
            }
            if ("ANULADA".equals(s.getReservaMaster().getReserva().getEstadoReserva())) {
                throw new BusinessException("La reserva " + s.getReservaMaster().getReservaId() + " está anulada.");
            }
        }
        Vehiculo v = vehiculos.findById(req.vehiculoId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con Id: " + req.vehiculoId()));
        if (!"OPERATIVO".equals(v.getEstado())) throw new BusinessException("El vehículo " + v.getPlaca() + " no está operativo.");
        int pax = lista.stream().mapToInt(SegmentoOperativo::getPax).sum();
        if (pax > v.getCapacidadPax()) {
            throw new BusinessException("El vehículo " + v.getPlaca() + " lleva " + v.getCapacidadPax() + " pasajeros y el viaje tiene " + pax + ".");
        }
        UsuarioSistema op = usuarios.findById(req.operadorId())
                .orElseThrow(() -> new ResourceNotFoundException("Operador no encontrado con Id: " + req.operadorId()));
        if (!esOperadorActivo(op)) throw new BusinessException(op.getUsername() + " no es un operador activo.");

        // Mismo viaje si todos ya compartían código; si no, viaje nuevo
        Set<String> codigos = lista.stream().map(SegmentoOperativo::getGrupoCodigo).collect(Collectors.toSet());
        String codigo = codigos.size() == 1 && codigos.iterator().next() != null ? codigos.iterator().next() : nuevoCodigo(primero);

        LocalDateTime desde = lista.stream().map(SegmentoOperativo::getHoraInicio).min(Comparator.naturalOrder()).orElseThrow();
        LocalDateTime hasta = lista.stream().map(SegmentoOperativo::getHoraInicio).max(Comparator.naturalOrder()).orElseThrow();
        long cruces = segmentos.cruces(primero.getFecha(), codigo, desde.minus(HOLGURA_CRUCE), hasta.plus(HOLGURA_CRUCE),
                v.getVehiculoId(), op.getUsuarioId());
        if (cruces > 0) {
            throw new BusinessException("El vehículo " + v.getPlaca() + " o el operador " + op.getUsername()
                    + " ya tienen otro viaje a menos de " + HOLGURA_CRUCE.toHours() + " h de este.");
        }
        for (SegmentoOperativo s : lista) {
            s.asignar(codigo, v, op);
            s.setObservacion(null);
        }
        segmentos.flush();
        return lista.stream().map(SegmentoDTO::desde).toList();
    }

    /** Deshace un viaje (solo si nadie ha empezado) */
    @Transactional
    public void liberar(String codigo) {
        List<SegmentoOperativo> lista = segmentos.bloquearGrupo(codigo);
        if (lista.isEmpty()) throw new ResourceNotFoundException("No existe el viaje " + codigo + ".");
        if (lista.stream().anyMatch(s -> s.getEstado() != EstadoSegmento.PROGRAMADO)) {
            throw new BusinessException("El viaje " + codigo + " ya empezó: no se puede liberar.");
        }
        lista.forEach(SegmentoOperativo::liberar);
    }

    private String nuevoCodigo(SegmentoOperativo s) {
        String prefijo = switch (s.getTipo()) {
            case TRANSFER_IN -> "TIN";
            case TRANSFER_OUT -> "TOUT";
            default -> "TOUR";
        } + "-" + s.getFecha().format(AAAAMMDD) + "-";
        String codigo;
        do {
            codigo = prefijo + UUID.randomUUID().toString().substring(0, 4).toUpperCase(Locale.ROOT);
        } while (!segmentos.findByGrupoCodigoOrderByHoraInicioAscSegmentoIdAsc(codigo).isEmpty());
        return codigo;
    }

    private boolean esOperadorActivo(UsuarioSistema u) {
        return "OPERADOR".equals(u.getRol()) && "ACTIVO".equals(u.getEstado());
    }
}
