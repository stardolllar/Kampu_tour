package pe.edu.upeu.lp2_gp2.itinerario.almacenamiento;

/** Archivo rechazado por una regla de validación (vacío, no es PDF, demasiado grande...): HTTP 400 / 413 */
public class ArchivoInvalidoException extends RuntimeException {
    private final boolean demasiadoGrande;

    public ArchivoInvalidoException(String mensaje) {
        this(mensaje, false);
    }

    public ArchivoInvalidoException(String mensaje, boolean demasiadoGrande) {
        super(mensaje);
        this.demasiadoGrande = demasiadoGrande;
    }

    public boolean isDemasiadoGrande() {
        return demasiadoGrande;
    }
}
