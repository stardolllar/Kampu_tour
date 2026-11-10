package pe.edu.upeu.lp2_gp2.itinerario.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.edu.upeu.lp2_gp2.seguridad.SesionUsuario;
import pe.edu.upeu.lp2_gp2.seguridad.TokenService;

import java.io.IOException;
import java.util.List;

/**
 * Puente entre la sesión propia del sistema (token Bearer de TokenService) y Spring Security:
 * si el token es válido, publica un Authentication con principal = SesionUsuario y
 * autoridad ROLE_<rol> (ROLE_ADMIN, ROLE_VENDEDOR, ROLE_OPERADOR, ROLE_CLIENTE).
 * Con eso @PreAuthorize puede validar rol y propiedad del viaje.
 * No se registra como filtro de servlet suelto: solo vive dentro de la cadena de seguridad.
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final TokenService tokens;

    public TokenAuthenticationFilter(TokenService tokens) {
        this.tokens = tokens;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = req.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            SesionUsuario s = tokens.obtener(cabecera.substring(7).trim());
            if (s != null) {
                var auth = new UsernamePasswordAuthenticationToken(s, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + s.rol())));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        try {
            chain.doFilter(req, resp);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
