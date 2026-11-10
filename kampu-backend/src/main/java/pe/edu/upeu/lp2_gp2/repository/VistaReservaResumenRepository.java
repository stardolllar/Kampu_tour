package pe.edu.upeu.lp2_gp2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.lp2_gp2.entity.VistaReservaResumen;

// Vista V_RESERVAS_RESUMEN (solo lectura)
@Repository
public interface VistaReservaResumenRepository extends JpaRepository<VistaReservaResumen, Long> {
}
