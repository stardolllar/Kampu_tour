package pe.edu.upeu.lp2_gp2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.lp2_gp2.dto.AuditoriaDTO;
import pe.edu.upeu.lp2_gp2.dto.ResumenPaqueteDTO;
import pe.edu.upeu.lp2_gp2.repository.AuditoriaRepository;
import pe.edu.upeu.lp2_gp2.repository.VistaResumenPaqueteRepository;

import java.util.List;

// Vistas internas por rol: VW_RESUMEN_PAQUETES_POR_ROL y la AUDITORIA (solo ADMIN)
@RestController
public class ResumenPaquetesController {
    private final VistaResumenPaqueteRepository resumenRepository;
    private final AuditoriaRepository auditoriaRepository;

    public ResumenPaquetesController(VistaResumenPaqueteRepository resumenRepository, AuditoriaRepository auditoriaRepository) {
        this.resumenRepository = resumenRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    // ADMIN/VENDEDOR: todas las salidas con montos; OPERADOR: su cola (por empezar / en curso) sin montos
    @GetMapping("/api/v1/resumen-paquetes")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ResumenPaqueteDTO>> resumen() {
        return ResponseEntity.ok(resumenRepository.findAllByOrderByFechaInicioAsc().stream().map(ResumenPaqueteDTO::desde).toList());
    }

    @GetMapping("/api/v1/auditoria")
    @Transactional(readOnly = true)
    public ResponseEntity<List<AuditoriaDTO>> auditoria() {
        return ResponseEntity.ok(auditoriaRepository.findTop300ByOrderByAuditoriaIdDesc().stream().map(AuditoriaDTO::desde).toList());
    }
}
