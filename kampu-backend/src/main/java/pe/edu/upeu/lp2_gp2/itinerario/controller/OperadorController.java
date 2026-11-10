package pe.edu.upeu.lp2_gp2.itinerario.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.lp2_gp2.itinerario.dto.EstadoRequestDTO;
import pe.edu.upeu.lp2_gp2.itinerario.dto.ViajeOperadorDTO;
import pe.edu.upeu.lp2_gp2.itinerario.seguridad.SegmentoSeguridad;
import pe.edu.upeu.lp2_gp2.itinerario.service.OperadorCampoService;

import java.util.List;

/**
 * App del operador (solo ROL_OPERADOR). Cada cambio exige que el viaje esté asignado al
 * operador autenticado: lo comprueba Spring Security ANTES de entrar al método.
 *   GET   /operador/jornada                     mis viajes de hoy con manifiesto y ruta
 *   PATCH /operador/viajes/{codigo}/estado       avanza el viaje completo (vehículo y pasajeros)
 *   PATCH /operador/segmentos/{id}/estado        avanza una sola reserva del viaje
 */
@RestController
@RequestMapping("/api/v1/operador")
@PreAuthorize("hasRole('OPERADOR')")
public class OperadorController {
    private final OperadorCampoService service;

    public OperadorController(OperadorCampoService service) {
        this.service = service;
    }

    @GetMapping("/jornada")
    public ResponseEntity<List<ViajeOperadorDTO>> jornada(Authentication auth) {
        return ResponseEntity.ok(service.jornada(SegmentoSeguridad.usuarioId(auth)));
    }

    @PatchMapping("/viajes/{codigo}/estado")
    @PreAuthorize("hasRole('OPERADOR') and @segmentoSeguridad.esOperadorDelGrupo(#codigo, authentication)")
    public ResponseEntity<ViajeOperadorDTO> avanzarViaje(@PathVariable String codigo, @Valid @RequestBody EstadoRequestDTO req,
                                                         Authentication auth) {
        return ResponseEntity.ok(service.avanzarGrupo(codigo, req, SegmentoSeguridad.usuarioId(auth)));
    }

    @PatchMapping("/segmentos/{id}/estado")
    @PreAuthorize("hasRole('OPERADOR') and @segmentoSeguridad.esOperadorDelSegmento(#id, authentication)")
    public ResponseEntity<ViajeOperadorDTO> avanzarSegmento(@PathVariable Long id, @Valid @RequestBody EstadoRequestDTO req,
                                                            Authentication auth) {
        return ResponseEntity.ok(service.avanzarSegmento(id, req, SegmentoSeguridad.usuarioId(auth)));
    }
}
