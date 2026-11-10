package pe.edu.upeu.lp2_gp2.services;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.lp2_gp2.dto.RutaProveedorDTO;
import pe.edu.upeu.lp2_gp2.dto.RutaProveedorRequestDTO;
import pe.edu.upeu.lp2_gp2.dto.SalidaProgramadaDTO;
import pe.edu.upeu.lp2_gp2.dto.SalidaProgramadaRequestDTO;
import pe.edu.upeu.lp2_gp2.entity.PaqueteTour;
import pe.edu.upeu.lp2_gp2.entity.RutaProveedor;
import pe.edu.upeu.lp2_gp2.exception.BusinessException;
import pe.edu.upeu.lp2_gp2.exception.DuplicateResourceException;
import pe.edu.upeu.lp2_gp2.exception.ResourceNotFoundException;
import pe.edu.upeu.lp2_gp2.repository.*;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Rutas de las empresas de transporte.
 *  - El destino puede ser un lugar o un paquete: con paquete, el destino es el lugar del paquete
 *    (trg_ruta_biu) y al CREAR la ruta se crea automáticamente su salida programada.
 *  - La duración, si no se indica, se calcula con datos reales (DuracionRutaService: OSRM o
 *    PKG_RUTAS con las coordenadas de los lugares).
 */
@Service
public class RutaProveedorServiceImpl implements IRutaProveedorService {
    private final RutaProveedorRepository rutaRepository;
    private final EmpresaTransporteRepository empresaRepository;
    private final LugarRepository lugarRepository;
    private final PaqueteTourRepository paqueteRepository;
    private final OperacionTransporteRepository operacionRepository;
    private final DuracionRutaService duracionService;
    private final ISalidaProgramadaService salidaService;
    private final EntityManager em;

