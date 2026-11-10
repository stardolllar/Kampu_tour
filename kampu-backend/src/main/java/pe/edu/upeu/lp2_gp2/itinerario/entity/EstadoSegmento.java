package pe.edu.upeu.lp2_gp2.itinerario.entity;

/** Estados de campo de un segmento (ver TipoSegmento.flujo para la secuencia válida de cada tipo) */
public enum EstadoSegmento {
    PROGRAMADO,
    // Transfer In
    EN_AEROPUERTO, PASAJERO_ABORDADO, EN_TRANSITO_A_HOTEL, DEJADO_EN_HOTEL,
    // Tour diario
    RECOGIENDO_HOTEL, RUMBO_AL_DESTINO, EN_ATRACTIVO, RETORNANDO, FINALIZADO_EN_HOTEL,
    // Transfer Out
    EN_TRANSITO_A_AEROPUERTO, DEJADO_EN_AEROPUERTO;

    /** Para el portal del cliente: el vehículo ya va (o espera) por el pasajero */
    public boolean choferEnCamino() {
        return this == EN_AEROPUERTO || this == RECOGIENDO_HOTEL;
    }
}
