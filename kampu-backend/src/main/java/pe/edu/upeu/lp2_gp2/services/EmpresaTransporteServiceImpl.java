package pe.edu.upeu.lp2_gp2.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.lp2_gp2.dto.EmpresaTransporteDTO;
import pe.edu.upeu.lp2_gp2.dto.EmpresaTransporteRequestDTO;
import pe.edu.upeu.lp2_gp2.dto.Reglas;
import pe.edu.upeu.lp2_gp2.entity.EmpresaTransporte;
import pe.edu.upeu.lp2_gp2.entity.VistaEmpresaCapacidad;
import pe.edu.upeu.lp2_gp2.exception.BusinessException;
import pe.edu.upeu.lp2_gp2.exception.DuplicateResourceException;
import pe.edu.upeu.lp2_gp2.exception.ResourceNotFoundException;
import pe.edu.upeu.lp2_gp2.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EmpresaTransporteServiceImpl implements IEmpresaTransporteService {
    private final EmpresaTransporteRepository empresaRepository;
    private final VistaEmpresaCapacidadRepository capacidadRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;
    private final RutaProveedorRepository rutaRepository;
    private final OperacionTransporteRepository operacionRepository;

    public EmpresaTransporteServiceImpl(EmpresaTransporteRepository empresaRepository,
                                        VistaEmpresaCapacidadRepository capacidadRepository,
                                        VehiculoRepository vehiculoRepository, ConductorRepository conductorRepository,
                                        RutaProveedorRepository rutaRepository, OperacionTransporteRepository operacionRepository) {
        this.empresaRepository = empresaRepository;
        this.capacidadRepository = capacidadRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
        this.rutaRepository = rutaRepository;
        this.operacionRepository = operacionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpresaTransporteDTO> listarTodo() {
        // Capacidad total, vehículos y conductores disponibles: vista VW_EMPRESA_CAPACIDAD
        Map<Long, VistaEmpresaCapacidad> capacidad = capacidadRepository.findAll().stream()
                .collect(Collectors.toMap(VistaEmpresaCapacidad::getEmpresaId, Function.identity()));
        return empresaRepository.findAllByOrderByEmpresaIdAsc().stream()
                .map(e -> EmpresaTransporteDTO.desde(e, capacidad.get(e.getEmpresaId()))).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmpresaTransporteDTO buscarPorId(Long id) {
        return dto(obtener(id));
    }

    @Override
    @Transactional
    public EmpresaTransporteDTO crear(EmpresaTransporteRequestDTO dto) {
        EmpresaTransporte e = new EmpresaTransporte();
        copiar(dto, e, null);
        return dto(empresaRepository.saveAndFlush(e));
    }

    @Override
    @Transactional
    public EmpresaTransporteDTO actualizar(Long id, EmpresaTransporteRequestDTO dto) {
        EmpresaTransporte e = obtener(id);
        copiar(dto, e, id);
        return dto(empresaRepository.saveAndFlush(e));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        EmpresaTransporte e = obtener(id);
        if (operacionRepository.existsByEmpresaId(id)) {
            throw new BusinessException("La empresa tiene operaciones de transporte registradas. Puedes marcarla como RETIRADA.");
        }
        if (vehiculoRepository.existsByEmpresaId(id) || conductorRepository.existsByEmpresaId(id)) {
            throw new BusinessException("Primero elimina los vehículos y conductores de la empresa (o márcala como RETIRADA).");
        }
        if (rutaRepository.existsByEmpresaId(id)) {
            throw new BusinessException("Primero elimina las rutas que opera la empresa (o márcala como RETIRADA).");
        }
        empresaRepository.delete(e);
    }

    private EmpresaTransporteDTO dto(EmpresaTransporte e) {
        return EmpresaTransporteDTO.desde(e, capacidadRepository.findById(e.getEmpresaId()).orElse(null));
    }

    private EmpresaTransporte obtener(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa de transporte no encontrada con Id: " + id));
    }

    private void copiar(EmpresaTransporteRequestDTO dto, EmpresaTransporte e, Long id) {
        if (!Reglas.telefonoValido(dto.contactoTelefono())) throw new BusinessException(Reglas.MSG_TELEFONO);
        if (empresaRepository.contarTelefono(Reglas.digitos(dto.contactoTelefono()), Reglas.idOMenosUno(id)) > 0) {
            throw new DuplicateResourceException("El teléfono " + dto.contactoTelefono().trim() + " ya está registrado para otra empresa.");
        }
        String ruc = dto.ruc().trim();
        Optional<EmpresaTransporte> misma = empresaRepository.findByRuc(ruc);
        if (misma.isPresent() && !misma.get().getEmpresaId().equals(id)) {
            throw new DuplicateResourceException("Ya existe una empresa con el RUC " + ruc + ".");
        }
        e.setNombreComercial(dto.nombreComercial().trim());
        e.setRuc(ruc);
        e.setContactoNombre(vacioANulo(dto.contactoNombre()));
        e.setContactoTelefono(dto.contactoTelefono().trim());
        e.setContactoEmail(dto.contactoEmail() == null || dto.contactoEmail().isBlank() ? null : dto.contactoEmail().trim().toLowerCase());
        e.setDireccion(vacioANulo(dto.direccion()));
        e.setEstadoAfiliacion(dto.estadoAfiliacion());
        e.setDisponibilidadOperativa(dto.disponibilidadOperativa());
        if (dto.fechaAfiliacion() != null) e.setFechaAfiliacion(dto.fechaAfiliacion());
        else if (e.getFechaAfiliacion() == null) e.setFechaAfiliacion(LocalDate.now());
    }

    private String vacioANulo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
