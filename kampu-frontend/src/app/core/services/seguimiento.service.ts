import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SeguimientoCliente } from '../itinerario.models';
import { API } from '../utils/api';
import { descargar } from './itinerario.service';

// Portal del pasajero: su itinerario en vivo, PDF de boletos y comprobante
@Injectable({ providedIn: 'root' })
export class SeguimientoService {
  private http = inject(HttpClient);

  itinerario(reservaId: number): Observable<SeguimientoCliente> {
    return this.http.get<SeguimientoCliente>(`${API}/portal/itinerarios/${reservaId}`);
  }

  pdf(boletoId: number): Observable<Blob> {
    return descargar(this.http, `${API}/portal/boletos/${boletoId}/pdf`);
  }

  comprobante(reservaId: number): Observable<string> {
    return this.http.get(`${API}/portal/itinerarios/${reservaId}/comprobante`, { responseType: 'text' });
  }
}
