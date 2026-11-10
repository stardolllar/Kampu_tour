package pe.edu.upeu.lp2_gp2.services;

import pe.edu.upeu.lp2_gp2.dto.*;

import java.util.List;

public interface IOperacionTransporteService {
    List<OperacionDTO> listar(Long salidaId);
    OperacionDTO buscarPorId(Long id);
    OperacionDTO crear(OperacionRequestDTO dto, String usuario);
    OperacionDTO actualizar(Long id, OperacionRequestDTO dto, String usuario);
    OperacionDTO cambiarEstado(Long id, CambioEstadoRequestDTO dto, String usuario);
    OperacionDTO asignarAutomatico(Long id, String usuario);
    OperacionDTO asignar(Long id, AsignacionRequestDTO dto, String usuario);
    List<HistorialOperacionDTO> historial(Long id);
    List<RecursoDisponibleDTO> recursosDisponibles(Long id);
    List<RecursoCandidatoDTO> candidatos(Long id);
    List<EstadoOperacionDTO> estados();
}
