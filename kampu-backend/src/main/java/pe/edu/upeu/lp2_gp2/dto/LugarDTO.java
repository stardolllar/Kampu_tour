package pe.edu.upeu.lp2_gp2.dto;

import pe.edu.upeu.lp2_gp2.entity.Lugar;

public record LugarDTO(
        Long lugarId,
        String nombre,
        String tipoLugar,
        String ciudad
) {
    public static LugarDTO desde(Lugar l) {
        return new LugarDTO(l.getLugarId(), l.getNombre(), l.getTipoLugar(), l.getCiudad());
    }
}
