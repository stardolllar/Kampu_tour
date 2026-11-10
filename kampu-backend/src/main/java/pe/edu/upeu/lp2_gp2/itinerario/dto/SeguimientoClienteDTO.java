package pe.edu.upeu.lp2_gp2.itinerario.dto;

import java.util.List;

// Portal del pasajero: itinerario con estado en vivo, vuelos (PDF) y comprobante
public record SeguimientoClienteDTO(Long reservaId, String paquete, String estadoLogistico, String hotel,
                                    String mensajeEnVivo, List<SegmentoDTO> segmentos, List<BoletoDTO> boletos) {
}
