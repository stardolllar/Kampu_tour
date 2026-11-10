package pe.edu.upeu.lp2_gp2.services;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.lp2_gp2.dto.SalidaProgramadaDTO;
import pe.edu.upeu.lp2_gp2.dto.SalidaProgramadaRequestDTO;
import pe.edu.upeu.lp2_gp2.entity.PaqueteTour;
import pe.edu.upeu.lp2_gp2.entity.SalidaProgramada;
import pe.edu.upeu.lp2_gp2.exception.BusinessException;
import pe.edu.upeu.lp2_gp2.exception.ResourceNotFoundException;
import pe.edu.upeu.lp2_gp2.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Salidas programadas. En la v3 la base de datos se encarga de:
 *  - trg_salida_biu: fecha_fin según los días del paquete y cupos_disponibles.
 *  - trg_salida_confirmar: solo se confirma si el transporte asignado (OPERACION_TRANSPORTE)
 *    alcanza para los pasajeros vendidos (PKG_ASIGNACION.faltante_transporte).
 * El vehículo y el conductor ya no van en la salida: se asignan por operación.
 */
@Service
public class SalidaProgramadaServiceImpl implements ISalidaProgramadaService {
    private final SalidaProgramadaRepository salidaRepository;
    private final PaqueteTourRepository paqueteRepository;
    private final GuiaRepository guiaRepository;
    private final DetalleReservaRepository detalleRepository;
    private final OperacionTransporteRepository operacionRepository;
    private final PkgAsignacionRepository pkg;
    private final EntityManager em;

    public SalidaProgramadaServiceImpl(SalidaProgramadaRepository salidaRepository, PaqueteTourRepository paqueteRepository,
                                       GuiaRepository guiaRepository, DetalleReservaRepository detalleRepository,
                                       OperacionTransporteRepository operacionRepository, PkgAsignacionRepository pkg,
                                       EntityManager em) {
        this.salidaRepository = salidaRepository;
        this.paqueteRepository = paqueteRepository;
        this.guiaRepository = guiaRepository;
        this.detalleRepository = detalleRepository;
        this.operacionRepository = operacionRepository;
        this.pkg = pkg;
        this.em = em;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalidaProgramadaDTO> listarTodo() {
        Map<Long, String> faltantes = pkg.faltantesPorSalida();
        Map<Long, Integer> operaciones = pkg.operacionesPorSalida();
        return salidaRepository.findAllByOrderBySalidaIdAsc().stream()
                .map(s -> SalidaProgramadaDTO.desde(s, operaciones.get(s.getSalidaId()), faltantes.get(s.getSalidaId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SalidaProgramadaDTO buscarPorId(Long id) {
        return dto(salidaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salida no encontrada con Id: " + id)));
    }

    @Override
    @Transactional
    public SalidaProgramadaDTO crear(SalidaProgramadaRequestDTO dto) {
        validar(dto, null);
        SalidaProgramada s = new SalidaProgramada();
        copiar(dto, s);
        s.setFechaFin(null);   // la calcula trg_salida_biu
        SalidaProgramada guardada = salidaRepository.saveAndFlush(s);
        em.refresh(guardada);  // fecha_fin y cupos_disponibles vienen del trigger
        return dto(guardada);
    }

    @Override
    @Transactional
    public SalidaProgramadaDTO actualizar(Long id, SalidaProgramadaRequestDTO dto) {
        SalidaProgramada s = salidaRepository.findByIdParaActualizar(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salida no encontrada con Id: " + id));
        validar(dto, id);
        int vendidos = s.getCuposMaximos() - s.getCuposDisponibles();
        if (dto.cuposMaximos() < vendidos) {
            throw new BusinessException("Ya hay " + vendidos + " viajeros reservados; los cupos no pueden ser menores.");
        }
        if ("CANCELADA".equals(dto.estado()) && !"CANCELADA".equals(s.getEstado()) && vendidos > 0) {
            throw new BusinessException("No puedes cancelar una salida con reservas activas. Anúlalas primero.");
        }
        boolean cambiaDuracion = !s.getPaqueteId().equals(dto.paqueteId()) || !s.getFechaInicio().equals(dto.fechaInicio());
        copiar(dto, s);
        if (cambiaDuracion) s.setFechaFin(null);   // trg_salida_biu la recalcula
        // Al pasar a CONFIRMADA, trg_salida_confirmar verifica el transporte (ORA-20014)
        salidaRepository.saveAndFlush(s);
        em.refresh(s);
        return dto(s);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        SalidaProgramada s = salidaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salida no encontrada con Id: " + id));
        if (detalleRepository.existsBySalidaId(id)) {
            throw new BusinessException("La salida tiene reservas asociadas. Puedes marcarla como CANCELADA.");
        }
        if (operacionRepository.existsBySalidaId(id)) {
            throw new BusinessException("La salida tiene operaciones de transporte. Cancélalas o márcala como CANCELADA.");
        }
        salidaRepository.delete(s);
    }

    private SalidaProgramadaDTO dto(SalidaProgramada s) {
        boolean activa = "PROGRAMADA".equals(s.getEstado()) || "CONFIRMADA".equals(s.getEstado());
        String faltante = activa ? pkg.faltanteTransporte(s.getSalidaId(), s.getCuposMaximos() - s.getCuposDisponibles()) : null;
        return SalidaProgramadaDTO.desde(s, pkg.operacionesPorSalida().get(s.getSalidaId()), faltante);
    }

    private void validar(SalidaProgramadaRequestDTO d, Long id) {
        PaqueteTour paquete = paqueteRepository.findById(d.paqueteId())
                .orElseThrow(() -> new BusinessException("Selecciona un paquete."));
        if (d.guiaId() != null && !guiaRepository.existsById(d.guiaId())) throw new BusinessException("El guía seleccionado no existe.");
        boolean activa = "PROGRAMADA".equals(d.estado()) || "CONFIRMADA".equals(d.estado());
        if (activa && d.guiaId() != null) {
            // Misma regla que el trigger para la fecha fin: inicio + días del paquete - 1
            LocalDate fin = d.fechaInicio().plusDays(paquete.getDias() - 1L);
            List<SalidaProgramada> cruces = salidaRepository
                    .findByGuiaIdAndEstadoInAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                            d.guiaId(), List.of("PROGRAMADA", "CONFIRMADA"), fin, d.fechaInicio());
            for (SalidaProgramada s : cruces) {
                if (id == null || !s.getSalidaId().equals(id)) {
                    throw new BusinessException("El guía ya está asignado a otra salida en esas fechas.");
                }
            }
        }
    }

    private void copiar(SalidaProgramadaRequestDTO d, SalidaProgramada s) {
        s.setPaqueteId(d.paqueteId());
        s.setFechaInicio(d.fechaInicio());
        s.setCuposMaximos(d.cuposMaximos());
        s.setGuiaId(d.guiaId());
        s.setEstado(d.estado());
    }
}
