export type EstadoSalida = 'PROGRAMADA' | 'CONFIRMADA' | 'COMPLETADA' | 'CANCELADA';
export type EstadoReserva = 'PENDIENTE' | 'CONFIRMADA' | 'ANULADA' | 'COMPLETADA';
export type EstadoPagoReserva = 'PENDIENTE' | 'PARCIAL' | 'PAGADO' | 'REEMBOLSADO';
export type EstadoTransaccion = 'PENDIENTE' | 'APROBADO' | 'RECHAZADO' | 'REEMBOLSADO';
export type TipoDocumento = 'DNI' | 'CE' | 'PASAPORTE';
// Los clientes también pueden ser empresas (RUC); los pasajeros no
export type TipoDocumentoCliente = TipoDocumento | 'RUC';
export type Moneda = 'PEN' | 'USD' | 'EUR';
// chk_usu_rol de la v3
export type Rol = 'ADMIN' | 'VENDEDOR' | 'OPERADOR';
export type EstadoRegistro = 'ACTIVO' | 'INACTIVO';
export type CategoriaLicencia = 'A-IIa' | 'A-IIb' | 'A-IIIa' | 'A-IIIb' | 'A-IIIc';
export type EstadoAfiliacion = 'PENDIENTE' | 'ACTIVA' | 'SUSPENDIDA' | 'RETIRADA';
export type DisponibilidadOperativa = 'DISPONIBLE' | 'LIMITADA' | 'NO_DISPONIBLE';
export type TipoLugar = 'AEROPUERTO' | 'TERMINAL' | 'HOTEL' | 'ATRACTIVO' | 'CIUDAD' | 'OTRO';
export type TipoServicio = 'TRASLADO' | 'TOUR' | 'INTERPROVINCIAL';
export type EstadoRuta = 'ACTIVA' | 'INACTIVA';
export type TipoVehiculo = 'AUTO' | 'CAMIONETA_4X4' | 'VAN' | 'MINIBUS' | 'BUS';
export type EstadoOperativo = 'OPERATIVO' | 'EN_MANTENIMIENTO' | 'FUERA_DE_SERVICIO';
export type EstadoConductor = 'DISPONIBLE' | 'DESCANSO' | 'VACACIONES' | 'INACTIVO';
// DOMINIO_ESTADOS de la v3
export type EstadoOperacion = 'PROGRAMADO' | 'ASIGNADO' | 'EMBARQUE' | 'EN_ESPERA_SALIDA' | 'EN_RUTA'
  | 'LLEGADA_PROXIMA' | 'FINALIZADO' | 'RETRASADO' | 'CANCELADO';

export interface UsuarioSistema {
  usuarioId: number;
  username: string;
  passwordHash: string;
  rol: Rol;
  estado: EstadoRegistro;
}

export interface PaqueteTour {
  paqueteId: number;
  nombre: string;
  descripcion: string;
  precioBase: number;
  dias: number;
  noches: number;
  imagen: string;
  estado: EstadoRegistro;
}

export interface Guia {
  guiaId: number;
  nombreCompleto: string;
  especialidad: string;
  telefono: string;
}

// ---------- Proveedores de transporte (v3) ----------
export interface EmpresaTransporte {
  empresaId: number;
  nombreComercial: string;
  ruc: string;
  contactoNombre: string;
  contactoTelefono: string;
  contactoEmail: string;
  direccion: string;
  estadoAfiliacion: EstadoAfiliacion;
  disponibilidadOperativa: DisponibilidadOperativa;
  fechaAfiliacion: string | null;
  // Vista VW_EMPRESA_CAPACIDAD
  totalVehiculos: number;
  capacidadTotalTransporte: number;
  conductoresDisponibles: number;
}

export interface Lugar {
  lugarId: number;
  nombre: string;
  tipoLugar: TipoLugar;
  ciudad: string;
}

export interface RutaProveedor {
  rutaId: number;
  empresaId: number;
  origenId: number;
  destinoId: number;
  tipoServicio: TipoServicio;
  duracionEstimadaMin: number;
  tarifaReferencial: number | null;
  estado: EstadoRuta;
}

export interface Vehiculo {
  vehiculoId: number;
  empresaId: number;
  placa: string;
  tipoVehiculo: TipoVehiculo;
  marcaModelo: string;
  capacidadPax: number;
  fechaProxMantenimiento: string;
  estadoOperativo: EstadoOperativo;
}

export interface Conductor {
  conductorId: number;
  empresaId: number;
  nombres: string;
  numeroLicencia: string;
  categoriaLicencia: CategoriaLicencia;
  fechaVencLicencia: string;
  telefono: string;
  estadoDisponibilidad: EstadoConductor;
}

// ---------- Tráfico (OPERACION_TRANSPORTE + VW_TABLERO_OPERACIONES) ----------
export interface Operacion {
  operacionId: number;
  codigoOperacion: string;
  salidaId: number | null;
  origenId: number;
  destinoId: number;
  empresaId: number | null;
  vehiculoId: number | null;
  conductorId: number | null;
  paxProgramados: number;
  horaProgramadaSalida: string;
  horaEstimadaLlegada: string;
  estadoActual: EstadoOperacion;
  observaciones: string;
  // Desde la vista VW_TABLERO_OPERACIONES
  origen: string;
  destino: string;
  empresa: string | null;
  placa: string | null;
  conductor: string | null;
  estadoNombre: string;
  ordenEstado: number;
  horaRealSalida: string | null;
  horaRealLlegada: string | null;
  minRetrasoSalida: number | null;
  minRetrasoLlegada: number | null;
}

