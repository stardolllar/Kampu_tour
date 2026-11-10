package pe.edu.upeu.lp2_gp2.dto;

import pe.edu.upeu.lp2_gp2.entity.RutaProveedor;

import java.math.BigDecimal;

public record RutaProveedorDTO(
        Long rutaId,
        Long empresaId,
        Long origenId,
        Long destinoId,
        String tipoServicio,
        Integer duracionEstimadaMin,
        BigDecimal tarifaReferencial,
        String estado
) {
    public static RutaProveedorDTO desde(RutaProveedor r) {
        return new RutaProveedorDTO(r.getRutaId(), r.getEmpresaId(), r.getOrigenId(), r.getDestinoId(), r.getTipoServicio(),
                r.getDuracionEstimadaMin(), r.getTarifaReferencial(), r.getEstado());
    }
}
