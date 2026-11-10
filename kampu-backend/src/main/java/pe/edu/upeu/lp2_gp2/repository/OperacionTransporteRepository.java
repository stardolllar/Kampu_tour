package pe.edu.upeu.lp2_gp2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.lp2_gp2.entity.OperacionTransporte;

import java.util.List;

@Repository
public interface OperacionTransporteRepository extends JpaRepository<OperacionTransporte, Long> {
    List<OperacionTransporte> findAllByOrderByHoraProgramadaSalidaAscOperacionIdAsc();

    List<OperacionTransporte> findBySalidaIdOrderByHoraProgramadaSalidaAscOperacionIdAsc(Long salidaId);

    boolean existsBySalidaId(Long salidaId);

    boolean existsByEmpresaId(Long empresaId);

    boolean existsByVehiculoId(Long vehiculoId);

    boolean existsByConductorId(Long conductorId);

    boolean existsByOrigenIdOrDestinoId(Long origenId, Long destinoId);

    boolean existsByEmpresaIdAndOrigenIdAndDestinoId(Long empresaId, Long origenId, Long destinoId);
}
