import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EstadoSegmento, ViajeOperador } from '../itinerario.models';
import { API } from '../utils/api';

// App del operador: solo SUS viajes de hoy (el backend valida la propiedad con @PreAuthorize)
@Injectable({ providedIn: 'root' })
export class OperadorCampoService {
  private http = inject(HttpClient);
  private url = `${API}/operador`;

  jornada(): Observable<ViajeOperador[]> {
    return this.http.get<ViajeOperador[]>(`${this.url}/jornada`);
  }

  avanzarViaje(codigo: string, estado: EstadoSegmento, observacion = ''): Observable<ViajeOperador> {
    return this.http.patch<ViajeOperador>(`${this.url}/viajes/${encodeURIComponent(codigo)}/estado`, { estado, observacion });
  }

  avanzarSegmento(segmentoId: number, estado: EstadoSegmento): Observable<ViajeOperador> {
    return this.http.patch<ViajeOperador>(`${this.url}/segmentos/${segmentoId}/estado`, { estado });
  }
}
