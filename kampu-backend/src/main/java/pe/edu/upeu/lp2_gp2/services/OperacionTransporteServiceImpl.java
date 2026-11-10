package pe.edu.upeu.lp2_gp2.services;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.lp2_gp2.dto.*;
import pe.edu.upeu.lp2_gp2.entity.OperacionTransporte;
import pe.edu.upeu.lp2_gp2.entity.Vehiculo;
import pe.edu.upeu.lp2_gp2.entity.VistaTableroOperacion;
import pe.edu.upeu.lp2_gp2.exception.BusinessException;
import pe.edu.upeu.lp2_gp2.exception.ResourceNotFoundException;
import pe.edu.upeu.lp2_gp2.repository.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Tráfico de transporte. Las reglas viven en la base de datos (01_schema_v3.sql):
 *  - trg_operacion_transporte: código KMP-###, llegada estimada, transiciones válidas,
 *    recursos obligatorios, ruta activa, cruces de horario, horas del conductor,
 *    mantenimiento, capacidad de la salida y el historial de estados.
 *  - PKG_ASIGNACION.asignar_automatico: elige el mejor vehículo + conductor.
 * Este servicio arma los datos, delega en esa lógica y traduce sus errores (ORA-20xxx)
 * con GlobalExceptionHandler.
 */
@Service
public class OperacionTransporteServiceImpl implements IOperacionTransporteService {
    private final OperacionTransporteRepository operacionRepository;
    private final VistaTableroOperacionRepository tableroRepository;
    private final HistorialTraficoOperacionRepository historialRepository;
    private final DominioEstadoRepository estadoRepository;
    private final SalidaProgramadaRepository salidaRepository;
    private final LugarRepository lugarRepository;
    private final EmpresaTransporteRepository empresaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;
    private final PkgAsignacionRepository pkg;
    private final EntityManager em;

    public OperacionTransporteServiceImpl(OperacionTransporteRepository operacionRepository,
                                          VistaTableroOperacionRepository tableroRepository,
                                          HistorialTraficoOperacionRepository historialRepository,
                                          DominioEstadoRepository estadoRepository, SalidaProgramadaRepository salidaRepository,
                                          LugarRepository lugarRepository, EmpresaTransporteRepository empresaRepository,
                                          VehiculoRepository vehiculoRepository, ConductorRepository conductorRepository,
                                          PkgAsignacionRepository pkg, EntityManager em) {
        this.operacionRepository = operacionRepository;
        this.tableroRepository = tableroRepository;
        this.historialRepository = historialRepository;
        this.estadoRepository = estadoRepository;
        this.salidaRepository = salidaRepository;
        this.lugarRepository = lugarRepository;
        this.empresaRepository = empresaRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
        this.pkg = pkg;
        this.em = em;
    }

    // ---------- Consultas ----------

