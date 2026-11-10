package pe.edu.upeu.lp2_gp2.itinerario.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import pe.edu.upeu.lp2_gp2.itinerario.service.ItinerarioVentasService;

import java.nio.charset.StandardCharsets;

final class Descargas {
    private Descargas() {
    }

    // PDF en línea (el navegador lo muestra) o como descarga
    static ResponseEntity<Resource> pdf(ItinerarioVentasService.Archivo a, boolean descargar) {
        ContentDisposition cd = (descargar ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(a.nombre(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(a.recurso());
    }
}
