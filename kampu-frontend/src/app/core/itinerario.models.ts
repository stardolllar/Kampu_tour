// Tipos del Motor de Itinerarios (mismos nombres JSON que los DTO de pe.edu.upeu.lp2_gp2.itinerario)

export type TipoSegmento = 'TRANSFER_IN' | 'TOUR_DIARIO' | 'TRANSFER_OUT' | 'ALOJAMIENTO';
export type EstadoLogistico = 'VUELO_PENDIENTE' | 'CONFIRMADO_CON_VUELO' | 'EN_CURSO' | 'FINALIZADO' | 'CANCELADO';
export type TipoVuelo = 'LLEGADA' | 'SALIDA';
export type Aerolinea = 'LATAM' | 'SKY' | 'JETSMART' | 'OTRA';
export type EstadoSegmento =
  | 'PROGRAMADO'
  | 'EN_AEROPUERTO' | 'PASAJERO_ABORDADO' | 'EN_TRANSITO_A_HOTEL' | 'DEJADO_EN_HOTEL'
  | 'RECOGIENDO_HOTEL' | 'RUMBO_AL_DESTINO' | 'EN_ATRACTIVO' | 'RETORNANDO' | 'FINALIZADO_EN_HOTEL'
  | 'EN_TRANSITO_A_AEROPUERTO' | 'DEJADO_EN_AEROPUERTO';

export interface BoletoAereo {
  boletoId: number;
  reservaId: number;
  tipoVuelo: TipoVuelo;
  aerolinea: Aerolinea;
  numeroVuelo: string;
  codigoPnr: string;
  aeropuertoOrigen: string;
  aeropuertoDestino: string;
  fechaHoraSalida: string;
  fechaHoraLlegada: string;
  tienePdf: boolean;
  nombreArchivo: string | null;
  tamanioBytes: number | null;
  registradoPor: string | null;
  fechaRegistro: string;
}

// Parte "datos" del multipart (el PDF va aparte como "archivo")
export interface BoletoForm {
  tipoVuelo: TipoVuelo;
  aerolinea: Aerolinea;
  numeroVuelo: string;
  codigoPnr: string;
  aeropuertoOrigen: string;
  aeropuertoDestino: string;
  fechaHoraSalida: string;   // yyyy-MM-ddTHH:mm
  fechaHoraLlegada: string;
}

export interface Segmento {
  segmentoId: number;
  reservaId: number;
  tipo: TipoSegmento;
  fecha: string;
  horaInicio: string;
  origenId: number;
  origen: string;
  destinoId: number;
  destino: string;
  pax: number;
  estado: EstadoSegmento;
  siguienteEstado: EstadoSegmento | null;
  paso: number;
  pasos: number;
  choferEnCamino: boolean;
  grupoCodigo: string | null;
  placa: string | null;
  operador: string | null;
  vuelo: string | null;
  estadoActualizadoEn: string | null;
}

export interface Itinerario {
  reservaId: number;
  cliente: string;
  paquete: string;
  fechaInicio: string | null;
  fechaFin: string | null;
  pax: number;
  estadoReserva: string;
  estadoLogistico: EstadoLogistico | null;   // null = sin itinerario todavía
  hotelId: number | null;
  hotel: string | null;
  aeropuertoId: number | null;
  aeropuerto: string | null;
  boletos: BoletoAereo[];
  segmentos: Segmento[];
}

export interface ItinerarioForm {
  hotelId: number | null;
  aeropuertoId: number | null;
  tours: { destinoId: number | null; horaRecojo: string }[];
}

// ---------- FIDS ----------
export interface SegmentoFids {
  segmentoId: number;
  tipo: TipoSegmento;
  fecha: string;
  horaInicio: string;
  estado: EstadoSegmento;
  pax: number;
  grupoCodigo: string | null;
  reservaId: number;
  cliente: string;
  origenId: number;
  origen: string;
  destinoId: number;
  destino: string;
  vuelo: string | null;
  pnr: string | null;
  vehiculoId: number | null;
  placa: string | null;
  capacidad: number | null;
  operadorId: number | null;
  operador: string | null;
}

