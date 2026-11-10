import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

// Envía el usuario de la sesión en "X-Usuario": el backend lo pone como CLIENT_IDENTIFIER
// y trg_operacion_transporte lo guarda en HISTORIAL_TRAFICO_OPERACION.usuario_registro.
export const usuarioInterceptor: HttpInterceptorFn = (req, next) => {
  const usuario = inject(AuthService).usuario();
  if (!usuario || !req.url.startsWith('/api/')) return next(req);
  return next(req.clone({ setHeaders: { 'X-Usuario': usuario.username } }));
};
