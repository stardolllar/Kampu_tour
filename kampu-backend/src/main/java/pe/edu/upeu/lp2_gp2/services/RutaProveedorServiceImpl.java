package pe.edu.upeu.lp2_gp2.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.lp2_gp2.dto.RutaProveedorDTO;
import pe.edu.upeu.lp2_gp2.dto.RutaProveedorRequestDTO;
import pe.edu.upeu.lp2_gp2.entity.RutaProveedor;
import pe.edu.upeu.lp2_gp2.exception.BusinessException;
import pe.edu.upeu.lp2_gp2.exception.DuplicateResourceException;
import pe.edu.upeu.lp2_gp2.exception.ResourceNotFoundException;
import pe.edu.upeu.lp2_gp2.repository.EmpresaTransporteRepository;
import pe.edu.upeu.lp2_gp2.repository.LugarRepository;
import pe.edu.upeu.lp2_gp2.repository.OperacionTransporteRepository;
import pe.edu.upeu.lp2_gp2.repository.RutaProveedorRepository;

import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class RutaProveedorServiceImpl implements IRutaProveedorService {
    private final RutaProveedorRepository rutaRepository;
    private final EmpresaTransporteRepository empresaRepository;
    private final LugarRepository lugarRepository;
    private final OperacionTransporteRepository operacionRepository;

    public RutaProveedorServiceImpl(RutaProveedorRepository rutaRepository, EmpresaTransporteRepository empresaRepository,
                                    LugarRepository lugarRepository, OperacionTransporteRepository operacionRepository) {
        this.rutaRepository = rutaRepository;
        this.empresaRepository = empresaRepository;
        this.lugarRepository = lugarRepository;
        this.operacionRepository = operacionRepository;
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
        RutaProveedor r = new RutaProveedor();
        copiar(dto, r, null);
        return RutaProveedorDTO.desde(rutaRepository.saveAndFlush(r));
    }

    @Override
    @Transactional
    public RutaProveedorDTO actualizar(Long id, RutaProveedorRequestDTO dto) {
        RutaProveedor r = obtener(id);
        boolean cambiaTramo = !r.getEmpresaId().equals(dto.empresaId()) || !r.getOrigenId().equals(dto.origenId())
                || !r.getDestinoId().equals(dto.destinoId());
        if (cambiaTramo && operacionRepository.existsByEmpresaIdAndOrigenIdAndDestinoId(r.getEmpresaId(), r.getOrigenId(), r.getDestinoId())) {
            throw new BusinessException("La ruta tiene operaciones registradas: solo puedes cambiar duración, tarifa, tipo y estado.");
        }
        copiar(dto, r, id);
        return RutaProveedorDTO.desde(rutaRepository.saveAndFlush(r));
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

    private void copiar(RutaProveedorRequestDTO dto, RutaProveedor r, Long id) {
        if (!empresaRepository.existsById(dto.empresaId())) throw new BusinessException("La empresa seleccionada no existe.");
        if (!lugarRepository.existsById(dto.origenId())) throw new BusinessException("El origen seleccionado no existe.");
        if (!lugarRepository.existsById(dto.destinoId())) throw new BusinessException("El destino seleccionado no existe.");
        if (dto.origenId().equals(dto.destinoId())) throw new BusinessException("El origen y el destino deben ser distintos.");
        Optional<RutaProveedor> misma = rutaRepository.findByEmpresaIdAndOrigenIdAndDestinoId(dto.empresaId(), dto.origenId(), dto.destinoId());
        if (misma.isPresent() && !misma.get().getRutaId().equals(id)) {
            throw new DuplicateResourceException("La empresa ya tiene registrada esa ruta.");
        }
        r.setEmpresaId(dto.empresaId());
        r.setOrigenId(dto.origenId());
        r.setDestinoId(dto.destinoId());
        r.setTipoServicio(dto.tipoServicio());
        r.setDuracionEstimadaMin(dto.duracionEstimadaMin());
        r.setTarifaReferencial(dto.tarifaReferencial() == null ? null : dto.tarifaReferencial().setScale(2, RoundingMode.HALF_UP));
        r.setEstado(dto.estado());
    }
}
