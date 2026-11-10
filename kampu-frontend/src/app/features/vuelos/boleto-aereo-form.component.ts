import { Component, EventEmitter, Input, OnChanges, Output, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Aerolinea, BoletoAereo, BoletoForm, Itinerario, TipoVuelo } from '../../core/itinerario.models';
import { abrirPdf, ItinerarioService } from '../../core/services/itinerario.service';
import { mensajeError } from '../../core/utils/api';
import { IconComponent } from '../../shared/icon.component';

export const PDF_MAX_BYTES = 5 * 1024 * 1024;   // igual que kampu.boletos.max-bytes del backend

/**
 * Carga manual de un vuelo (Ventas): datos del vuelo + PDF del boarding pass / e-ticket.
 * Se envía como multipart/form-data (FormData). Al guardar, el backend confirma la reserva
 * (CONFIRMADO_CON_VUELO) y genera/actualiza el transfer; el componente emite el itinerario nuevo.
 */
@Component({
  selector: 'app-boleto-aereo-form',
  standalone: true,
  imports: [FormsModule, IconComponent],
  templateUrl: './boleto-aereo-form.component.html'
})
export class BoletoAereoFormComponent implements OnChanges {
  private api = inject(ItinerarioService);

  @Input({ required: true }) reservaId!: number;
  @Input({ required: true }) tipo!: TipoVuelo;
  @Input() boleto: BoletoAereo | null = null;
  @Input() aeropuertoLocal = 'TPP';
  @Output() guardado = new EventEmitter<Itinerario>();

  aerolineas: Aerolinea[] = ['LATAM', 'SKY', 'JETSMART', 'OTRA'];
  form: BoletoForm = this.vacio();
  archivo: File | null = null;
  arrastrando = signal(false);
  enviando = signal(false);
  error = signal('');
  errores: Record<string, string> = {};

  ngOnChanges() {
    const b = this.boleto;
    this.form = b ? {
      tipoVuelo: b.tipoVuelo, aerolinea: b.aerolinea, numeroVuelo: b.numeroVuelo, codigoPnr: b.codigoPnr,
      aeropuertoOrigen: b.aeropuertoOrigen.trim(), aeropuertoDestino: b.aeropuertoDestino.trim(),
      fechaHoraSalida: b.fechaHoraSalida.slice(0, 16), fechaHoraLlegada: b.fechaHoraLlegada.slice(0, 16)
    } : this.vacio();
    this.archivo = null;
    this.errores = {};
    this.error.set('');
  }

  private vacio(): BoletoForm {
    const llegada = this.tipo !== 'SALIDA';
    return {
      tipoVuelo: this.tipo ?? 'LLEGADA', aerolinea: 'LATAM', numeroVuelo: '', codigoPnr: '',
      aeropuertoOrigen: llegada ? 'LIM' : this.aeropuertoLocal, aeropuertoDestino: llegada ? this.aeropuertoLocal : 'LIM',
      fechaHoraSalida: '', fechaHoraLlegada: ''
    };
  }

  // <input type="file">
  elegir(ev: Event) {
    const input = ev.target as HTMLInputElement;
    this.tomar(input.files?.[0] ?? null);
    input.value = '';   // permite volver a elegir el mismo archivo
  }

  soltar(ev: DragEvent) {
    ev.preventDefault();
    this.arrastrando.set(false);
    this.tomar(ev.dataTransfer?.files?.[0] ?? null);
  }

  private tomar(f: File | null) {
    delete this.errores['archivo'];
    if (!f) return;
    if (!f.name.toLowerCase().endsWith('.pdf') || (f.type && f.type !== 'application/pdf')) {
      this.errores['archivo'] = 'Solo se aceptan archivos PDF.';
      return;
    }
    if (f.size > PDF_MAX_BYTES) {
      this.errores['archivo'] = `El PDF pesa ${(f.size / 1048576).toFixed(1)} MB; el máximo es 5 MB.`;
      return;
    }
    this.archivo = f;
  }

  mayusculas(campo: 'numeroVuelo' | 'codigoPnr' | 'aeropuertoOrigen' | 'aeropuertoDestino') {
    this.form[campo] = (this.form[campo] ?? '').toUpperCase().replace(campo === 'numeroVuelo' ? /[^A-Z0-9 ]/g : /[^A-Z0-9]/g, '');
  }

  private validar(): boolean {
    const f = this.form, e: Record<string, string> = {};
    if (!/^[A-Z0-9]{2} ?\d{1,4}$/.test(f.numeroVuelo)) e['numeroVuelo'] = 'Código de aerolínea + número (ej. LA2380).';
    if (!/^[A-Z0-9]{6}$/.test(f.codigoPnr)) e['codigoPnr'] = 'El PNR tiene 6 letras o números.';
    if (!/^[A-Z]{3}$/.test(f.aeropuertoOrigen)) e['aeropuertoOrigen'] = 'Código IATA de 3 letras.';
    if (!/^[A-Z]{3}$/.test(f.aeropuertoDestino)) e['aeropuertoDestino'] = 'Código IATA de 3 letras.';
    else if (f.aeropuertoDestino === f.aeropuertoOrigen) e['aeropuertoDestino'] = 'Debe ser distinto al origen.';
    if (!f.fechaHoraSalida) e['fechaHoraSalida'] = 'Indica la salida del vuelo.';
    if (!f.fechaHoraLlegada) e['fechaHoraLlegada'] = 'Indica la llegada del vuelo.';
    else if (f.fechaHoraSalida && f.fechaHoraLlegada <= f.fechaHoraSalida) e['fechaHoraLlegada'] = 'Debe ser posterior a la salida.';
    if (!this.archivo && !this.boleto?.tienePdf) e['archivo'] = 'Adjunta el PDF del boarding pass o e-ticket.';
    if (this.errores['archivo'] && !this.archivo) e['archivo'] = this.errores['archivo'];
    this.errores = e;
    return Object.keys(e).length === 0;
  }

  guardar() {
    this.error.set('');
    if (!this.validar()) return;
    this.enviando.set(true);
    this.api.registrarBoleto(this.reservaId, { ...this.form, tipoVuelo: this.tipo }, this.archivo).subscribe({
      next: it => { this.enviando.set(false); this.archivo = null; this.guardado.emit(it); },
      error: err => { this.enviando.set(false); this.error.set(mensajeError(err)); }
    });
  }

  verPdf() {
    if (!this.boleto) return;
    this.api.pdf(this.boleto.boletoId).subscribe({ next: abrirPdf, error: err => this.error.set(mensajeError(err)) });
  }

  tamanio(bytes: number | null | undefined) {
    return bytes ? (bytes / 1024 < 1024 ? `${Math.round(bytes / 1024)} KB` : `${(bytes / 1048576).toFixed(1)} MB`) : '';
  }
}
