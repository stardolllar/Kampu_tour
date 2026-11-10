package pe.edu.upeu.lp2_gp2.itinerario.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pe.edu.upeu.lp2_gp2.seguridad.TokenService;

/**
 * Spring Security para el motor de itinerarios, sin cambiar el resto del sistema:
 *  - API sin estado (sin sesión HTTP, sin CSRF, sin formulario ni Basic).
 *  - Las URLs se dejan pasar (permitAll): el control por URL sigue en SeguridadInterceptor
 *    (PKG_SEGURIDAD_ROLES), igual que antes.
 *  - @EnableMethodSecurity: los controladores del módulo usan @PreAuthorize para el rol y,
 *    en el operador, para verificar que el viaje le pertenece.
 */
@Configuration
@EnableMethodSecurity
public class SeguridadSpringConfig {

    @Bean
    public SecurityFilterChain cadenaSeguridad(HttpSecurity http, TokenService tokens) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(new TokenAuthenticationFilter(tokens), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
