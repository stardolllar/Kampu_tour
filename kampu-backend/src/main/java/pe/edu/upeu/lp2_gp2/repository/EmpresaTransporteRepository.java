package pe.edu.upeu.lp2_gp2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.lp2_gp2.entity.EmpresaTransporte;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaTransporteRepository extends JpaRepository<EmpresaTransporte, Long> {
    List<EmpresaTransporte> findAllByOrderByEmpresaIdAsc();

    Optional<EmpresaTransporte> findByRuc(String ruc);

    // Teléfono repetido comparando solo los dígitos (mismo criterio que el índice único de 02_extension_kampu.sql).
    // idExcluir = -1 al crear.
    @Query(value = "SELECT COUNT(*) FROM EMPRESA_TRANSPORTE WHERE TRANSLATE(contacto_telefono, '0+-() ', '0') = :digitos AND empresa_id <> :idExcluir",
            nativeQuery = true)
    long contarTelefono(@Param("digitos") String digitos, @Param("idExcluir") Long idExcluir);
}
