package pe.edu.upeu.lp2_gp2.itinerario.entity;

import java.util.List;

import static pe.edu.upeu.lp2_gp2.itinerario.entity.EstadoSegmento.*;

/**
 * Tipo de tarea operativa y su máquina de estados (secuencia estricta que sigue el operador en campo).
 * ALOJAMIENTO es informativo: no tiene vehículo ni operador.
 */
public enum TipoSegmento {
    TRANSFER_IN(List.of(PROGRAMADO, EN_AEROPUERTO, PASAJERO_ABORDADO, EN_TRANSITO_A_HOTEL, DEJADO_EN_HOTEL)),
    TOUR_DIARIO(List.of(PROGRAMADO, RECOGIENDO_HOTEL, RUMBO_AL_DESTINO, EN_ATRACTIVO, RETORNANDO, FINALIZADO_EN_HOTEL)),
    TRANSFER_OUT(List.of(PROGRAMADO, RECOGIENDO_HOTEL, EN_TRANSITO_A_AEROPUERTO, DEJADO_EN_AEROPUERTO)),
    ALOJAMIENTO(List.of(PROGRAMADO));

    private final List<EstadoSegmento> flujo;

    TipoSegmento(List<EstadoSegmento> flujo) {
        this.flujo = flujo;
    }

    public List<EstadoSegmento> flujo() {
        return flujo;
    }

    /** true si se le asigna vehículo y operador (todo menos ALOJAMIENTO) */
    public boolean esOperable() {
        return this != ALOJAMIENTO;
    }

    /** Siguiente estado de la secuencia, o null si ya terminó */
    public EstadoSegmento siguiente(EstadoSegmento actual) {
        int i = flujo.indexOf(actual);
        return i < 0 || i + 1 >= flujo.size() ? null : flujo.get(i + 1);
    }

    public boolean esFinal(EstadoSegmento estado) {
        return flujo.get(flujo.size() - 1) == estado;
    }

    /** Posición en la secuencia (para mostrar el avance y comparar segmentos de un grupo) */
    public int orden(EstadoSegmento estado) {
        return flujo.indexOf(estado);
    }
}
