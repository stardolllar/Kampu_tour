import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/services/auth.service';
import { TourStore } from '../core/services/tour-store.service';
import { AlertService } from '../core/services/alert.service';
import { IconComponent } from '../shared/icon.component';
import { LogoComponent } from '../shared/logo.component';
import { etiqueta } from '../core/utils/etiquetas';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, IconComponent, LogoComponent],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.css'
})
export class ShellComponent {
  auth = inject(AuthService);
  private store = inject(TourStore);
  private router = inject(Router);
  private alerts = inject(AlertService);

  abierto = signal(false);

  // Menú según el rol (mismas reglas que app.routes.ts y PKG_SEGURIDAD_ROLES)
  private todo: { ruta: string; texto: string; icono: string; roles?: string[]; exact?: boolean }[] = [
    { ruta: '/dashboard', texto: 'Panel', icono: 'home' },
    { ruta: '/reservas', texto: 'Reservas', icono: 'ticket', roles: ['ADMIN', 'VENDEDOR'] },
    { ruta: '/salidas', texto: 'Salidas', icono: 'calendar' },
    { ruta: '/paquetes', texto: 'Paquetes', icono: 'package', roles: ['ADMIN', 'VENDEDOR'] },
    { ruta: '/pagos', texto: 'Pagos', icono: 'card', roles: ['ADMIN', 'VENDEDOR'] },
    { ruta: '/clientes', texto: 'Clientes', icono: 'users', roles: ['ADMIN', 'VENDEDOR'] },
    { ruta: '/operaciones', texto: 'Tráfico', icono: 'truck' },
    { ruta: '/recursos', texto: 'Logística', icono: 'map-pin' }
  ];
  menu = computed(() => this.todo.filter(m => !m.roles || m.roles.includes(this.auth.rol() ?? '')));
  rolTexto = computed(() => etiqueta(this.auth.rol()));

  pendientes = computed(() => this.store.reservas().filter(r => r.estadoReserva === 'PENDIENTE').length);
  iniciales = computed(() => (this.auth.usuario()?.username ?? '?').slice(0, 2).toUpperCase());

  constructor() {
    if (!this.auth.autenticado()) {
      this.router.navigate(['/login']);
      return;
    }
    this.store.cargar(true, error => {
      if (error && this.auth.autenticado()) this.alerts.error(error, 'No se pudieron cargar los datos');
    });
  }

  async salir() {
    if (await this.alerts.confirm('Se cerrará tu sesión actual.', 'Cerrar sesión', 'Cerrar sesión')) {
      this.auth.logout();
      this.store.limpiar();
      this.router.navigate(['/login']);
    }
  }
}
