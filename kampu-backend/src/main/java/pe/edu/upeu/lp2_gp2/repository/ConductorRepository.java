package pe.edu.upeu.lp2_gp2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.lp2_gp2.entity.Conductor;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConductorRepository extends JpaRepository<Conductor, Long> {
    List<Conductor> findAllByOrderByConductorIdAsc();

    Optional<Conductor> findByNumeroLicenciaIgnoreCase(String numeroLicencia);

    boolean existsByEmpresaId(Long empresaId);
}
