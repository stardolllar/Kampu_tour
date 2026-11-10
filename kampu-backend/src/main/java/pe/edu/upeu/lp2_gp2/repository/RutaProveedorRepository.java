package pe.edu.upeu.lp2_gp2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.lp2_gp2.entity.RutaProveedor;

import java.util.List;
import java.util.Optional;

@Repository
public interface RutaProveedorRepository extends JpaRepository<RutaProveedor, Long> {
    List<RutaProveedor> findAllByOrderByRutaIdAsc();

    Optional<RutaProveedor> findByEmpresaIdAndOrigenIdAndDestinoId(Long empresaId, Long origenId, Long destinoId);

    boolean existsByEmpresaId(Long empresaId);

    boolean existsByOrigenIdOrDestinoId(Long origenId, Long destinoId);
}
