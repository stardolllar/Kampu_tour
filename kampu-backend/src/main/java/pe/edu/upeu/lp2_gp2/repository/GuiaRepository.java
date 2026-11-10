package pe.edu.upeu.lp2_gp2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.lp2_gp2.entity.Guia;

import java.util.List;

@Repository
public interface GuiaRepository extends JpaRepository<Guia, Long> {
    List<Guia> findAllByOrderByGuiaIdAsc();

    // Teléfono repetido comparando solo los dígitos (mismo criterio que el índice único de 02_extension_kampu.sql).
    // idExcluir = -1 al crear.
    @Query(value = "SELECT COUNT(*) FROM GUIAS WHERE TRANSLATE(telefono, '0+-() ', '0') = :digitos AND guia_id <> :idExcluir",
            nativeQuery = true)
    long contarTelefono(@Param("digitos") String digitos, @Param("idExcluir") Long idExcluir);
}
