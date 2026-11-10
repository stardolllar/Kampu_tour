import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-badge',
  standalone: true,
  templateUrl: './badge.component.html',
  styleUrl: './badge.component.css'
})
export class BadgeComponent {
  @Input({ required: true }) estado = '';

  private mapa: { [estado: string]: string } = {
    PENDIENTE: 'bg-amber-50 text-amber-700 ring-amber-200/70',
    PARCIAL: 'bg-sky-50 text-sky-700 ring-sky-200/70',
    PROGRAMADA: 'bg-sky-50 text-sky-700 ring-sky-200/70',
    CONFIRMADA: 'bg-emerald-50 text-emerald-700 ring-emerald-200/70',
    APROBADO: 'bg-emerald-50 text-emerald-700 ring-emerald-200/70',
    PAGADO: 'bg-emerald-50 text-emerald-700 ring-emerald-200/70',
    COMPLETADA: 'bg-zinc-100 text-zinc-700 ring-zinc-200',
    ANULADA: 'bg-rose-50 text-rose-700 ring-rose-200/70',
    CANCELADA: 'bg-rose-50 text-rose-700 ring-rose-200/70',
    RECHAZADO: 'bg-rose-50 text-rose-700 ring-rose-200/70',
    REEMBOLSADO: 'bg-violet-50 text-violet-700 ring-violet-200/70',
    ACTIVO: 'bg-emerald-50 text-emerald-700 ring-emerald-200/70',
    INACTIVO: 'bg-zinc-100 text-zinc-500 ring-zinc-200',
    // Proveedores de transporte
    ACTIVA: 'bg-emerald-50 text-emerald-700 ring-emerald-200/70',
    INACTIVA: 'bg-zinc-100 text-zinc-500 ring-zinc-200',
    SUSPENDIDA: 'bg-rose-50 text-rose-700 ring-rose-200/70',
    RETIRADA: 'bg-zinc-100 text-zinc-500 ring-zinc-200',
    DISPONIBLE: 'bg-emerald-50 text-emerald-700 ring-emerald-200/70',
    LIMITADA: 'bg-amber-50 text-amber-700 ring-amber-200/70',
    NO_DISPONIBLE: 'bg-rose-50 text-rose-700 ring-rose-200/70',
    OPERATIVO: 'bg-emerald-50 text-emerald-700 ring-emerald-200/70',
    EN_MANTENIMIENTO: 'bg-amber-50 text-amber-700 ring-amber-200/70',
    FUERA_DE_SERVICIO: 'bg-rose-50 text-rose-700 ring-rose-200/70',
    DESCANSO: 'bg-amber-50 text-amber-700 ring-amber-200/70',
    VACACIONES: 'bg-sky-50 text-sky-700 ring-sky-200/70',
    // Operaciones de transporte (DOMINIO_ESTADOS)
    PROGRAMADO: 'bg-zinc-100 text-zinc-700 ring-zinc-200',
    ASIGNADO: 'bg-sky-50 text-sky-700 ring-sky-200/70',
    EMBARQUE: 'bg-violet-50 text-violet-700 ring-violet-200/70',
    EN_ESPERA_SALIDA: 'bg-violet-50 text-violet-700 ring-violet-200/70',
    EN_RUTA: 'bg-brand-50 text-brand-700 ring-brand-200/70',
    LLEGADA_PROXIMA: 'bg-leaf-50 text-leaf-700 ring-leaf-200/70',
    FINALIZADO: 'bg-emerald-50 text-emerald-700 ring-emerald-200/70',
    RETRASADO: 'bg-amber-50 text-amber-700 ring-amber-200/70',
    CANCELADO: 'bg-rose-50 text-rose-700 ring-rose-200/70'
  };

  get clase() { return this.mapa[this.estado] ?? 'bg-zinc-100 text-zinc-700 ring-zinc-200'; }
  get texto() { return this.estado.replace(/_/g, ' '); }
}
