package pe.edu.upeu.lp2_gp2.dto;

import pe.edu.upeu.lp2_gp2.entity.Auditoria;

import java.time.LocalDateTime;

public record AuditoriaDTO(
        Long auditoriaId,
        String entidad,
        Long entidadId,
        String campo,
        String valorAnterior,
        String valorNuevo,
        String usuario,
        String rolUsuario,
        LocalDateTime fecha
) {
    public static AuditoriaDTO desde(Auditoria a) {
        return new AuditoriaDTO(a.getAuditoriaId(), a.getEntidad(), a.getEntidadId(), a.getCampo(), a.getValorAnterior(),
                a.getValorNuevo(), a.getUsuario(), a.getRolUsuario(), a.getFecha());
    }
}
