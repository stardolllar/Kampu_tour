import { DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, EMPTY, filter, switchMap, timer } from 'rxjs';
import { SeguimientoCliente, TEXTO_ESTADO, TEXTO_TIPO } from '../../core/itinerario.models';
import { abrirPdf } from '../../core/services/itinerario.service';
import { SeguimientoService } from '../../core/services/seguimiento.service';
import { mensajeError } from '../../core/utils/api';
import { IconComponent } from '../../shared/icon.component';
import { PortalHeaderComponent } from './portal-header.component';

/**
 * Itinerario del pasajero en vivo: ¿el chofer ya viene?, segmentos con su estado, PDF de sus
 * boletos aéreos y comprobante. Solo lectura; se actualiza cada 20 s.
 */
@Component({
  selector: 'app-itinerario-cliente',
  standalone: true,
  imports: [DatePipe, RouterLink, IconComponent, PortalHeaderComponent],
  templateUrl: './itinerario-cliente.component.html'
})
export class ItinerarioClienteComponent {
  private api = inject(SeguimientoService);
  id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));
  texto = TEXTO_ESTADO;
  tipoTexto = TEXTO_TIPO;

  datos = signal<SeguimientoCliente | null>(null);
  error = signal('');

  constructor() {
    timer(0, 20_000).pipe(filter(() => !document.hidden),
      switchMap(() => this.api.itinerario(this.id).pipe(catchError(e => { this.error.set(mensajeError(e)); return EMPTY; }))),
      takeUntilDestroyed(inject(DestroyRef)))
      .subscribe(d => { this.datos.set(d); this.error.set(''); });
  }

  verPdf(boletoId: number) {
    this.api.pdf(boletoId).subscribe({ next: abrirPdf, error: e => this.error.set(mensajeError(e)) });
  }

  comprobante() {
    const ventana = window.open('', '_blank');
    this.api.comprobante(this.id).subscribe({
      next: html => { ventana?.document.open(); ventana?.document.write(html); ventana?.document.close(); },
      error: e => { ventana?.close(); this.error.set(mensajeError(e)); }
    });
  }
}
