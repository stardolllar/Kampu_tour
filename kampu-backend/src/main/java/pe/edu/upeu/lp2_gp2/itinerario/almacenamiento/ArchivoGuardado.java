package pe.edu.upeu.lp2_gp2.itinerario.almacenamiento;

/**
 * @param ruta           ruta RELATIVA dentro del almacenamiento (es lo que se guarda en la BD)
 * @param nombreOriginal nombre que tenía el archivo en el equipo del usuario (saneado)
 * @param bytes          tamaño
 */
public record ArchivoGuardado(String ruta, String nombreOriginal, long bytes) {
}
