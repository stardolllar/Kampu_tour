package pe.edu.upeu.lp2_gp2.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.lp2_gp2.dto.EmpresaTransporteDTO;
import pe.edu.upeu.lp2_gp2.dto.EmpresaTransporteRequestDTO;
import pe.edu.upeu.lp2_gp2.services.IEmpresaTransporteService;

import java.util.List;

// Empresas proveedoras de transporte (con la capacidad de VW_EMPRESA_CAPACIDAD)
@RestController
@RequestMapping("/api/v1/empresas")
public class EmpresaTransporteController {
    private final IEmpresaTransporteService empresaService;

    public EmpresaTransporteController(IEmpresaTransporteService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    public ResponseEntity<List<EmpresaTransporteDTO>> listar() {
        return ResponseEntity.ok(empresaService.listarTodo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaTransporteDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(empresaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<EmpresaTransporteDTO> crear(@Valid @RequestBody EmpresaTransporteRequestDTO dto) {
        return new ResponseEntity<>(empresaService.crear(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpresaTransporteDTO> actualizar(@PathVariable Long id, @Valid @RequestBody EmpresaTransporteRequestDTO dto) {
        return ResponseEntity.ok(empresaService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        empresaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
