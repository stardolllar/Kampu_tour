package pe.edu.upeu.lp2_gp2.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.lp2_gp2.dto.*;
import pe.edu.upeu.lp2_gp2.services.IOperacionTransporteService;

import java.util.List;

/**
 * Tráfico de transporte (OPERACION_TRANSPORTE). El encabezado opcional X-Usuario
 * (username de la sesión) queda registrado en HISTORIAL_TRAFICO_OPERACION.usuario_registro.
 */
@RestController
@RequestMapping("/api/v1/operaciones")
public class OperacionTransporteController {
    private static final String USUARIO = "X-Usuario";

    private final IOperacionTransporteService operacionService;

    public OperacionTransporteController(IOperacionTransporteService operacionService) {
        this.operacionService = operacionService;
    }

    // Tablero (VW_TABLERO_OPERACIONES); ?salidaId= filtra las operaciones de una salida
    @GetMapping
    public ResponseEntity<List<OperacionDTO>> listar(@RequestParam(required = false) Long salidaId) {
        return ResponseEntity.ok(operacionService.listar(salidaId));
    }

    // Catálogo DOMINIO_ESTADOS con sus transiciones permitidas
    @GetMapping("/estados")
    public ResponseEntity<List<EstadoOperacionDTO>> estados() {
        return ResponseEntity.ok(operacionService.estados());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperacionDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(operacionService.buscarPorId(id));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialOperacionDTO>> historial(@PathVariable Long id) {
        return ResponseEntity.ok(operacionService.historial(id));
    }

    // Vista VW_RECURSOS_DISPONIBLES (solo operaciones PROGRAMADO)
    @GetMapping("/{id}/recursos-disponibles")
    public ResponseEntity<List<RecursoDisponibleDTO>> recursosDisponibles(@PathVariable Long id) {
        return ResponseEntity.ok(operacionService.recursosDisponibles(id));
    }

    // Vehículos y conductores de las empresas con esa ruta, con el motivo si no son elegibles
    @GetMapping("/{id}/candidatos")
    public ResponseEntity<List<RecursoCandidatoDTO>> candidatos(@PathVariable Long id) {
        return ResponseEntity.ok(operacionService.candidatos(id));
    }

    @PostMapping
    public ResponseEntity<OperacionDTO> crear(@Valid @RequestBody OperacionRequestDTO dto,
                                              @RequestHeader(value = USUARIO, required = false) String usuario) {
        return new ResponseEntity<>(operacionService.crear(dto, usuario), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OperacionDTO> actualizar(@PathVariable Long id, @Valid @RequestBody OperacionRequestDTO dto,
                                                   @RequestHeader(value = USUARIO, required = false) String usuario) {
        return ResponseEntity.ok(operacionService.actualizar(id, dto, usuario));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<OperacionDTO> cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequestDTO dto,
                                                      @RequestHeader(value = USUARIO, required = false) String usuario) {
        return ResponseEntity.ok(operacionService.cambiarEstado(id, dto, usuario));
    }

    // PKG_ASIGNACION.asignar_automatico
    @PostMapping("/{id}/asignar-automatico")
    public ResponseEntity<OperacionDTO> asignarAutomatico(@PathVariable Long id,
                                                          @RequestHeader(value = USUARIO, required = false) String usuario) {
        return ResponseEntity.ok(operacionService.asignarAutomatico(id, usuario));
    }

    @PatchMapping("/{id}/asignar")
    public ResponseEntity<OperacionDTO> asignar(@PathVariable Long id, @Valid @RequestBody AsignacionRequestDTO dto,
                                                @RequestHeader(value = USUARIO, required = false) String usuario) {
        return ResponseEntity.ok(operacionService.asignar(id, dto, usuario));
    }
}
