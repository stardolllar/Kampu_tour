import { DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, EMPTY, filter, merge, Subject, switchMap, timer } from 'rxjs';
import { EstadoSegmento, TEXTO_ESTADO, TEXTO_TIPO, ViajeOperador } from '../../core/itinerario.models';
import { AlertService } from '../../core/services/alert.service';
import { OperadorCampoService } from '../../core/services/operador-campo.service';
import { mensajeError } from '../../core/utils/api';
import { IconComponent } from '../../shared/icon.component';

/**
 * "Mi jornada" del operador (chofer / guía), pensada para el celular:
 * solo sus viajes de HOY, ruta optimizada de hoteles, manifiesto de pasajeros y un botón
 * grande para avanzar al siguiente estado de la secuencia.
 */
@Component({
  selector: 'app-jornada',
  standalone: true,
  imports: [DatePipe, IconComponent],
  templateUrl: './jornada.component.html'
})
export class JornadaComponent {
  private api = inject(OperadorCampoService);
  private alerts = inject(AlertService);
  texto = TEXTO_ESTADO;
  tipoTexto = TEXTO_TIPO;
  hoy = new Date();

  viajes = signal<ViajeOperador[]>([]);
  cargado = signal(false);
  error = signal('');
  enviando = signal<string | null>(null);
  private refrescar$ = new Subject<void>();

  constructor() {
    merge(timer(0, 30_000).pipe(filter(() => !document.hidden)), this.refrescar$)
      .pipe(switchMap(() => this.api.jornada().pipe(catchError(e => { this.error.set(mensajeError(e)); return EMPTY; }))),
        takeUntilDestroyed(inject(DestroyRef)))
      .subscribe(v => { this.viajes.set(v); this.cargado.set(true); this.error.set(''); });
  }

  async avanzar(v: ViajeOperador) {
    if (!v.siguienteEstado) return;
    if (!(await this.alerts.confirm(`${v.pax} pasajeros pasarán a "${this.texto[v.siguienteEstado]}".`, this.texto[v.siguienteEstado], 'Confirmar'))) return;
    this.enviando.set(v.grupoCodigo);
    this.api.avanzarViaje(v.grupoCodigo, v.siguienteEstado).subscribe({
      next: nuevo => { this.enviando.set(null); this.reemplazar(nuevo); },
      error: e => { this.enviando.set(null); this.alerts.error(mensajeError(e)); this.refrescar$.next(); }
    });
  }

  // Una sola reserva del viaje (p. ej. esa familia ya abordó)
  avanzarUno(v: ViajeOperador, segmentoId: number, estado: EstadoSegmento | null) {
    if (!estado) return;
    this.enviando.set(v.grupoCodigo);
    this.api.avanzarSegmento(segmentoId, estado).subscribe({
      next: nuevo => { this.enviando.set(null); this.reemplazar(nuevo); },
      error: e => { this.enviando.set(null); this.alerts.error(mensajeError(e)); this.refrescar$.next(); }
    });
  }

  private reemplazar(nuevo: ViajeOperador) {
    this.viajes.update(l => l.map(x => x.grupoCodigo === nuevo.grupoCodigo ? nuevo : x));
  }

  pasos(v: ViajeOperador): number[] {
    return Array.from({ length: v.pasos }, (_, i) => i + 1);
  }

  mapa(v: ViajeOperador): string | null {
    const p = v.ruta.filter(r => r.latitud !== null && r.longitud !== null);
    if (p.length < 2) return null;
    const ruta = p.map(r => `${r.latitud},${r.longitud}`);
    return `https://www.google.com/maps/dir/${ruta.join('/')}`;
  }
}
