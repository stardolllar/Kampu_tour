package pe.edu.upeu.lp2_gp2.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.lp2_gp2.dto.ConductorDTO;
import pe.edu.upeu.lp2_gp2.dto.ConductorRequestDTO;
import pe.edu.upeu.lp2_gp2.services.IConductorService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conductores")
public class ConductorController {
    private final IConductorService conductorService;

    public ConductorController(IConductorService conductorService) {
        this.conductorService = conductorService;
    }

    @GetMapping
    public ResponseEntity<List<ConductorDTO>> listar() {
        return ResponseEntity.ok(conductorService.listarTodo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConductorDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(conductorService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ConductorDTO> crear(@Valid @RequestBody ConductorRequestDTO dto) {
        return new ResponseEntity<>(conductorService.crear(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConductorDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ConductorRequestDTO dto) {
        return ResponseEntity.ok(conductorService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        conductorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
