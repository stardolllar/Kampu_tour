import { Routes } from '@angular/router';
import { LandingComponent } from './features/landing/landing.component';
import { LoginComponent } from './features/login/login.component';
import { ShellComponent } from './layout/shell.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { ReservasComponent } from './features/reservas/reservas.component';
import { ReservaNuevaComponent } from './features/reservas/reserva-nueva.component';
import { ReservaDetalleComponent } from './features/reservas/reserva-detalle.component';
import { SalidasComponent } from './features/salidas/salidas.component';
import { PaquetesComponent } from './features/paquetes/paquetes.component';
import { PagosComponent } from './features/pagos/pagos.component';
import { ClientesComponent } from './features/clientes/clientes.component';
import { RecursosComponent } from './features/recursos/recursos.component';
import { OperacionesComponent } from './features/operaciones/operaciones.component';
import { RegistroComponent } from './features/portal/registro.component';
import { MisPaquetesComponent } from './features/portal/mis-paquetes.component';
import { PaqueteClienteComponent } from './features/portal/paquete-cliente.component';
import { clienteGuard, personalGuard, rolGuard } from './core/services/guards';

// Roles por pantalla (espejo de PKG_SEGURIDAD_ROLES.puede; el backend es quien decide)
const COMERCIAL = { roles: ['ADMIN', 'VENDEDOR'] };

export const routes: Routes = [
  { path: '', component: LandingComponent, pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  // Módulo de clientes (v4)
  { path: 'registro', component: RegistroComponent },
  { path: 'mi-cuenta', component: MisPaquetesComponent, canActivate: [clienteGuard] },
  { path: 'mi-cuenta/paquetes/:id', component: PaqueteClienteComponent, canActivate: [clienteGuard] },
  {
    path: '',
    component: ShellComponent,
    canActivate: [personalGuard],
    canActivateChild: [rolGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'reservas', component: ReservasComponent, data: COMERCIAL },
      { path: 'reservas/nueva', component: ReservaNuevaComponent, data: COMERCIAL },
      { path: 'reservas/:id', component: ReservaDetalleComponent, data: COMERCIAL },
      { path: 'salidas', component: SalidasComponent },
      { path: 'paquetes', component: PaquetesComponent, data: COMERCIAL },
      { path: 'pagos', component: PagosComponent, data: COMERCIAL },
      { path: 'clientes', component: ClientesComponent, data: COMERCIAL },
      { path: 'operaciones', component: OperacionesComponent },
      { path: 'recursos', component: RecursosComponent }
    ]
  },
  { path: '**', redirectTo: '' }
];
