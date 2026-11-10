package pe.edu.upeu.lp2_gp2.itinerario.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.lp2_gp2.itinerario.dto.SeguimientoClienteDTO;
import pe.edu.upeu.lp2_gp2.itinerario.service.SeguimientoClienteService;
import pe.edu.upeu.lp2_gp2.seguridad.SesionUsuario;

/**
 * Portal del pasajero (solo ROL_CLIENTE; cada método verifica que la reserva sea suya).
 *   GET /portal/itinerarios/{reservaId}               itinerario en vivo (el portal consulta cada 20 s)
 *   GET /portal/itinerarios/{reservaId}/comprobante   comprobante HTML de la reserva
 *   GET /portal/boletos/{boletoId}/pdf                PDF de su boleto aéreo
 */
@RestController
@RequestMapping("/api/v1/portal")
@PreAuthorize("hasRole('CLIENTE')")
public class SeguimientoClienteController {
    private final SeguimientoClienteService service;

    public SeguimientoClienteController(SeguimientoClienteService service) {
        this.service = service;
    }

    @GetMapping("/itinerarios/{reservaId}")
    public ResponseEntity<SeguimientoClienteDTO> seguimiento(@PathVariable Long reservaId, Authentication auth) {
        return ResponseEntity.ok(service.seguimiento(reservaId, clienteId(auth)));
    }

    @GetMapping(value = "/itinerarios/{reservaId}/comprobante", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> comprobante(@PathVariable Long reservaId, Authentication auth) {
        return ResponseEntity.ok(service.comprobante(reservaId, clienteId(auth)));
    }

    @GetMapping("/boletos/{boletoId}/pdf")
    public ResponseEntity<Resource> pdf(@PathVariable Long boletoId, @RequestParam(defaultValue = "false") boolean descargar,
                                        Authentication auth) {
        return Descargas.pdf(service.pdf(boletoId, clienteId(auth)), descargar);
    }

    private static Long clienteId(Authentication auth) {
        return auth != null && auth.getPrincipal() instanceof SesionUsuario s ? s.clienteId() : null;
    }
}
