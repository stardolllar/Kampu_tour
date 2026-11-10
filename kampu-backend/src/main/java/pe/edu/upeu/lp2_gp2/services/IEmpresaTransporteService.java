package pe.edu.upeu.lp2_gp2.services;

import pe.edu.upeu.lp2_gp2.dto.EmpresaTransporteDTO;
import pe.edu.upeu.lp2_gp2.dto.EmpresaTransporteRequestDTO;

import java.util.List;

public interface IEmpresaTransporteService {
    List<EmpresaTransporteDTO> listarTodo();
    EmpresaTransporteDTO buscarPorId(Long id);
    EmpresaTransporteDTO crear(EmpresaTransporteRequestDTO dto);
    EmpresaTransporteDTO actualizar(Long id, EmpresaTransporteRequestDTO dto);
    void eliminar(Long id);
}
