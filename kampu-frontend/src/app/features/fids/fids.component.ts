import { DatePipe } from '@angular/common';
import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { catchError, EMPTY, filter, merge, Subject, switchMap, timer } from 'rxjs';
import { GrupoFids, RecursosFids, TableroFids, TEXTO_ESTADO, TEXTO_TIPO, TipoSegmento } from '../../core/itinerario.models';
import { AlertService } from '../../core/services/alert.service';
import { FidsService } from '../../core/services/fids.service';
import { mensajeError } from '../../core/utils/api';
import { hoy } from '../../core/utils/date.util';
import { IconComponent } from '../../shared/icon.component';

export const FIDS_REFRESCO_MS = 15_000;

/**
 * Torre de control (Admin): tablero en vivo de transfers y tours consolidados entre reservas.
 * Se actualiza solo cada 15 s (se pausa con la pestaña oculta) y al instante tras asignar.
 */
@Component({
  selector: 'app-fids',
  standalone: true,
  imports: [FormsModule, DatePipe, IconComponent],
  templateUrl: './fids.component.html'
})
export class FidsComponent {
  private api = inject(FidsService);
  private alerts = inject(AlertService);
  texto = TEXTO_ESTADO;
  tipoTexto = TEXTO_TIPO;

  fecha = signal(hoy());
  tipo = signal<TipoSegmento | ''>('');
  tablero = signal<TableroFids | null>(null);
  error = signal('');
  cargando = signal(false);
  abierto = signal<string | null>(null);
  recursos = signal<RecursosFids>({ vehiculos: [], operadores: [] });

  // Asignación
  grupoSel = signal<GrupoFids | null>(null);
  elegidos = signal<Set<number>>(new Set());
  vehiculoId: number | null = null;
  operadorId: number | null = null;
  asignando = signal(false);

  paxElegidos = computed(() => {
    const g = this.grupoSel();
    return g ? g.segmentos.filter(s => this.elegidos().has(s.segmentoId)).reduce((a, s) => a + s.pax, 0) : 0;
  });

  private refrescar$ = new Subject<void>();

  constructor() {
    const destroy = inject(DestroyRef);
    // Polling: cada 15 s (solo con la pestaña visible) o al pedirlo (cambio de filtro / tras asignar)
    merge(timer(0, FIDS_REFRESCO_MS).pipe(filter(() => !document.hidden)), this.refrescar$)
      .pipe(
        switchMap(() => {
          this.cargando.set(true);
          // El error se maneja aquí para que un corte de red no detenga el refresco automático
          return this.api.tablero(this.fecha(), this.tipo()).pipe(catchError(e => {
            this.error.set(mensajeError(e));
            this.cargando.set(false);
            return EMPTY;
          }));
        }),
        takeUntilDestroyed(destroy))
      .subscribe(t => { this.tablero.set(t); this.error.set(''); this.cargando.set(false); });
    this.api.recursos().subscribe({ next: r => this.recursos.set(r), error: () => undefined });
  }

  recargar() {
    this.refrescar$.next();
  }

  cambiarFecha(f: string) {
    this.fecha.set(f || hoy());
    this.recargar();
  }

  cambiarTipo(t: TipoSegmento | '') {
    this.tipo.set(t);
    this.recargar();
  }

  abrirAsignacion(g: GrupoFids) {
    this.grupoSel.set(g);
    this.elegidos.set(new Set(g.segmentos.filter(s => s.estado === 'PROGRAMADO').map(s => s.segmentoId)));
    this.operadorId = g.operadorId;
    // Sugerencia: el vehículo más chico que alcance (o el que ya tiene)
    this.vehiculoId = g.vehiculoId ?? this.recursos().vehiculos.find(v => v.capacidad >= g.pax)?.vehiculoId ?? null;
  }

  alternar(id: number) {
    const s = new Set(this.elegidos());
    s.has(id) ? s.delete(id) : s.add(id);
    this.elegidos.set(s);
  }

  asignar() {
    if (!this.vehiculoId || !this.operadorId || this.elegidos().size === 0) {
      this.alerts.error('Elige segmentos, vehículo y operador.');
      return;
    }
    this.asignando.set(true);
    this.api.asignar([...this.elegidos()], this.vehiculoId, this.operadorId).subscribe({
      next: segs => {
        this.asignando.set(false);
        this.grupoSel.set(null);
        this.alerts.success(`Viaje ${segs[0]?.grupoCodigo}: ${segs.length} reserva(s) en ${segs[0]?.placa} con ${segs[0]?.operador}.`);
        this.recargar();
      },
      error: e => { this.asignando.set(false); this.alerts.error(mensajeError(e)); }
    });
  }

  async liberar(g: GrupoFids) {
    if (!g.grupoCodigo || !(await this.alerts.confirm(`Se quitará el vehículo y el operador del viaje ${g.grupoCodigo}.`, 'Liberar viaje', 'Liberar'))) return;
    this.api.liberar(g.grupoCodigo).subscribe({
      next: () => { this.alerts.success('Viaje liberado.'); this.recargar(); },
      error: e => this.alerts.error(mensajeError(e))
    });
  }

  claseEstado(g: GrupoFids): string {
    if (!g.asignado) return 'text-amber-300';
    if (g.estado === 'PROGRAMADO') return 'text-sky-300';
    return /DEJADO|FINALIZADO/.test(g.estado) ? 'text-zinc-400' : 'text-emerald-300';
  }

  hora(iso: string) {
    return iso.slice(11, 16);
  }
}
