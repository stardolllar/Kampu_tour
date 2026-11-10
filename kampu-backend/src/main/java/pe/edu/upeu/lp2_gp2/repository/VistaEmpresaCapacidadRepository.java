package pe.edu.upeu.lp2_gp2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.lp2_gp2.entity.VistaEmpresaCapacidad;

// Vista VW_EMPRESA_CAPACIDAD (solo lectura)
@Repository
public interface VistaEmpresaCapacidadRepository extends JpaRepository<VistaEmpresaCapacidad, Long> {
}
