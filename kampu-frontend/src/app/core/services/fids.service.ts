import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RecursosFids, Segmento, TableroFids, TipoSegmento } from '../itinerario.models';
import { API } from '../utils/api';

// Torre de control (solo Admin)
@Injectable({ providedIn: 'root' })
export class FidsService {
  private http = inject(HttpClient);
  private url = `${API}/fids`;

  tablero(fecha: string, tipo: TipoSegmento | '' = ''): Observable<TableroFids> {
    let p = new HttpParams().set('fecha', fecha);
    if (tipo) p = p.set('tipo', tipo);
    return this.http.get<TableroFids>(this.url, { params: p });
  }

  recursos(): Observable<RecursosFids> {
    return this.http.get<RecursosFids>(`${this.url}/recursos`);
  }

  asignar(segmentoIds: number[], vehiculoId: number, operadorId: number): Observable<Segmento[]> {
    return this.http.post<Segmento[]>(`${this.url}/asignaciones`, { segmentoIds, vehiculoId, operadorId });
  }

  liberar(codigo: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/asignaciones/${encodeURIComponent(codigo)}`);
  }
}
