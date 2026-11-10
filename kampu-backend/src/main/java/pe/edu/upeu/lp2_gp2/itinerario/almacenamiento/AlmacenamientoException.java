package pe.edu.upeu.lp2_gp2.itinerario.almacenamiento;

/** Falla de disco al guardar o leer un archivo: HTTP 500 (no es culpa del usuario) */
public class AlmacenamientoException extends RuntimeException {
    public AlmacenamientoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public AlmacenamientoException(String mensaje) {
        super(mensaje);
    }
}
