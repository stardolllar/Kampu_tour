package pe.edu.upeu.lp2_gp2.itinerario.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.lp2_gp2.itinerario.dto.AsignacionRequestDTO;
import pe.edu.upeu.lp2_gp2.itinerario.dto.RecursosFidsDTO;
import pe.edu.upeu.lp2_gp2.itinerario.dto.SegmentoDTO;
import pe.edu.upeu.lp2_gp2.itinerario.dto.TableroFidsDTO;
import pe.edu.upeu.lp2_gp2.itinerario.entity.TipoSegmento;
import pe.edu.upeu.lp2_gp2.itinerario.service.ConsolidacionService;

import java.time.LocalDate;
import java.util.List;

/**
 * Torre de control FIDS (solo ROL_ADMIN).
 *   GET    /fids?fecha=&tipo=&lugarId=   grupos consolidados entre reservas (el panel lo consulta cada 15 s)
 *   GET    /fids/recursos                vehículos operativos y operadores activos
 *   POST   /fids/asignaciones            agrupa segmentos en un viaje con vehículo y operador
 *   DELETE /fids/asignaciones/{codigo}   deshace un viaje que aún no empezó
 */
@RestController
@RequestMapping("/api/v1/fids")
@PreAuthorize("hasRole('ADMIN')")
public class FidsController {
    private final ConsolidacionService service;

    public FidsController(ConsolidacionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<TableroFidsDTO> tablero(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) TipoSegmento tipo,
            @RequestParam(required = false) Long lugarId) {
        return ResponseEntity.ok(service.tablero(fecha == null ? LocalDate.now() : fecha, tipo, lugarId));
    }

    @GetMapping("/recursos")
    public ResponseEntity<RecursosFidsDTO> recursos() {
        return ResponseEntity.ok(service.recursos());
    }

    @PostMapping("/asignaciones")
    public ResponseEntity<List<SegmentoDTO>> asignar(@Valid @RequestBody AsignacionRequestDTO req) {
        return ResponseEntity.ok(service.asignar(req));
    }

    @DeleteMapping("/asignaciones/{codigo}")
    public ResponseEntity<Void> liberar(@PathVariable String codigo) {
        service.liberar(codigo);
        return ResponseEntity.noContent().build();
    }
}
