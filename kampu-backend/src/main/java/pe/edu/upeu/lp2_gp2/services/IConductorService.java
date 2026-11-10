package pe.edu.upeu.lp2_gp2.services;

import pe.edu.upeu.lp2_gp2.dto.ConductorDTO;
import pe.edu.upeu.lp2_gp2.dto.ConductorRequestDTO;

import java.util.List;

public interface IConductorService {
    List<ConductorDTO> listarTodo();
    ConductorDTO buscarPorId(Long id);
    ConductorDTO crear(ConductorRequestDTO dto);
    ConductorDTO actualizar(Long id, ConductorRequestDTO dto);
    void eliminar(Long id);
}
