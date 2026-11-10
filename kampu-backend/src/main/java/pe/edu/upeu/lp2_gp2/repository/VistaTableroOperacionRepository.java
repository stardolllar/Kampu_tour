package pe.edu.upeu.lp2_gp2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.lp2_gp2.entity.VistaTableroOperacion;

// Vista VW_TABLERO_OPERACIONES (solo lectura)
@Repository
public interface VistaTableroOperacionRepository extends JpaRepository<VistaTableroOperacion, Long> {
}
