import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Itinerario, ItinerarioForm, TEXTO_ESTADO, TEXTO_TIPO } from '../../core/itinerario.models';
import { AlertService } from '../../core/services/alert.service';
import { ItinerarioService } from '../../core/services/itinerario.service';
import { TourStore } from '../../core/services/tour-store.service';
import { mensajeError } from '../../core/utils/api';
import { IconComponent } from '../../shared/icon.component';
import { BoletoAereoFormComponent } from './boleto-aereo-form.component';

type Filtro = 'PENDIENTES' | 'CONFIRMADAS' | 'TODAS';

/**
 * Booking y vuelos (Ventas / Admin): por reserva se abre el itinerario (hotel, aeropuerto,
 * tours) y se cargan los vuelos comprados fuera, con su PDF. El backend confirma la reserva
 * y genera los transfers automáticamente.
 */
@Component({
  selector: 'app-vuelos',
  standalone: true,
  imports: [FormsModule, DatePipe, IconComponent, BoletoAereoFormComponent],
  templateUrl: './vuelos.component.html'
})
export class VuelosComponent {
  private api = inject(ItinerarioService);
  private alerts = inject(AlertService);
  store = inject(TourStore);
  texto = TEXTO_ESTADO;
  tipoTexto = TEXTO_TIPO;

  lista = signal<Itinerario[]>([]);
  cargando = signal(true);
  filtro = signal<Filtro>('PENDIENTES');
  q = signal('');
  sel = signal<Itinerario | null>(null);
  form: ItinerarioForm = { hotelId: null, aeropuertoId: null, tours: [] };
  guardando = signal(false);

  hoteles = computed(() => this.store.lugares().filter(l => l.tipoLugar === 'HOTEL'));
  aeropuertos = computed(() => this.store.lugares().filter(l => l.tipoLugar === 'AEROPUERTO'));
  atractivos = computed(() => this.store.lugares().filter(l => l.tipoLugar === 'ATRACTIVO'));

  visibles = computed(() => {
    const t = this.q().trim().toLowerCase();
    return this.lista().filter(i => {
      const pasa = this.filtro() === 'TODAS' ? true
        : this.filtro() === 'PENDIENTES' ? (i.estadoLogistico === null || i.estadoLogistico === 'VUELO_PENDIENTE')
          : i.estadoLogistico !== null && i.estadoLogistico !== 'VUELO_PENDIENTE';
      return pasa && (!t || `${i.reservaId} ${i.cliente} ${i.paquete}`.toLowerCase().includes(t));
    });
  });
  contar = (f: Filtro) => this.lista().filter(i => f === 'TODAS' || (f === 'PENDIENTES'
    ? (i.estadoLogistico === null || i.estadoLogistico === 'VUELO_PENDIENTE')
    : i.estadoLogistico !== null && i.estadoLogistico !== 'VUELO_PENDIENTE')).length;

  llegada = computed(() => this.sel()?.boletos.find(b => b.tipoVuelo === 'LLEGADA') ?? null);
  salida = computed(() => this.sel()?.boletos.find(b => b.tipoVuelo === 'SALIDA') ?? null);
  // Código IATA del aeropuerto local, tomado del nombre "(TPP)" si lo tiene
  iataLocal = computed(() => /\(([A-Z]{3})\)/.exec(this.sel()?.aeropuerto ?? '')?.[1] ?? 'TPP');

  constructor() {
    this.cargar();
  }

  cargar() {
    this.cargando.set(true);
    this.api.listar().subscribe({
      next: l => { this.lista.set(l); this.cargando.set(false); },
      error: e => { this.cargando.set(false); this.alerts.error(mensajeError(e)); }
    });
  }

  abrir(i: Itinerario) {
    this.api.detalle(i.reservaId).subscribe({
      next: d => this.mostrar(d),
      error: e => this.alerts.error(mensajeError(e))
    });
  }

  private mostrar(d: Itinerario) {
    this.sel.set(d);
    this.form = {
      hotelId: d.hotelId, aeropuertoId: d.aeropuertoId ?? this.aeropuertos().find(a => /TPP/.test(a.nombre))?.lugarId ?? null,
      tours: d.segmentos.filter(s => s.tipo === 'TOUR_DIARIO').map(s => ({ destinoId: s.destinoId, horaRecojo: s.horaInicio.slice(0, 16) }))
    };
    this.lista.update(l => l.map(x => x.reservaId === d.reservaId ? { ...d, segmentos: [] } : x));
  }

  agregarTour() {
    const d = this.sel();
    const dia = d?.fechaInicio ? d.fechaInicio : new Date().toISOString().slice(0, 10);
    this.form.tours = [...this.form.tours, { destinoId: null, horaRecojo: `${dia}T08:00` }];
  }

  quitarTour(i: number) {
    this.form.tours = this.form.tours.filter((_, j) => j !== i);
  }

  guardarItinerario() {
    const d = this.sel();
    if (!d) return;
    if (!this.form.hotelId || !this.form.aeropuertoId) { this.alerts.error('Elige el hotel y el aeropuerto.'); return; }
    if (this.form.tours.some(t => !t.destinoId || !t.horaRecojo)) { this.alerts.error('Completa destino y hora de cada tour.'); return; }
    this.guardando.set(true);
    this.api.guardar(d.reservaId, this.form).subscribe({
      next: it => { this.guardando.set(false); this.mostrar(it); this.alerts.success('Itinerario guardado.'); },
      error: e => { this.guardando.set(false); this.alerts.error(mensajeError(e)); }
    });
  }

  vueloGuardado(it: Itinerario) {
    this.mostrar(it);
    this.alerts.success(it.estadoLogistico === 'CONFIRMADO_CON_VUELO'
      ? 'Vuelo guardado: la reserva quedó CONFIRMADA CON VUELO y el transfer se actualizó.' : 'Vuelo guardado.');
  }

  claseEstado(e: string | null) {
    switch (e) {
      case null: return 'bg-zinc-100 text-zinc-600';
      case 'VUELO_PENDIENTE': return 'bg-amber-50 text-amber-700';
      case 'CONFIRMADO_CON_VUELO': return 'bg-emerald-50 text-emerald-700';
      case 'EN_CURSO': return 'bg-sky-50 text-sky-700';
      default: return 'bg-zinc-100 text-zinc-700';
    }
  }
}
