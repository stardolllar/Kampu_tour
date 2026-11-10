import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Rol } from '../models';
import { AuthService } from './auth.service';

// Panel interno: solo personal (ADMIN / VENDEDOR / OPERADOR). El cliente va a su portal.
export const personalGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.autenticado()) return router.parseUrl('/login');
  return auth.esCliente() ? router.parseUrl('/mi-cuenta') : true;
};

// Portal del cliente: solo rol CLIENTE
export const clienteGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.autenticado()) return router.parseUrl('/login');
  return auth.esCliente() ? true : router.parseUrl('/dashboard');
};

// Pantallas del panel restringidas por rol (data.roles). El backend vuelve a validar (PKG_SEGURIDAD_ROLES).
export const rolGuard: CanActivateFn = (ruta) => {
  const auth = inject(AuthService);
  const roles = (ruta.data?.['roles'] ?? []) as Rol[];
  return !roles.length || roles.includes(auth.rol() as Rol) ? true : inject(Router).parseUrl('/dashboard');
};
