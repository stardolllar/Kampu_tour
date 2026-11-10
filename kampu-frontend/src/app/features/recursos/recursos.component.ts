import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import {
  CategoriaLicencia, ConductorForm, DisponibilidadOperativa, EmpresaForm, EstadoAfiliacion, EstadoConductor,
  EstadoOperativo, EstadoRuta, GuiaForm, Lugar, LugarForm, RutaForm, TipoLugar, TipoServicio, TipoVehiculo, VehiculoForm
} from '../../core/models';
import { TourStore } from '../../core/services/tour-store.service';
import { AlertService } from '../../core/services/alert.service';
import { IconComponent } from '../../shared/icon.component';
import { BadgeComponent } from '../../shared/badge.component';
import { filtrarEscritura, filtrarPegado, sinRueda } from '../../core/utils/filtro';
import { FormValidacion, V } from '../../core/utils/validacion';
import { duracion, etiqueta } from '../../core/utils/etiquetas';
import { addDays, hoy } from '../../core/utils/date.util';
import { ModalComponent } from '../../shared/modal.component';

type Tab = 'guias' | 'empresas' | 'vehiculos' | 'conductores' | 'lugares' | 'rutas';

@Component({
  selector: 'app-recursos',
  standalone: true,
  imports: [FormsModule, DatePipe, IconComponent, BadgeComponent, ModalComponent],
  templateUrl: './recursos.component.html',
  styleUrl: './recursos.component.css'
})
export class RecursosComponent {
  store = inject(TourStore);
  private alerts = inject(AlertService);

  tabs: { id: Tab; texto: string; icono: string; singular: string }[] = [
    { id: 'guias', texto: 'Guías', icono: 'user', singular: 'guía' },
    { id: 'empresas', texto: 'Empresas', icono: 'flag', singular: 'empresa de transporte' },
    { id: 'vehiculos', texto: 'Vehículos', icono: 'truck', singular: 'vehículo' },
    { id: 'conductores', texto: 'Conductores', icono: 'id', singular: 'conductor' },
    { id: 'lugares', texto: 'Lugares', icono: 'map-pin', singular: 'lugar' },
    { id: 'rutas', texto: 'Rutas', icono: 'compass', singular: 'ruta' }
  ];
  categorias: CategoriaLicencia[] = ['A-IIa', 'A-IIb', 'A-IIIa', 'A-IIIb', 'A-IIIc'];
  afiliaciones: EstadoAfiliacion[] = ['PENDIENTE', 'ACTIVA', 'SUSPENDIDA', 'RETIRADA'];
  disponibilidades: DisponibilidadOperativa[] = ['DISPONIBLE', 'LIMITADA', 'NO_DISPONIBLE'];
  tiposVehiculo: TipoVehiculo[] = ['AUTO', 'CAMIONETA_4X4', 'VAN', 'MINIBUS', 'BUS'];
  estadosVehiculo: EstadoOperativo[] = ['OPERATIVO', 'EN_MANTENIMIENTO', 'FUERA_DE_SERVICIO'];
  estadosConductor: EstadoConductor[] = ['DISPONIBLE', 'DESCANSO', 'VACACIONES', 'INACTIVO'];
  tiposLugar: TipoLugar[] = ['AEROPUERTO', 'TERMINAL', 'HOTEL', 'ATRACTIVO', 'CIUDAD', 'OTRO'];
  tiposServicio: TipoServicio[] = ['TRASLADO', 'TOUR', 'INTERPROVINCIAL'];
  estadosRuta: EstadoRuta[] = ['ACTIVA', 'INACTIVA'];
  etiqueta = etiqueta;
  duracion = duracion;
  hoy = hoy();
  pronto = addDays(hoy(), 30);

  tab = signal<Tab>('guias');
  filtroEmpresa = signal<number | null>(null);
  abierto = signal(false);
  editId: number | null = null;

  guia: GuiaForm = this.guiaVacia();
  empresa: EmpresaForm = this.empresaVacia();
  vehiculo: VehiculoForm = this.vehiculoVacio();
  conductor: ConductorForm = this.conductorVacio();
  lugar: LugarForm = this.lugarVacio();
  ruta: RutaForm = this.rutaVacia();