export interface GrupoFids {
  clave: string;
  tipo: TipoSegmento;
  lugarId: number;
  lugar: string;
  desde: string;
  hasta: string;
  pax: number;
  reservas: number;
  vuelos: string[];
  estado: EstadoSegmento;
  asignado: boolean;
  excedeCapacidad: boolean;
  grupoCodigo: string | null;
  vehiculoId: number | null;
  placa: string | null;
  capacidad: number | null;
  operadorId: number | null;
  operador: string | null;
  segmentos: SegmentoFids[];
}

export interface TableroFids {
  fecha: string;
  generadoEn: string;
  segmentos: number;
  pax: number;
  sinAsignar: number;
  grupos: GrupoFids[];
}

export interface RecursosFids {
  vehiculos: { vehiculoId: number; placa: string; tipo: string; capacidad: number }[];
  operadores: { usuarioId: number; username: string }[];
}

// ---------- Operador ----------
export interface Parada {
  orden: number;
  tipo: 'INICIO' | 'RECOJO' | 'ENTREGA' | 'DESTINO';
  lugarId: number;
  lugar: string;
  latitud: number | null;
  longitud: number | null;
  minutosDesdeAnterior: number | null;
  segmentoIds: number[];
  pasajeros: string[];
}

export interface ViajeOperador {
  grupoCodigo: string;
  tipo: TipoSegmento;
  horaInicio: string;
  placa: string | null;
  capacidad: number | null;
  estado: EstadoSegmento;
  siguienteEstado: EstadoSegmento | null;
  paso: number;
  pasos: number;
  pax: number;
  vuelos: string[];
  fuenteRuta: 'OSRM' | 'ESTIMADA' | 'SIN_COORDENADAS';
  minutosTotales: number;
  ruta: Parada[];
  manifiesto: {
    segmentoId: number; reservaId: number; cliente: string; hotel: string; pax: number;
    pasajeros: string[]; vuelo: string | null; estado: EstadoSegmento; siguienteEstado: EstadoSegmento | null;
  }[];
}

// ---------- Portal del pasajero ----------
export interface SeguimientoCliente {
  reservaId: number;
  paquete: string;
  estadoLogistico: EstadoLogistico | null;
  hotel: string | null;
  mensajeEnVivo: string | null;
  segmentos: Segmento[];
  boletos: BoletoAereo[];
}

// Textos para la interfaz
export const TEXTO_ESTADO: Record<string, string> = {
  PROGRAMADO: 'Programado',
  EN_AEROPUERTO: 'En aeropuerto', PASAJERO_ABORDADO: 'Pasajero abordado', EN_TRANSITO_A_HOTEL: 'En tránsito a hotel',
  DEJADO_EN_HOTEL: 'Dejado en hotel', RECOGIENDO_HOTEL: 'Recogiendo en hotel', RUMBO_AL_DESTINO: 'Rumbo al destino',
  EN_ATRACTIVO: 'En el atractivo', RETORNANDO: 'Retornando', FINALIZADO_EN_HOTEL: 'Finalizado en hotel',
  EN_TRANSITO_A_AEROPUERTO: 'En tránsito al aeropuerto', DEJADO_EN_AEROPUERTO: 'Dejado en aeropuerto',
  VUELO_PENDIENTE: 'Vuelo pendiente', CONFIRMADO_CON_VUELO: 'Confirmado con vuelo', EN_CURSO: 'En curso',
  FINALIZADO: 'Finalizado', CANCELADO: 'Cancelado'
};

export const TEXTO_TIPO: Record<TipoSegmento, string> = {
  TRANSFER_IN: 'Transfer In', TOUR_DIARIO: 'Tour', TRANSFER_OUT: 'Transfer Out', ALOJAMIENTO: 'Alojamiento'
};