    public RutaProveedorServiceImpl(RutaProveedorRepository rutaRepository, EmpresaTransporteRepository empresaRepository,
                                    LugarRepository lugarRepository, PaqueteTourRepository paqueteRepository,
                                    OperacionTransporteRepository operacionRepository, DuracionRutaService duracionService,
                                    ISalidaProgramadaService salidaService, EntityManager em) {
        this.rutaRepository = rutaRepository;
        this.empresaRepository = empresaRepository;
        this.lugarRepository = lugarRepository;
        this.paqueteRepository = paqueteRepository;
        this.operacionRepository = operacionRepository;
        this.duracionService = duracionService;
        this.salidaService = salidaService;
        this.em = em;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RutaProveedorDTO> listarTodo() {
        return rutaRepository.findAllByOrderByRutaIdAsc().stream().map(RutaProveedorDTO::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RutaProveedorDTO buscarPorId(Long id) {
        return RutaProveedorDTO.desde(obtener(id));
    }

    @Override
    @Transactional
    public RutaProveedorDTO crear(RutaProveedorRequestDTO dto) {
        // Ruta hacia un paquete: se pide también la salida que se creará con ella
        if (dto.paqueteId() != null) {
            if (dto.fechaSalida() == null) throw new BusinessException("Indica la fecha de la salida programada del paquete.");
            if (dto.fechaSalida().isBefore(LocalDate.now())) throw new BusinessException("La fecha de la salida no puede ser pasada.");
            if (dto.cuposSalida() == null) throw new BusinessException("Indica los cupos de la salida programada.");
        }
        RutaProveedor r = new RutaProveedor();
        copiar(dto, r, null);
        RutaProveedor guardada = rutaRepository.saveAndFlush(r);
        em.refresh(guardada);   // destino y duración pueden venir de trg_ruta_biu

        Long salidaId = null;
        if (dto.paqueteId() != null) {
            SalidaProgramadaDTO salida = salidaService.crear(new SalidaProgramadaRequestDTO(
                    dto.paqueteId(), dto.fechaSalida(), dto.cuposSalida(), dto.guiaSalidaId(), "PROGRAMADA"));
            salidaId = salida.salidaId();
        }
        return RutaProveedorDTO.desde(guardada, salidaId);
    }

    @Override
    @Transactional
    public RutaProveedorDTO actualizar(Long id, RutaProveedorRequestDTO dto) {
        RutaProveedor r = obtener(id);
        Long destinoNuevo = destino(dto);
        boolean cambiaTramo = !r.getEmpresaId().equals(dto.empresaId()) || !r.getOrigenId().equals(dto.origenId())
                || !r.getDestinoId().equals(destinoNuevo);
        if (cambiaTramo && operacionRepository.existsByEmpresaIdAndOrigenIdAndDestinoId(r.getEmpresaId(), r.getOrigenId(), r.getDestinoId())) {
            throw new BusinessException("La ruta tiene operaciones registradas: solo puedes cambiar duración, tarifa, tipo y estado.");
        }
        copiar(dto, r, id);
        rutaRepository.saveAndFlush(r);
        em.refresh(r);
        return RutaProveedorDTO.desde(r);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        RutaProveedor r = obtener(id);
        if (operacionRepository.existsByEmpresaIdAndOrigenIdAndDestinoId(r.getEmpresaId(), r.getOrigenId(), r.getDestinoId())) {
            throw new BusinessException("La ruta tiene operaciones registradas. Puedes marcarla como INACTIVA.");
        }
        rutaRepository.delete(r);
    }

    private RutaProveedor obtener(Long id) {
        return rutaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ruta no encontrada con Id: " + id));
    }

    // Destino de la ruta: el lugar del paquete, o el lugar indicado.
    // Si el paquete aún no tiene lugar de destino, se le asigna el indicado.
    private Long destino(RutaProveedorRequestDTO dto) {
        if (dto.paqueteId() == null) {
            if (dto.destinoId() == null) throw new BusinessException("Selecciona el destino (un lugar o un paquete).");
            return dto.destinoId();
        }
        PaqueteTour p = paqueteRepository.findById(dto.paqueteId())
                .orElseThrow(() -> new BusinessException("El paquete seleccionado no existe."));
        if (!"ACTIVO".equals(p.getEstado())) throw new BusinessException("El paquete " + p.getNombre() + " está inactivo.");
        if (p.getLugarDestinoId() == null) {
            if (dto.destinoId() == null) {
                throw new BusinessException("El paquete " + p.getNombre() + " no tiene lugar de destino: indícalo.");
            }
            if (!lugarRepository.existsById(dto.destinoId())) throw new BusinessException("El destino seleccionado no existe.");
            p.setLugarDestinoId(dto.destinoId());
            paqueteRepository.saveAndFlush(p);
        }
        return p.getLugarDestinoId();
    }

    private void copiar(RutaProveedorRequestDTO dto, RutaProveedor r, Long id) {
        if (!empresaRepository.existsById(dto.empresaId())) throw new BusinessException("La empresa seleccionada no existe.");
        if (!lugarRepository.existsById(dto.origenId())) throw new BusinessException("El origen seleccionado no existe.");
        Long destinoId = destino(dto);
        if (!lugarRepository.existsById(destinoId)) throw new BusinessException("El destino seleccionado no existe.");
        if (dto.origenId().equals(destinoId)) throw new BusinessException("El origen y el destino deben ser distintos.");
        Optional<RutaProveedor> misma = rutaRepository.findByEmpresaIdAndOrigenIdAndDestinoId(dto.empresaId(), dto.origenId(), destinoId);
        if (misma.isPresent() && !misma.get().getRutaId().equals(id)) {
            throw new DuplicateResourceException("La empresa ya tiene registrada esa ruta.");
        }
        // Duración vacía: con datos reales (OSRM o PKG_RUTAS); si no hay coordenadas, trg_ruta_biu avisa
        Integer duracion = dto.duracionEstimadaMin() != null ? dto.duracionEstimadaMin()
                : duracionService.minutos(dto.origenId(), destinoId, dto.tipoServicio());
        r.setEmpresaId(dto.empresaId());
        r.setOrigenId(dto.origenId());
        r.setDestinoId(destinoId);
        r.setPaqueteId(dto.paqueteId());
        r.setTipoServicio(dto.tipoServicio());
        r.setDuracionEstimadaMin(duracion);
        r.setTarifaReferencial(dto.tarifaReferencial() == null ? null : dto.tarifaReferencial().setScale(2, RoundingMode.HALF_UP));
        r.setEstado(dto.estado());
    }
}