  v = new FormValidacion(() => {
    switch (this.tab()) {
      case 'guias': return {
        gNombre: V.nombrePersona(this.guia.nombreCompleto),
        gEspecialidad: V.texto(this.guia.especialidad, 'La especialidad', 100),
        gTelefono: V.telefono(this.guia.telefono)
      };
      case 'empresas': return {
        eNombre: V.texto(this.empresa.nombreComercial, 'El nombre comercial', 150, true),
        eRuc: V.ruc(this.empresa.ruc),
        eContacto: this.empresa.contactoNombre.trim() ? V.nombrePersona(this.empresa.contactoNombre, 'El nombre del contacto') : null,
        eTelefono: this.empresa.contactoTelefono.trim() ? V.telefono(this.empresa.contactoTelefono) : 'El teléfono de contacto es obligatorio.',
        eEmail: V.email(this.empresa.contactoEmail),
        eDireccion: V.texto(this.empresa.direccion, 'La dirección', 250)
      };
      case 'vehiculos': return {
        vEmpresa: this.vehiculo.empresaId ? null : 'Selecciona la empresa.',
        vPlaca: V.placa(this.vehiculo.placa),
        vMarca: V.texto(this.vehiculo.marcaModelo, 'La marca/modelo', 100),
        vCapacidad: V.entero(this.vehiculo.capacidadPax, 'La capacidad', 1, 999),
        vMantenimiento: V.fecha(this.vehiculo.fechaProxMantenimiento, 'La fecha del próximo mantenimiento')
      };
      case 'conductores': {
        const lic = this.conductor.numeroLicencia.trim();
        return {
          cEmpresa: this.conductor.empresaId ? null : 'Selecciona la empresa.',
          cNombre: V.nombrePersona(this.conductor.nombres),
          cLicencia: !lic ? 'El número de licencia es obligatorio.'
            : (/^[A-Za-z0-9]{6,20}$/.test(lic) ? null : 'La licencia solo admite letras y números (6 a 20 caracteres).'),
          cVencimiento: V.fecha(this.conductor.fechaVencLicencia, 'La fecha de vencimiento'),
          cTelefono: V.telefono(this.conductor.telefono)
        };
      }
      case 'lugares': return {
        lNombre: V.texto(this.lugar.nombre, 'El nombre', 150, true),
        lCiudad: V.texto(this.lugar.ciudad, 'La ciudad', 100, true)
      };
      case 'rutas': {
        const r = this.ruta;
        return {
          rEmpresa: r.empresaId ? null : 'Selecciona la empresa.',
          rOrigen: r.origenId ? null : 'Selecciona el origen.',
          rDestino: !r.destinoId ? 'Selecciona el destino.' : (r.destinoId === r.origenId ? 'El destino debe ser distinto del origen.' : null),
          rDuracion: V.entero(r.duracionEstimadaMin, 'La duración', 1, 9999),
          rTarifa: r.tarifaReferencial === null || (r.tarifaReferencial as unknown) === '' ? null
            : (r.tarifaReferencial < 0 ? 'La tarifa no puede ser negativa.' : null)
        };
      }
    }
  });

  // ---------- Listas ----------
  empresasActivas = computed(() => this.store.empresas().filter(e => e.estadoAfiliacion !== 'RETIRADA'));
  vehiculosFiltrados = computed(() => {
    const e = this.filtroEmpresa();
    return this.store.vehiculos().filter(v => !e || v.empresaId === e);
  });
  conductoresFiltrados = computed(() => {
    const e = this.filtroEmpresa();
    return this.store.conductores().filter(c => !e || c.empresaId === e);
  });
  rutasFiltradas = computed(() => {
    const e = this.filtroEmpresa();
    return this.store.rutas().filter(r => !e || r.empresaId === e);
  });
  lugaresPorCiudad = computed(() => {
    const grupos: { ciudad: string; lugares: Lugar[] }[] = [];
    for (const l of this.store.lugares()) {
      let g = grupos.find(x => x.ciudad === l.ciudad);
      if (!g) { g = { ciudad: l.ciudad, lugares: [] }; grupos.push(g); }
      g.lugares.push(l);
    }
    return grupos;
  });

  nombreEmpresa(id?: number | null) { return this.store.getEmpresa(id)?.nombreComercial ?? '—'; }
  vehiculosDe(empresaId: number) { return this.store.vehiculos().filter(v => v.empresaId === empresaId).length; }
  rutasDe(empresaId: number) { return this.store.rutas().filter(r => r.empresaId === empresaId).length; }

