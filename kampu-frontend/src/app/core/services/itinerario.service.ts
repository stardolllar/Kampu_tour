import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { from, Observable, switchMap, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { BoletoForm, Itinerario, ItinerarioForm } from '../itinerario.models';
import { API } from '../utils/api';

/**
 * Booking y vuelos (Ventas / Admin): itinerarios y carga de boletos aéreos.
 * La subida va como multipart/form-data: "datos" (JSON) + "archivo" (PDF).
 */
@Injectable({ providedIn: 'root' })
export class ItinerarioService {
  private http = inject(HttpClient);
  private url = `${API}/itinerarios`;

  listar(): Observable<Itinerario[]> {
    return this.http.get<Itinerario[]>(this.url);
  }

  detalle(reservaId: number): Observable<Itinerario> {
    return this.http.get<Itinerario>(`${this.url}/${reservaId}`);
  }

  guardar(reservaId: number, f: ItinerarioForm): Observable<Itinerario> {
    return this.http.put<Itinerario>(`${this.url}/${reservaId}`, {
      hotelId: f.hotelId, aeropuertoId: f.aeropuertoId,
      tours: f.tours.map(t => ({ destinoId: t.destinoId, horaRecojo: segundos(t.horaRecojo) }))
    });
  }

  // FormData: el navegador arma el boundary del multipart (no fijar Content-Type a mano)
  registrarBoleto(reservaId: number, datos: BoletoForm, archivo: File | null): Observable<Itinerario> {
    const fd = new FormData();
    const cuerpo = { ...datos, fechaHoraSalida: segundos(datos.fechaHoraSalida), fechaHoraLlegada: segundos(datos.fechaHoraLlegada) };
    fd.append('datos', new Blob([JSON.stringify(cuerpo)], { type: 'application/json' }));
    if (archivo) fd.append('archivo', archivo, archivo.name);
    return this.http.post<Itinerario>(`${this.url}/${reservaId}/boletos`, fd);
  }

  pdf(boletoId: number): Observable<Blob> {
    return descargar(this.http, `${this.url}/boletos/${boletoId}/pdf`);
  }
}

// "2026-11-10T10:30" -> "2026-11-10T10:30:00" (LocalDateTime de Java)
export function segundos(v: string): string {
  return v && v.length === 16 ? v + ':00' : v;
}

// Descarga binaria con el token; si el servidor responde error, el JSON viene dentro del Blob
export function descargar(http: HttpClient, url: string): Observable<Blob> {
  return http.get(url, { responseType: 'blob' }).pipe(
    catchError((e: unknown) => {
      if (e instanceof HttpErrorResponse && e.error instanceof Blob) {
        return from(e.error.text()).pipe(switchMap(t => {
          let cuerpo: unknown = null;
          try { cuerpo = JSON.parse(t); } catch { /* no es JSON */ }
          return throwError(() => new HttpErrorResponse({ error: cuerpo, status: e.status, url: e.url ?? undefined }));
        }));
      }
      return throwError(() => e);
    }));
}

// Abre un PDF (Blob) en una pestaña nueva
export function abrirPdf(blob: Blob) {
  const url = URL.createObjectURL(blob);
  window.open(url, '_blank', 'noopener');
  setTimeout(() => URL.revokeObjectURL(url), 60_000);
}
