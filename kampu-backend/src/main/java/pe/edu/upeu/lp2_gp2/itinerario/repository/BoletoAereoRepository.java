package pe.edu.upeu.lp2_gp2.itinerario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.lp2_gp2.itinerario.entity.BoletoAereo;
import pe.edu.upeu.lp2_gp2.itinerario.entity.TipoVuelo;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoletoAereoRepository extends JpaRepository<BoletoAereo, Long> {
    Optional<BoletoAereo> findByReservaMasterReservaIdAndTipoVuelo(Long reservaId, TipoVuelo tipoVuelo);

    List<BoletoAereo> findByReservaMasterReservaIdOrderByTipoVueloAsc(Long reservaId);
}
