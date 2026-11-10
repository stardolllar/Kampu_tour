package pe.edu.upeu.lp2_gp2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.lp2_gp2.entity.Conductor;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConductorRepository extends JpaRepository<Conductor, Long> {
    List<Conductor> findAllByOrderByConductorIdAsc();

    Optional<Conductor> findByNumeroLicenciaIgnoreCase(String numeroLicencia);

    boolean existsByEmpresaId(Long empresaId);

    // Teléfono repetido comparando solo los dígitos (mismo criterio que el índice único de 02_extension_kampu.sql).
    // idExcluir = -1 al crear.
    @Query(value = "SELECT COUNT(*) FROM CONDUCTOR WHERE TRANSLATE(telefono, '0+-() ', '0') = :digitos AND conductor_id <> :idExcluir",
            nativeQuery = true)
    long contarTelefono(@Param("digitos") String digitos, @Param("idExcluir") Long idExcluir);
}
