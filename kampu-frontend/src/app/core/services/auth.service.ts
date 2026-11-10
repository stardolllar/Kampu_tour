import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { RegistroForm, Sesion } from '../models';
import { API } from '../utils/api';

const KEY = 'tm4_session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private sesion = signal<Sesion | null>(this.cargar());
  usuario = computed(() => this.sesion());
  autenticado = computed(() => this.sesion() !== null);
  esCliente = computed(() => this.sesion()?.rol === 'CLIENTE');
  rol = computed(() => this.sesion()?.rol ?? null);

  private cargar(): Sesion | null {
    const texto = localStorage.getItem(KEY);
    if (!texto) return null;
    try {
      const s = JSON.parse(texto);
      return s && s.token && s.rol ? s : null;
    } catch {
      return null;
    }
  }

  // Personal interno: usuario · Cliente: correo o número de documento
  login(username: string, password: string): Observable<Sesion> {
    return this.http.post<Sesion>(`${API}/auth/login`, { username: username.trim(), password: password });
  }

  // "Registrarse": crea la cuenta del cliente y devuelve la sesión iniciada
  registrar(d: RegistroForm): Observable<Sesion> {
    return this.http.post<Sesion>(`${API}/auth/registro`, d);
  }

  guardarSesion(s: Sesion) {
    this.sesion.set(s);
    localStorage.setItem(KEY, JSON.stringify(s));
  }

  // Ruta de inicio según el rol
  inicio(): string {
    return this.esCliente() ? '/mi-cuenta' : '/dashboard';
  }

  logout() {
    if (this.sesion()) this.http.post(`${API}/auth/logout`, {}).subscribe({ error: () => undefined });
    this.olvidar();
  }

  // Sin llamar al backend (token vencido o rechazado)
  olvidar() {
    this.sesion.set(null);
    localStorage.removeItem(KEY);
  }
}
