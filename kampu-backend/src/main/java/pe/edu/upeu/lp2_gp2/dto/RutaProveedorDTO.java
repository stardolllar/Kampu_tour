package pe.edu.upeu.lp2_gp2.dto;

import pe.edu.upeu.lp2_gp2.entity.RutaProveedor;

import java.math.BigDecimal;

public record RutaProveedorDTO(
        Long rutaId,
        Long empresaId,
        Long origenId,
        Long destinoId,
        Long paqueteId,
        String tipoServicio,
        Integer duracionEstimadaMin,
        BigDecimal tarifaReferencial,
        String estado,
        // Salida programada creada automáticamente junto con la ruta (solo al crear una ruta hacia un paquete)
        Long salidaCreadaId
) {
    public static RutaProveedorDTO desde(RutaProveedor r) {
        return desde(r, null);
    }

    public static RutaProveedorDTO desde(RutaProveedor r, Long salidaCreadaId) {
        return new RutaProveedorDTO(r.getRutaId(), r.getEmpresaId(), r.getOrigenId(), r.getDestinoId(), r.getPaqueteId(),
                r.getTipoServicio(), r.getDuracionEstimadaMin(), r.getTarifaReferencial(), r.getEstado(), salidaCreadaId);
    }
}
