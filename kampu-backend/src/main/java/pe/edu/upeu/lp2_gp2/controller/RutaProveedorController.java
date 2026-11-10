package pe.edu.upeu.lp2_gp2.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.lp2_gp2.dto.RutaProveedorDTO;
import pe.edu.upeu.lp2_gp2.dto.RutaProveedorRequestDTO;
import pe.edu.upeu.lp2_gp2.services.IRutaProveedorService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rutas")
public class RutaProveedorController {
    private final IRutaProveedorService rutaService;

    public RutaProveedorController(IRutaProveedorService rutaService) {
        this.rutaService = rutaService;
    }

    @GetMapping
    public ResponseEntity<List<RutaProveedorDTO>> listar() {
        return ResponseEntity.ok(rutaService.listarTodo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RutaProveedorDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(rutaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<RutaProveedorDTO> crear(@Valid @RequestBody RutaProveedorRequestDTO dto) {
        return new ResponseEntity<>(rutaService.crear(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RutaProveedorDTO> actualizar(@PathVariable Long id, @Valid @RequestBody RutaProveedorRequestDTO dto) {
        return ResponseEntity.ok(rutaService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        rutaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