  get actual() { return this.tabs.find(t => t.id === this.tab())!; }
  cambiarTab(t: Tab) { this.tab.set(t); }

  // ---------- Formularios vacíos ----------
  private guiaVacia(): GuiaForm { return { nombreCompleto: '', especialidad: '', telefono: '' }; }
  private empresaVacia(): EmpresaForm {
    return { nombreComercial: '', ruc: '', contactoNombre: '', contactoTelefono: '', contactoEmail: '', direccion: '',
      estadoAfiliacion: 'PENDIENTE', disponibilidadOperativa: 'DISPONIBLE', fechaAfiliacion: hoy() };
  }
  private vehiculoVacio(): VehiculoForm {
    return { empresaId: this.filtroEmpresa(), placa: '', tipoVehiculo: 'VAN', marcaModelo: '', capacidadPax: 12,
      fechaProxMantenimiento: addDays(hoy(), 180), estadoOperativo: 'OPERATIVO' };
  }
  private conductorVacio(): ConductorForm {
    return { empresaId: this.filtroEmpresa(), nombres: '', numeroLicencia: '', categoriaLicencia: 'A-IIb',
      fechaVencLicencia: addDays(hoy(), 365), telefono: '', estadoDisponibilidad: 'DISPONIBLE' };
  }
  private lugarVacio(): LugarForm { return { nombre: '', tipoLugar: 'HOTEL', ciudad: '' }; }
  private rutaVacia(): RutaForm {
    return { empresaId: this.filtroEmpresa(), origenId: null, destinoId: null, tipoServicio: 'TRASLADO',
      duracionEstimadaMin: 30, tarifaReferencial: null, estado: 'ACTIVA' };
  }

  nuevo() {
    this.editId = null;
    this.guia = this.guiaVacia();
    this.empresa = this.empresaVacia();
    this.vehiculo = this.vehiculoVacio();
    this.conductor = this.conductorVacio();
    this.lugar = this.lugarVacio();
    this.ruta = this.rutaVacia();
    this.abrir();
  }

  private abrir() { this.v.reiniciar(); this.abierto.set(true); }

  editarGuia(id: number) {
    const g = this.store.getGuia(id)!;
    this.editId = id;
    this.guia = { nombreCompleto: g.nombreCompleto, especialidad: g.especialidad, telefono: g.telefono };
    this.abrir();
  }
  editarEmpresa(id: number) {
    const e = this.store.getEmpresa(id)!;
    this.editId = id;
    this.empresa = {
      nombreComercial: e.nombreComercial, ruc: e.ruc, contactoNombre: e.contactoNombre, contactoTelefono: e.contactoTelefono,
      contactoEmail: e.contactoEmail, direccion: e.direccion, estadoAfiliacion: e.estadoAfiliacion,
      disponibilidadOperativa: e.disponibilidadOperativa, fechaAfiliacion: e.fechaAfiliacion
    };
    this.abrir();
  }
  editarVehiculo(id: number) {
    const x = this.store.getVehiculo(id)!;
    this.editId = id;
    this.vehiculo = {
      empresaId: x.empresaId, placa: x.placa, tipoVehiculo: x.tipoVehiculo, marcaModelo: x.marcaModelo, capacidadPax: x.capacidadPax,
      fechaProxMantenimiento: x.fechaProxMantenimiento, estadoOperativo: x.estadoOperativo
    };
    this.abrir();
  }
  editarConductor(id: number) {
    const c = this.store.getConductor(id)!;
    this.editId = id;
    this.conductor = {
      empresaId: c.empresaId, nombres: c.nombres, numeroLicencia: c.numeroLicencia, categoriaLicencia: c.categoriaLicencia,
      fechaVencLicencia: c.fechaVencLicencia, telefono: c.telefono, estadoDisponibilidad: c.estadoDisponibilidad
    };
    this.abrir();
  }
  editarLugar(id: number) {
    const l = this.store.getLugar(id)!;
    this.editId = id;
    this.lugar = { nombre: l.nombre, tipoLugar: l.tipoLugar, ciudad: l.ciudad };
    this.abrir();
  }
  editarRuta(id: number) {
    const r = this.store.rutas().find(x => x.rutaId === id)!;
    this.editId = id;
    this.ruta = {
      empresaId: r.empresaId, origenId: r.origenId, destinoId: r.destinoId, tipoServicio: r.tipoServicio,
      duracionEstimadaMin: r.duracionEstimadaMin, tarifaReferencial: r.tarifaReferencial, estado: r.estado
    };
    this.abrir();
  }