export interface EstadoOperacionInfo {
  codigo: EstadoOperacion;
  nombre: string;
  orden: number;
  requiereRecursos: boolean;
  ocupaRecursos: boolean;
  esFinal: boolean;
  siguientes: EstadoOperacion[];
}

export interface HistorialOperacion {
  historialId: number;
  operacionId: number;
  estadoAnterior: EstadoOperacion | null;
  estadoNuevo: EstadoOperacion;
  fechaCambio: string;
  usuarioRegistro: string;
  observacion: string;
}

// Vehículo o conductor posible para una operación (motivo = null: elegible)
export interface RecursoCandidato {
  tipoRecurso: 'VEHICULO' | 'CONDUCTOR';
  recursoId: number;
  empresaId: number;
  empresa: string;
  descripcion: string;
  capacidadPax: number | null;
  horasDia: number | null;
  motivo: string | null;
}

export interface Cliente {
  clienteId: number;
  nombreCompleto: string;
  tipoDocumento: TipoDocumentoCliente;
  numeroDocumento: string;
  email: string;
  telefono: string;
}

export interface SalidaProgramada {
  salidaId: number;
  paqueteId: number;
  fechaInicio: string;
  fechaFin: string;
  cuposMaximos: number;
  cuposDisponibles: number;
  guiaId?: number | null;
  estado: EstadoSalida;
  // Operaciones de transporte vigentes y PKG_ASIGNACION.faltante_transporte (null = alcanza)
  operaciones: number;
  faltanteTransporte: string | null;
}

// Cabecera + detalle de la salida reservada (DETALLE_RESERVA)
export interface Reserva {
  reservaId: number;
  clienteId: number | null;
  detalleId: number;
  salidaId: number;
  fechaReserva: string;
  numeroViajeros: number;
  precioUnitario: number;
  descuento: number;
  montoTotal: number;
  montoPagado: number;
  saldo: number;
  estadoReserva: EstadoReserva;
  estadoPago: EstadoPagoReserva;
  observaciones: string;
  formaReembolso?: string;
  usuarioAnulaId?: number;
  contactoNombre?: string | null;
  contactoEmail?: string | null;
  contactoTelefono?: string | null;
  boletaEnviadaEn?: string | null;
}

// Datos mínimos de un cliente no registrado (invitado)
export interface ContactoReserva {
  nombre: string;
  email: string;
  telefono: string;
}

export interface PasajeroReserva {
  pasajeroId: number;
  detalleId: number;
  reservaId: number;
  nombreCompleto: string;
  tipoDocumento: TipoDocumento;
  numeroDocumento: string;
  nacionalidad: string;
  fechaNacimiento: string;
}

export interface Pago {
  pagoId: number;
  reservaId: number;
  monto: number;
  moneda: Moneda;
  tipoCambio: number;
  montoPen: number;
  metodoPago: string;
  codigoTransaccion: string;
  fechaPago: string;
  estadoTransaccion: EstadoTransaccion;
}

export interface PaqueteForm {
  nombre: string;
  descripcion: string;
  precioBase: number;
  dias: number;
  noches: number;
  imagen: string;
  estado: EstadoRegistro;
}

export interface GuiaForm {
  nombreCompleto: string;
  especialidad: string;
  telefono: string;
}

export interface EmpresaForm {
  nombreComercial: string;
  ruc: string;
  contactoNombre: string;
  contactoTelefono: string;
  contactoEmail: string;
  direccion: string;
  estadoAfiliacion: EstadoAfiliacion;
  disponibilidadOperativa: DisponibilidadOperativa;
  fechaAfiliacion: string | null;
}

export interface LugarForm {
  nombre: string;
  tipoLugar: TipoLugar;
  ciudad: string;
}

export interface RutaForm {
  empresaId: number | null;
  origenId: number | null;
  destinoId: number | null;
  tipoServicio: TipoServicio;
  duracionEstimadaMin: number | null;
  tarifaReferencial: number | null;
  estado: EstadoRuta;
}

export interface VehiculoForm {
  empresaId: number | null;
  placa: string;
  tipoVehiculo: TipoVehiculo;
  marcaModelo: string;
  capacidadPax: number | null;
  fechaProxMantenimiento: string;
  estadoOperativo: EstadoOperativo;
}

export interface ConductorForm {
  empresaId: number | null;
  nombres: string;
  numeroLicencia: string;
  categoriaLicencia: CategoriaLicencia;
  fechaVencLicencia: string;
  telefono: string;
  estadoDisponibilidad: EstadoConductor;
}

export interface OperacionForm {
  salidaId: number | null;
  origenId: number | null;
  destinoId: number | null;
  empresaId: number | null;
  vehiculoId: number | null;
  conductorId: number | null;
  paxProgramados: number | null;
  horaProgramadaSalida: string;      // 'YYYY-MM-DDTHH:mm'
  horaEstimadaLlegada: string | null;
  observaciones: string;
}

export interface ClienteForm {
  nombreCompleto: string;
  tipoDocumento: TipoDocumentoCliente;
  numeroDocumento: string;
  email: string;
  telefono: string;
}

// La fecha fin la calcula la base de datos (trg_salida_biu) con los días del paquete
export interface SalidaForm {
  paqueteId: number;
  fechaInicio: string;
  cuposMaximos: number;
  guiaId?: number | null;
  estado: EstadoSalida;
}

export interface PasajeroDraft {
  nombreCompleto: string;
  tipoDocumento: TipoDocumento;
  numeroDocumento: string;
  nacionalidad: string;
  fechaNacimiento: string;
}

export interface Sesion {
  usuarioId: number;
  username: string;
  rol: Rol;
  estado: EstadoRegistro;
}
