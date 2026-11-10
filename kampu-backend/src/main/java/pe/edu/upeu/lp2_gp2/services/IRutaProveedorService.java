package pe.edu.upeu.lp2_gp2.services;

import pe.edu.upeu.lp2_gp2.dto.RutaProveedorDTO;
import pe.edu.upeu.lp2_gp2.dto.RutaProveedorRequestDTO;

import java.util.List;

public interface IRutaProveedorService {
    List<RutaProveedorDTO> listarTodo();
    RutaProveedorDTO buscarPorId(Long id);
    RutaProveedorDTO crear(RutaProveedorRequestDTO dto);
    RutaProveedorDTO actualizar(Long id, RutaProveedorRequestDTO dto);
    void eliminar(Long id);
}
