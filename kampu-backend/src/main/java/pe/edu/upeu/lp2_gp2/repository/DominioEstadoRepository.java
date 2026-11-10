package pe.edu.upeu.lp2_gp2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.lp2_gp2.entity.DominioEstado;

import java.util.List;

@Repository
public interface DominioEstadoRepository extends JpaRepository<DominioEstado, String> {
    List<DominioEstado> findAllByOrderByOrdenAsc();
}
