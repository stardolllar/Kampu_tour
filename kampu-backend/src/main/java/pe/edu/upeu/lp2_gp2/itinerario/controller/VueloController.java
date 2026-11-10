package pe.edu.upeu.lp2_gp2.itinerario.controller;

import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.lp2_gp2.itinerario.dto.BoletoRequestDTO;
import pe.edu.upeu.lp2_gp2.itinerario.dto.ItinerarioDTO;
import pe.edu.upeu.lp2_gp2.itinerario.dto.ItinerarioRequestDTO;
import pe.edu.upeu.lp2_gp2.itinerario.service.ItinerarioVentasService;

import java.util.List;

/**
 * Booking y vuelos (ROL_VENTAS y ROL_ADMIN).
 *   GET  /itinerarios                      reservas con su estado logístico
 *   GET  /itinerarios/{reservaId}          itinerario completo (segmentos y boletos)
 *   PUT  /itinerarios/{reservaId}          abrir/actualizar: hotel, aeropuerto y tours por día
 *   POST /itinerarios/{reservaId}/boletos  multipart: "datos" (JSON) + "archivo" (PDF)
 *   GET  /itinerarios/boletos/{id}/pdf     ver o descargar el PDF (?descargar=true)
 */
@RestController
@RequestMapping("/api/v1/itinerarios")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
public class VueloController {
    private final ItinerarioVentasService service;

    public VueloController(ItinerarioVentasService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ItinerarioDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{reservaId}")
    public ResponseEntity<ItinerarioDTO> detalle(@PathVariable Long reservaId) {
        return ResponseEntity.ok(service.detalle(reservaId));
    }

    @PutMapping("/{reservaId}")
    public ResponseEntity<ItinerarioDTO> guardar(@PathVariable Long reservaId, @Valid @RequestBody ItinerarioRequestDTO req) {
        return ResponseEntity.ok(service.guardarItinerario(reservaId, req));
    }

    @PostMapping(value = "/{reservaId}/boletos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItinerarioDTO> registrarBoleto(@PathVariable Long reservaId,
                                                         @Valid @RequestPart("datos") BoletoRequestDTO datos,
                                                         @RequestPart(value = "archivo", required = false) MultipartFile archivo) {
        return ResponseEntity.ok(service.registrarBoleto(reservaId, datos, archivo));
    }

    @GetMapping("/boletos/{boletoId}/pdf")
    public ResponseEntity<Resource> pdf(@PathVariable Long boletoId, @RequestParam(defaultValue = "false") boolean descargar) {
        return Descargas.pdf(service.pdf(boletoId), descargar);
    }
}