    @Override
    @Transactional(readOnly = true)
    public List<OperacionDTO> listar(Long salidaId) {
        List<OperacionTransporte> lista = salidaId == null
                ? operacionRepository.findAllByOrderByHoraProgramadaSalidaAscOperacionIdAsc()
                : operacionRepository.findBySalidaIdOrderByHoraProgramadaSalidaAscOperacionIdAsc(salidaId);
        // Nombres, estado y retrasos: vista VW_TABLERO_OPERACIONES
        Map<Long, VistaTableroOperacion> tablero = tableroRepository.findAll().stream()
                .collect(Collectors.toMap(VistaTableroOperacion::getOperacionId, Function.identity()));
        return lista.stream().map(o -> OperacionDTO.desde(o, tablero.get(o.getOperacionId()))).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OperacionDTO buscarPorId(Long id) {
        return dto(obtener(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialOperacionDTO> historial(Long id) {
        obtener(id);
        return historialRepository.findByOperacionIdOrderByHistorialIdAsc(id).stream()
                .map(HistorialOperacionDTO::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecursoDisponibleDTO> recursosDisponibles(Long id) {
        obtener(id);
        return pkg.recursosDisponibles(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecursoCandidatoDTO> candidatos(Long id) {
        obtener(id);
        return pkg.candidatos(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadoOperacionDTO> estados() {
        Map<String, List<String>> transiciones = pkg.transiciones();
        return estadoRepository.findAllByOrderByOrdenAsc().stream()
                .map(e -> new EstadoOperacionDTO(e.getCodigoEstado(), e.getNombreMostrar(), e.getOrden(),
                        "S".equals(e.getRequiereRecursos()), "S".equals(e.getOcupaRecursos()), "S".equals(e.getEsFinal()),
                        transiciones.getOrDefault(e.getCodigoEstado(), List.of())))
                .toList();
    }

    // ---------- Escritura ----------

    @Override
    @Transactional
    public OperacionDTO crear(OperacionRequestDTO dto, String usuario) {
        OperacionTransporte o = new OperacionTransporte();
        copiar(dto, o);
        // Con vehículo y conductor nace ASIGNADA; si no, PROGRAMADA (para asignarla después)
        o.setEstadoActual(o.getVehiculoId() != null ? "ASIGNADO" : "PROGRAMADO");
        return comoUsuario(usuario, () -> {
            OperacionTransporte guardada = operacionRepository.saveAndFlush(o);
            return refrescar(guardada);
        });
    }

    @Override
    @Transactional
    public OperacionDTO actualizar(Long id, OperacionRequestDTO dto, String usuario) {
        OperacionTransporte o = obtener(id);
        String estado = o.getEstadoActual();
        copiar(dto, o);
        boolean conRecursos = o.getVehiculoId() != null;
        if (conRecursos && "PROGRAMADO".equals(estado)) {
            o.setEstadoActual("ASIGNADO");
        } else if (!conRecursos && "ASIGNADO".equals(estado)) {
            o.setEstadoActual("PROGRAMADO");   // desasignar (el trigger valida la transición)
        }
        return comoUsuario(usuario, () -> refrescar(operacionRepository.saveAndFlush(o)));
    }

    @Override
    @Transactional
    public OperacionDTO cambiarEstado(Long id, CambioEstadoRequestDTO dto, String usuario) {
        OperacionTransporte o = obtener(id);
        String nuevo = dto.estado().trim().toUpperCase();
        if (nuevo.equals(o.getEstadoActual())) {
            throw new BusinessException(o.getCodigoOperacion() + " ya está en estado " + nuevo + ".");
        }
        o.setEstadoActual(nuevo);
        // La transición, los recursos y la capacidad los valida trg_operacion_transporte
        return comoUsuario(usuario, () -> {
            operacionRepository.saveAndFlush(o);
            if (dto.observacion() != null && !dto.observacion().isBlank()) {
                pkg.observarUltimoCambio(id, dto.observacion().trim());
            }
            return refrescar(o);
        });
    }

    @Override
    @Transactional
    public OperacionDTO asignarAutomatico(Long id, String usuario) {
        OperacionTransporte o = obtener(id);
        return comoUsuario(usuario, () -> {
            pkg.asignarAutomatico(id);   // PKG_ASIGNACION.asignar_automatico
            return refrescar(o);
        });
    }

    @Override
    @Transactional
    public OperacionDTO asignar(Long id, AsignacionRequestDTO dto, String usuario) {
        OperacionTransporte o = obtener(id);
        if (!"PROGRAMADO".equals(o.getEstadoActual())) {
            throw new BusinessException(o.getCodigoOperacion() + " está " + o.getEstadoActual()
                    + ": para cambiar el vehículo o el conductor primero desasígnala (vuelve a PROGRAMADO).");
        }
        Vehiculo v = vehiculoRepository.findById(dto.vehiculoId())
                .orElseThrow(() -> new BusinessException("El vehículo seleccionado no existe."));
        if (!conductorRepository.existsById(dto.conductorId())) throw new BusinessException("El conductor seleccionado no existe.");
        o.setEmpresaId(v.getEmpresaId());
        o.setVehiculoId(v.getVehiculoId());
        o.setConductorId(dto.conductorId());
        o.setEstadoActual("ASIGNADO");
        return comoUsuario(usuario, () -> refrescar(operacionRepository.saveAndFlush(o)));
    }

    // ---------- Apoyo ----------

    private void copiar(OperacionRequestDTO d, OperacionTransporte o) {
        if (d.salidaId() != null && !salidaRepository.existsById(d.salidaId())) {
            throw new BusinessException("La salida seleccionada no existe.");
        }
        if (!lugarRepository.existsById(d.origenId())) throw new BusinessException("El origen seleccionado no existe.");
        if (!lugarRepository.existsById(d.destinoId())) throw new BusinessException("El destino seleccionado no existe.");
        if (d.origenId().equals(d.destinoId())) throw new BusinessException("El origen y el destino deben ser distintos.");
        if ((d.vehiculoId() == null) != (d.conductorId() == null)) {
            throw new BusinessException("Asigna el vehículo y el conductor juntos (o deja ambos vacíos).");
        }
        if (d.horaEstimadaLlegada() != null && !d.horaEstimadaLlegada().isAfter(d.horaProgramadaSalida())) {
            throw new BusinessException("La llegada estimada debe ser posterior a la hora de salida.");
        }
        Long empresaId = d.empresaId();
        if (d.vehiculoId() != null) {
            Vehiculo v = vehiculoRepository.findById(d.vehiculoId())
                    .orElseThrow(() -> new BusinessException("El vehículo seleccionado no existe."));
            if (empresaId != null && !empresaId.equals(v.getEmpresaId())) {
                throw new BusinessException("El vehículo " + v.getPlaca() + " no pertenece a la empresa seleccionada.");
            }
            empresaId = v.getEmpresaId();   // la empresa es la del vehículo
        }
        if (empresaId != null && !empresaRepository.existsById(empresaId)) {
            throw new BusinessException("La empresa seleccionada no existe.");
        }
        o.setSalidaId(d.salidaId());
        o.setOrigenId(d.origenId());
        o.setDestinoId(d.destinoId());
        o.setEmpresaId(empresaId);
        o.setVehiculoId(d.vehiculoId());
        o.setConductorId(d.conductorId());
        o.setPaxProgramados(d.paxProgramados());
        o.setHoraProgramadaSalida(d.horaProgramadaSalida().withSecond(0).withNano(0));
        // Nula: el trigger la calcula con la duración de la ruta
        o.setHoraEstimadaLlegada(d.horaEstimadaLlegada() == null ? null : d.horaEstimadaLlegada().withSecond(0).withNano(0));
        o.setObservaciones(d.observaciones() == null || d.observaciones().isBlank() ? null : d.observaciones().trim());
    }

    // Lee de nuevo la fila: los triggers pudieron cambiar código, llegada, recursos o estado
    private OperacionDTO refrescar(OperacionTransporte o) {
        em.refresh(o);
        return dto(o);
    }

    private OperacionDTO dto(OperacionTransporte o) {
        return OperacionDTO.desde(o, tableroRepository.findById(o.getOperacionId()).orElse(null));
    }

    // El historial (trigger) guarda USERENV.CLIENT_IDENTIFIER como usuario_registro
    private <T> T comoUsuario(String usuario, Supplier<T> accion) {
        pkg.identificarUsuario(usuario);
        try {
            return accion.get();
        } finally {
            try {
                pkg.limpiarUsuario();
            } catch (RuntimeException ignorada) {
                // la conexión pudo quedar inutilizable tras el error original; ese error es el que importa
            }
        }
    }

    private OperacionTransporte obtener(Long id) {
        return operacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operación de transporte no encontrada con Id: " + id));
    }
}