  // ---------- Guardar / eliminar ----------
  guardar() {
    if (this.store.ocupado()) return;
    if (!this.v.valido()) { this.alerts.toast('Revisa los campos marcados en rojo', 'error'); return; }
    const t = (s: string) => s.trim();
    let peticion: Observable<unknown>;
    switch (this.tab()) {
      case 'guias':
        peticion = this.store.guardarGuia({ nombreCompleto: t(this.guia.nombreCompleto), especialidad: t(this.guia.especialidad),
          telefono: t(this.guia.telefono) }, this.editId);
        break;
      case 'empresas': {
        const e = this.empresa;
        peticion = this.store.guardarEmpresa({ ...e, nombreComercial: t(e.nombreComercial), ruc: t(e.ruc), contactoNombre: t(e.contactoNombre),
          contactoTelefono: t(e.contactoTelefono), contactoEmail: t(e.contactoEmail), direccion: t(e.direccion),
          fechaAfiliacion: e.fechaAfiliacion || null }, this.editId);
        break;
      }
      case 'vehiculos': {
        const x = this.vehiculo;
        peticion = this.store.guardarVehiculo({ ...x, placa: t(x.placa).toUpperCase(), marcaModelo: t(x.marcaModelo) }, this.editId);
        break;
      }
      case 'conductores': {
        const c = this.conductor;
        peticion = this.store.guardarConductor({ ...c, nombres: t(c.nombres), numeroLicencia: t(c.numeroLicencia).toUpperCase(),
          telefono: t(c.telefono) }, this.editId);
        break;
      }
      case 'lugares':
        peticion = this.store.guardarLugar({ ...this.lugar, nombre: t(this.lugar.nombre), ciudad: t(this.lugar.ciudad) }, this.editId);
        break;
      default: {
        const r = this.ruta;
        const tarifa = r.tarifaReferencial === null || (r.tarifaReferencial as unknown) === '' ? null : r.tarifaReferencial;
        peticion = this.store.guardarRuta({ ...r, tarifaReferencial: tarifa }, this.editId);
      }
    }
    this.store.ocupado.set(true);
    peticion.subscribe({
      next: () => {
        this.store.ocupado.set(false);
        this.recargar();
        this.abierto.set(false);
        this.alerts.toast('Guardado');
      },
      error: (err) => {
        this.store.ocupado.set(false);
        this.alerts.error(this.store.error(err));
      }
    });
  }

  // Vehículos y conductores cambian la capacidad de la empresa (VW_EMPRESA_CAPACIDAD)
  private recargar() {
    switch (this.tab()) {
      case 'guias': this.store.cargarGuias(); break;
      case 'lugares': this.store.cargarLugares(); break;
      case 'rutas': this.store.cargarRutas(); break;
      case 'vehiculos': this.store.cargarVehiculos(); this.store.cargarEmpresas(); break;
      case 'conductores': this.store.cargarConductores(); this.store.cargarEmpresas(); break;
      default: this.store.cargarEmpresas();
    }
  }

  async eliminar(id: number) {
    const tab = this.tab();
    if (!(await this.alerts.confirm('Esta acción no se puede deshacer.', 'Eliminar ' + this.actual.singular))) return;
    const peticiones: { [t in Tab]: () => Observable<void> } = {
      guias: () => this.store.eliminarGuia(id),
      empresas: () => this.store.eliminarEmpresa(id),
      vehiculos: () => this.store.eliminarVehiculo(id),
      conductores: () => this.store.eliminarConductor(id),
      lugares: () => this.store.eliminarLugar(id),
      rutas: () => this.store.eliminarRuta(id)
    };
    peticiones[tab]().subscribe({
      next: () => { this.recargar(); this.alerts.toast('Eliminado'); },
      error: (err) => this.alerts.error(this.store.error(err))
    });
  }

  filtrar(evento: Event, tipo: string) { filtrarEscritura(evento, tipo); }
  pegar(evento: Event, tipo: string) { filtrarPegado(evento, tipo); }
  rueda(evento: Event) { sinRueda(evento); }
}
