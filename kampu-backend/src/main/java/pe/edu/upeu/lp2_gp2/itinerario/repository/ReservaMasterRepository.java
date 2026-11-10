package pe.edu.upeu.lp2_gp2.itinerario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.lp2_gp2.itinerario.entity.ReservaMaster;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface ReservaMasterRepository extends JpaRepository<ReservaMaster, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM ReservaMaster m WHERE m.reservaId = :id")
    Optional<ReservaMaster> bloquear(@Param("id") Long id);
}
