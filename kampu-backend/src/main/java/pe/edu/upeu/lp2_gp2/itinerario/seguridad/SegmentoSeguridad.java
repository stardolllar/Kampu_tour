package pe.edu.upeu.lp2_gp2.itinerario.seguridad;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.lp2_gp2.itinerario.entity.SegmentoOperativo;
import pe.edu.upeu.lp2_gp2.itinerario.repository.SegmentoOperativoRepository;
import pe.edu.upeu.lp2_gp2.seguridad.SesionUsuario;

import java.util.List;

/**
 * Reglas de propiedad que usan los @PreAuthorize del operador, p. ej.:
 *   @PreAuthorize("hasRole('OPERADOR') and @segmentoSeguridad.esOperadorDelGrupo(#codigo, authentication)")
 * Un operador solo puede tocar viajes (grupos) y segmentos que el Admin le asignó a ÉL.
 */
@Component("segmentoSeguridad")
public class SegmentoSeguridad {
    private final SegmentoOperativoRepository segmentos;

    public SegmentoSeguridad(SegmentoOperativoRepository segmentos) {
        this.segmentos = segmentos;
    }

    @Transactional(readOnly = true)
    public boolean esOperadorDelGrupo(String codigo, Authentication auth) {
        Long yo = usuarioId(auth);
        if (yo == null || codigo == null) return false;
        List<SegmentoOperativo> lista = segmentos.findByGrupoCodigoOrderByHoraInicioAscSegmentoIdAsc(codigo);
        return !lista.isEmpty() && lista.stream().allMatch(s -> esMio(s, yo));
    }

    @Transactional(readOnly = true)
    public boolean esOperadorDelSegmento(Long segmentoId, Authentication auth) {
        Long yo = usuarioId(auth);
        if (yo == null || segmentoId == null) return false;
        return segmentos.findById(segmentoId).map(s -> esMio(s, yo)).orElse(false);
    }

    private static boolean esMio(SegmentoOperativo s, Long yo) {
        return s.getOperador() != null && yo.equals(s.getOperador().getUsuarioId());
    }

    public static Long usuarioId(Authentication auth) {
        return auth != null && auth.getPrincipal() instanceof SesionUsuario s ? s.usuarioId() : null;
    }
}
