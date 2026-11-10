import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal, WritableSignal } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Cliente, ClienteForm, Conductor, ConductorForm, ContactoReserva, EmpresaForm, EmpresaTransporte, EstadoOperacion,
  EstadoOperacionInfo, EstadoTransaccion, EstimacionDuracion, Guia, GuiaForm, HistorialOperacion, Lugar, LugarForm, Moneda, Operacion,
  OperacionForm, PaqueteForm, PaqueteTour, Pago, PasajeroDraft, PasajeroReserva, RecursoCandidato, Reserva,
  RutaForm, RutaProveedor, SalidaForm, SalidaProgramada, Sesion, Vehiculo, VehiculoForm
} from '../models';
import { redondear } from '../utils/moneda';
import { API, mensajeError } from '../utils/api';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class TourStore {
  private http = inject(HttpClient);
  private auth = inject(AuthService);

  usuarios = signal<Sesion[]>([]);
  paquetes = signal<PaqueteTour[]>([]);
  guias = signal<Guia[]>([]);
  // Proveedores de transporte (v3)
  empresas = signal<EmpresaTransporte[]>([]);
  lugares = signal<Lugar[]>([]);
  rutas = signal<RutaProveedor[]>([]);
  vehiculos = signal<Vehiculo[]>([]);
  conductores = signal<Conductor[]>([]);
  // Tráfico: operaciones (tablero) y catálogo de estados con sus transiciones
  operaciones = signal<Operacion[]>([]);
  estadosOperacion = signal<EstadoOperacionInfo[]>([]);
  clientes = signal<Cliente[]>([]);
  salidas = signal<SalidaProgramada[]>([]);
  reservas = signal<Reserva[]>([]);
  pasajeros = signal<PasajeroReserva[]>([]);
  pagos = signal<Pago[]>([]);

  ocupado = signal(false);
  cargado = signal(false);

  private cargando = false;
  private pendientes = 0;
  private errorCarga = '';
  private avisos: ((error: string) => void)[] = [];

  error(e: unknown): string {
    return mensajeError(e);
  }

  cargar(forzar = false, alTerminar?: (error: string) => void) {
    if (alTerminar) this.avisos.push(alTerminar);
    if (this.cargado() && !forzar) {
      this.avisar();
      return;
    }
    if (this.cargando) return;
    this.cargando = true;
    this.errorCarga = '';
    this.pendientes = 15;
    this.cargarUsuarios(true);
    this.cargarPaquetes(true);
    this.cargarGuias(true);
    this.cargarEmpresas(true);
    this.cargarLugares(true);
    this.cargarRutas(true);
    this.cargarVehiculos(true);
    this.cargarConductores(true);
    this.cargarOperaciones(true);
    this.cargarEstadosOperacion(true);
    this.cargarClientes(true);
    this.cargarSalidas(true);
    this.cargarReservas(true);
    this.cargarPasajeros(true);
    this.cargarPagos(true);
  }

  private terminoTabla(e?: unknown) {
    if (e !== undefined && !this.errorCarga) this.errorCarga = this.error(e);
    this.pendientes--;
    if (this.pendientes > 0) return;
    this.cargando = false;
    this.cargado.set(this.errorCarga === '');
    this.avisar();
  }

  private avisar() {
    const lista = this.avisos;
    this.avisos = [];
    for (const aviso of lista) aviso(this.errorCarga);
  }

  limpiar() {
    this.cargado.set(false);
    this.cargando = false;
    this.usuarios.set([]);
    this.paquetes.set([]);
    this.guias.set([]);
    this.empresas.set([]);
    this.lugares.set([]);
    this.rutas.set([]);
    this.vehiculos.set([]);
    this.conductores.set([]);
    this.operaciones.set([]);
    this.estadosOperacion.set([]);
    this.clientes.set([]);
    this.salidas.set([]);
    this.reservas.set([]);
    this.pasajeros.set([]);
    this.pagos.set([]);
  }

  cargarUsuarios(contar = false) {
    this.http.get<Sesion[]>(`${API}/usuarios`).subscribe({
      next: data => { this.usuarios.set(data); if (contar) this.terminoTabla(); },
      error: e => { if (contar) this.terminoTabla(e); else this.error(e); }
    });
  }
  cargarPaquetes(contar = false) {
    this.http.get<PaqueteTour[]>(`${API}/paquetes`).subscribe({
      next: data => { this.paquetes.set(data); if (contar) this.terminoTabla(); },
      error: e => { if (contar) this.terminoTabla(e); else this.error(e); }
    });
  }
  cargarGuias(contar = false) {
    this.http.get<Guia[]>(`${API}/guias`).subscribe({
      next: data => { this.guias.set(data); if (contar) this.terminoTabla(); },
      error: e => { if (contar) this.terminoTabla(e); else this.error(e); }
    });
  }
  private cargarLista<T>(url: string, destino: WritableSignal<T[]>, contar: boolean) {
    this.http.get<T[]>(`${API}/${url}`).subscribe({
      next: data => { destino.set(data); if (contar) this.terminoTabla(); },
      error: e => { if (contar) this.terminoTabla(e); else this.error(e); }
    });
  }
  cargarEmpresas(contar = false) { this.cargarLista('empresas', this.empresas, contar); }
  cargarLugares(contar = false) { this.cargarLista('lugares', this.lugares, contar); }
  cargarRutas(contar = false) { this.cargarLista('rutas', this.rutas, contar); }
  cargarVehiculos(contar = false) { this.cargarLista('vehiculos', this.vehiculos, contar); }
  cargarConductores(contar = false) { this.cargarLista('conductores', this.conductores, contar); }
  cargarOperaciones(contar = false) { this.cargarLista('operaciones', this.operaciones, contar); }
  cargarEstadosOperacion(contar = false) { this.cargarLista('operaciones/estados', this.estadosOperacion, contar); }
  cargarClientes(contar = false) {
    this.http.get<Cliente[]>(`${API}/clientes`).subscribe({
      next: data => { this.clientes.set(data); if (contar) this.terminoTabla(); },
      error: e => { if (contar) this.terminoTabla(e); else this.error(e); }
    });
  }
  cargarSalidas(contar = false) {
    this.http.get<SalidaProgramada[]>(`${API}/salidas`).subscribe({
      next: data => { this.salidas.set(data); if (contar) this.terminoTabla(); },
      error: e => { if (contar) this.terminoTabla(e); else this.error(e); }
    });
  }
  cargarReservas(contar = false) {
    this.http.get<Reserva[]>(`${API}/reservas`).subscribe({
      next: data => { this.reservas.set(data); if (contar) this.terminoTabla(); },
      error: e => { if (contar) this.terminoTabla(e); else this.error(e); }
    });
  }
  cargarPasajeros(contar = false) {
    this.http.get<PasajeroReserva[]>(`${API}/pasajeros`).subscribe({
      next: data => { this.pasajeros.set(data); if (contar) this.terminoTabla(); },
      error: e => { if (contar) this.terminoTabla(e); else this.error(e); }
    });
  }
  cargarPagos(contar = false) {
    this.http.get<Pago[]>(`${API}/pagos`).subscribe({
      next: data => { this.pagos.set(data); if (contar) this.terminoTabla(); },
      error: e => { if (contar) this.terminoTabla(e); else this.error(e); }
    });
  }

  getPaquete(id?: number | null) { return this.paquetes().find(p => p.paqueteId === id); }
  getGuia(id?: number | null) { return this.guias().find(g => g.guiaId === id); }
  getEmpresa(id?: number | null) { return this.empresas().find(e => e.empresaId === id); }
  getLugar(id?: number | null) { return this.lugares().find(l => l.lugarId === id); }
  getVehiculo(id?: number | null) { return this.vehiculos().find(v => v.vehiculoId === id); }
  getConductor(id?: number | null) { return this.conductores().find(c => c.conductorId === id); }
  getOperacion(id?: number | null) { return this.operaciones().find(o => o.operacionId === id); }
  getEstadoOperacion(codigo?: string | null) { return this.estadosOperacion().find(e => e.codigo === codigo); }
  operacionesDe(salidaId: number) { return this.operaciones().filter(o => o.salidaId === salidaId); }
  nombreLugar(id?: number | null): string {
    const l = this.getLugar(id);
    return l ? `${l.nombre} (${l.ciudad})` : '';
  }
  getCliente(id?: number | null) { return this.clientes().find(c => c.clienteId === id); }

  // Nombre a mostrar: el cliente registrado o el contacto del invitado
  nombreCliente(r?: Reserva | null): string {
    if (!r) return '';
    if (r.clienteId) return this.getCliente(r.clienteId)?.nombreCompleto ?? '';
    return r.contactoNombre ?? '';
  }
  esInvitado(r?: Reserva | null): boolean { return !!r && !r.clienteId; }
  getSalida(id?: number | null) { return this.salidas().find(s => s.salidaId === id); }
  getReserva(id?: number | null) { return this.reservas().find(r => r.reservaId === id); }

  pasajerosDe(reservaId: number) { return this.pasajeros().filter(p => p.reservaId === reservaId); }
  pagosDe(reservaId: number) { return this.pagos().filter(p => p.reservaId === reservaId); }
  salidasDe(paqueteId: number) { return this.salidas().filter(s => s.paqueteId === paqueteId); }
  usernameDe(id?: number | null): string {
    const u = this.usuarios().find(x => x.usuarioId === id);
    return u ? u.username : 'usuario #' + id;
  }

  codigoReserva(id: number) { return 'R-' + String(id).padStart(4, '0'); }
  saldo(r: Reserva): number {
    return r.estadoReserva === 'ANULADA' ? 0 : Math.max(redondear(r.montoTotal - r.montoPagado), 0);
  }
  // Los cupos los mantienen los triggers de DETALLE_RESERVA / RESERVAS
  cuposOcupados(salidaId: number): number {
    const s = this.getSalida(salidaId);
    return s ? s.cuposMaximos - s.cuposDisponibles : 0;
  }

  guardarPaquete(d: PaqueteForm, id: number | null): Observable<PaqueteTour> {
    return id
      ? this.http.put<PaqueteTour>(`${API}/paquetes/${id}`, d)
      : this.http.post<PaqueteTour>(`${API}/paquetes`, d);
  }
  eliminarPaquete(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/paquetes/${id}`);
  }

  guardarGuia(d: GuiaForm, id: number | null): Observable<Guia> {
    return id
      ? this.http.put<Guia>(`${API}/guias/${id}`, d)
      : this.http.post<Guia>(`${API}/guias`, d);
  }
  eliminarGuia(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/guias/${id}`);
  }
  // ---------- Proveedores de transporte ----------
  private guardarEn<T>(url: string, d: unknown, id: number | null): Observable<T> {
    return id ? this.http.put<T>(`${API}/${url}/${id}`, d) : this.http.post<T>(`${API}/${url}`, d);
  }
  guardarEmpresa(d: EmpresaForm, id: number | null) { return this.guardarEn<EmpresaTransporte>('empresas', d, id); }
  eliminarEmpresa(id: number) { return this.http.delete<void>(`${API}/empresas/${id}`); }
  guardarLugar(d: LugarForm, id: number | null) { return this.guardarEn<Lugar>('lugares', d, id); }
  eliminarLugar(id: number) { return this.http.delete<void>(`${API}/lugares/${id}`); }
  guardarRuta(d: RutaForm, id: number | null) { return this.guardarEn<RutaProveedor>('rutas', d, id); }
  eliminarRuta(id: number) { return this.http.delete<void>(`${API}/rutas/${id}`); }
  // Duración con datos reales (OSRM / PKG_RUTAS)
  estimarDuracion(origenId: number, destinoId: number, tipoServicio: string): Observable<EstimacionDuracion> {
    return this.http.get<EstimacionDuracion>(`${API}/rutas/estimar-duracion`,
      { params: { origenId: origenId, destinoId: destinoId, tipoServicio: tipoServicio } });
  }
  guardarVehiculo(d: VehiculoForm, id: number | null) { return this.guardarEn<Vehiculo>('vehiculos', d, id); }
  eliminarVehiculo(id: number) { return this.http.delete<void>(`${API}/vehiculos/${id}`); }
  guardarConductor(d: ConductorForm, id: number | null) { return this.guardarEn<Conductor>('conductores', d, id); }
  eliminarConductor(id: number) { return this.http.delete<void>(`${API}/conductores/${id}`); }

  // ---------- Tráfico (las reglas las aplican trg_operacion_transporte y PKG_ASIGNACION) ----------
  guardarOperacion(d: OperacionForm, id: number | null) { return this.guardarEn<Operacion>('operaciones', d, id); }
  cambiarEstadoOperacion(id: number, estado: EstadoOperacion, observacion: string): Observable<Operacion> {
    return this.http.patch<Operacion>(`${API}/operaciones/${id}/estado`, { estado: estado, observacion: observacion });
  }
  asignarAutomatico(id: number): Observable<Operacion> {
    return this.http.post<Operacion>(`${API}/operaciones/${id}/asignar-automatico`, {});
  }
  asignarRecursos(id: number, vehiculoId: number, conductorId: number): Observable<Operacion> {
    return this.http.patch<Operacion>(`${API}/operaciones/${id}/asignar`, { vehiculoId: vehiculoId, conductorId: conductorId });
  }
  candidatos(id: number): Observable<RecursoCandidato[]> {
    return this.http.get<RecursoCandidato[]>(`${API}/operaciones/${id}/candidatos`);
  }
  historialOperacion(id: number): Observable<HistorialOperacion[]> {
    return this.http.get<HistorialOperacion[]>(`${API}/operaciones/${id}/historial`);
  }

  // Autocompletado: busca por nombre, DNI/pasaporte o correo (mínimo 2 caracteres)
  buscarClientes(texto: string): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(`${API}/clientes/buscar`, { params: { q: texto } });
  }

  guardarCliente(d: ClienteForm, id: number | null): Observable<Cliente> {
    return id
      ? this.http.put<Cliente>(`${API}/clientes/${id}`, d)
      : this.http.post<Cliente>(`${API}/clientes`, d);
  }
  eliminarCliente(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/clientes/${id}`);
  }

  guardarSalida(d: SalidaForm, id: number | null): Observable<SalidaProgramada> {
    return id
      ? this.http.put<SalidaProgramada>(`${API}/salidas/${id}`, d)
      : this.http.post<SalidaProgramada>(`${API}/salidas`, d);
  }
  eliminarSalida(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/salidas/${id}`);
  }

  // Se envía clienteId (cliente registrado) o contacto (invitado), nunca ambos
  crearReserva(clienteId: number | null, contacto: ContactoReserva | null, salidaId: number, observaciones: string,
               pasajeros: PasajeroDraft[]): Observable<Reserva> {
    const lista = pasajeros.map(p => ({
      nombreCompleto: p.nombreCompleto, tipoDocumento: p.tipoDocumento, numeroDocumento: p.numeroDocumento,
      nacionalidad: p.nacionalidad, fechaNacimiento: p.fechaNacimiento ? p.fechaNacimiento : null
    }));
    const cuerpo = { clienteId: clienteId, contacto: contacto, salidaId: salidaId, observaciones: observaciones, pasajeros: lista };
    return this.http.post<Reserva>(`${API}/reservas`, cuerpo);
  }

  urlBoleta(id: number): string { return `${API}/reservas/${id}/boleta`; }
  enviarBoleta(id: number): Observable<{ destinatario: string; enviadaEn: string }> {
    return this.http.post<{ destinatario: string; enviadaEn: string }>(`${API}/reservas/${id}/boleta`, {});
  }

  confirmarReserva(id: number): Observable<Reserva> {
    return this.http.patch<Reserva>(`${API}/reservas/${id}/confirmar`, {});
  }

  completarReserva(id: number): Observable<Reserva> {
    return this.http.patch<Reserva>(`${API}/reservas/${id}/completar`, {});
  }

  anularReserva(id: number, formaReembolso: string | null): Observable<Reserva> {
    return this.http.patch<Reserva>(`${API}/reservas/${id}/anular`, { formaReembolso: formaReembolso, usuarioId: this.auth.usuario()?.usuarioId ?? null });
  }

  registrarPago(reservaId: number, monto: number, moneda: Moneda, metodoPago: string,
                codigoTransaccion: string, estadoTransaccion: EstadoTransaccion): Observable<Pago> {
    const cuerpo = {
      reservaId: reservaId, monto: monto, moneda: moneda, metodoPago: metodoPago,
      codigoTransaccion: codigoTransaccion, estadoTransaccion: estadoTransaccion
    };
    return this.http.post<Pago>(`${API}/pagos`, cuerpo);
  }

  resolverPago(pagoId: number, estado: 'APROBADO' | 'RECHAZADO'): Observable<Pago> {
    return this.http.patch<Pago>(`${API}/pagos/${pagoId}/resolver`, { estado: estado });
  }
}
