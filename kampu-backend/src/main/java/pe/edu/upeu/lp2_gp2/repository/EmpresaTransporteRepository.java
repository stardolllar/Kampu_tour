package pe.edu.upeu.lp2_gp2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.lp2_gp2.entity.EmpresaTransporte;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaTransporteRepository extends JpaRepository<EmpresaTransporte, Long> {
    List<EmpresaTransporte> findAllByOrderByEmpresaIdAsc();

    Optional<EmpresaTransporte> findByRuc(String ruc);
}
