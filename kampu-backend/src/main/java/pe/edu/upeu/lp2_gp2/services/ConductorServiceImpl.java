package pe.edu.upeu.lp2_gp2.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.lp2_gp2.dto.ConductorDTO;
import pe.edu.upeu.lp2_gp2.dto.ConductorRequestDTO;
import pe.edu.upeu.lp2_gp2.dto.Reglas;
import pe.edu.upeu.lp2_gp2.entity.Conductor;
import pe.edu.upeu.lp2_gp2.exception.BusinessException;
import pe.edu.upeu.lp2_gp2.exception.DuplicateResourceException;
import pe.edu.upeu.lp2_gp2.exception.ResourceNotFoundException;
import pe.edu.upeu.lp2_gp2.repository.ConductorRepository;
import pe.edu.upeu.lp2_gp2.repository.EmpresaTransporteRepository;
import pe.edu.upeu.lp2_gp2.repository.OperacionTransporteRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ConductorServiceImpl implements IConductorService {
    private final ConductorRepository conductorRepository;
    private final EmpresaTransporteRepository empresaRepository;
    private final OperacionTransporteRepository operacionRepository;

    public ConductorServiceImpl(ConductorRepository conductorRepository, EmpresaTransporteRepository empresaRepository,
                                OperacionTransporteRepository operacionRepository) {
        this.conductorRepository = conductorRepository;
        this.empresaRepository = empresaRepository;
        this.operacionRepository = operacionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConductorDTO> listarTodo() {
        return conductorRepository.findAllByOrderByConductorIdAsc().stream().map(ConductorDTO::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConductorDTO buscarPorId(Long id) {
        return ConductorDTO.desde(obtener(id));
    }

    @Override
    @Transactional
    public ConductorDTO crear(ConductorRequestDTO dto) {
        Conductor c = new Conductor();
        copiar(dto, c, null);
        return ConductorDTO.desde(conductorRepository.saveAndFlush(c));
    }

    @Override
    @Transactional
    public ConductorDTO actualizar(Long id, ConductorRequestDTO dto) {
        Conductor c = obtener(id);
        if (!c.getEmpresaId().equals(dto.empresaId()) && operacionRepository.existsByConductorId(id)) {
            throw new BusinessException("El conductor tiene operaciones registradas: no puede cambiar de empresa.");
        }
        copiar(dto, c, id);
        return ConductorDTO.desde(conductorRepository.saveAndFlush(c));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Conductor c = obtener(id);
        if (operacionRepository.existsByConductorId(id)) {
            throw new BusinessException("El conductor tiene operaciones registradas. Puedes marcarlo como INACTIVO.");
        }
        conductorRepository.delete(c);
    }

    private Conductor obtener(Long id) {
        return conductorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado con Id: " + id));
    }

    private void copiar(ConductorRequestDTO dto, Conductor c, Long id) {
        if (!empresaRepository.existsById(dto.empresaId())) throw new BusinessException("La empresa seleccionada no existe.");
        if (!Reglas.telefonoValido(dto.telefono())) throw new BusinessException(Reglas.MSG_TELEFONO);
        String licencia = dto.numeroLicencia().trim().toUpperCase();
        Optional<Conductor> mismo = conductorRepository.findByNumeroLicenciaIgnoreCase(licencia);
        if (mismo.isPresent() && !mismo.get().getConductorId().equals(id)) {
            throw new DuplicateResourceException("Ya existe un conductor con la licencia " + licencia + ".");
        }
        c.setEmpresaId(dto.empresaId());
        c.setNombres(dto.nombres().trim());
        c.setNumeroLicencia(licencia);
        c.setCategoriaLicencia(dto.categoriaLicencia());
        c.setFechaVencLicencia(dto.fechaVencLicencia());
        c.setTelefono(dto.telefono() == null || dto.telefono().isBlank() ? null : dto.telefono().trim());
        c.setEstadoDisponibilidad(dto.estadoDisponibilidad());
    }
}
