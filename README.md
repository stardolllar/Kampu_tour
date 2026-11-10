# KAMPU · Tour Manager v3 — sistema integrado (Angular + Spring Boot + Oracle)

```
proyecto/
├── kampu-backend/      Backend Spring Boot 3.4 (Java 21) — API REST en http://localhost:8082/lp2_gp2
│   └── src/main/resources/db/
│       ├── 00_reset.sql      Solo si ya tenías una base creada: borra el esquema anterior y el v3
│       ├── 01_schema_v3.sql       Script v3: tablas, PKG_ASIGNACION (PL/SQL), vistas y triggers
│       ├── 02_extension_kampu.sql Teléfonos/documentos únicos, rutas hacia paquetes, PKG_RUTAS (duración real)
│       └── 03_data.sql            Datos de demostración (se cargan pasando por los triggers)
└── kampu-frontend/     Frontend Angular 18
```

## Versión 3 (Kampu Tour Manager v3)

El proyecto quedó adaptado al script `kampu_tour_manager_v3.sql`. La lógica de negocio pesada vive ahora en
**Oracle** (PL/SQL, triggers y vistas) y el backend la usa en vez de repetirla en Java:

| Antes | v3 |
|---|---|
| `TRANSPORTE`, `CHOFERES` | `EMPRESA_TRANSPORTE` (proveedores afiliados), `VEHICULO` y `CONDUCTOR` (de una empresa), `LUGARES`, `RUTAS_PROVEEDOR` |
| Vehículo y chofer en `SALIDAS_PROGRAMADAS` | `OPERACION_TRANSPORTE`: cada tramo de la salida (aeropuerto → hotel, hotel → atractivo…) con su empresa, vehículo, conductor y estado |
| `RESERVAS.salida_id` / `numero_viajeros` | `DETALLE_RESERVA` (una línea por salida) y los pasajeros cuelgan del detalle |
| Cupos y montos calculados en Java | Triggers: cupos, precio congelado y `monto_total`; la anulación devuelve los cupos |
| — | `HISTORIAL_TRAFICO_OPERACION`, catálogo `DOMINIO_ESTADOS` / `DOMINIO_TRANSICIONES` |
| Roles `ADMIN`, `VENTAS` | `ADMIN`, `VENDEDOR`, `OPERADOR` (`chk_usu_rol`) |
| Pagos solo con moneda | `tipo_cambio` y `monto_pen` (columna virtual) |
| Clientes DNI/CE/PASAPORTE | también **RUC** (empresas); documento único por tipo |

### Dónde usa el backend cada objeto PL/SQL

| Objeto de la base de datos | Qué hace | Lo usa |
|---|---|---|
| `PKG_ASIGNACION.asignar_automatico` | Elige vehículo + conductor de una misma empresa y deja la operación ASIGNADA | `POST /operaciones/{id}/asignar-automatico` (`PkgAsignacionRepository`) |
| `PKG_ASIGNACION.motivo_vehiculo` / `motivo_conductor` / `horas_conductor_dia` | Por qué un recurso no es elegible (cruce, mantenimiento, licencia, 10 h/día…) | `GET /operaciones/{id}/candidatos` (pantalla *Asignar*) |
| `PKG_ASIGNACION.faltante_transporte` | Tramo de la salida cuyo transporte no alcanza | `faltanteTransporte` de `GET /salidas` |
| `VW_RECURSOS_DISPONIBLES` | Recursos elegibles para una operación PROGRAMADA | `GET /operaciones/{id}/recursos-disponibles` |
| `VW_TABLERO_OPERACIONES` | Tablero con nombres, horas reales y minutos de retraso | `GET /operaciones` (entidad `VistaTableroOperacion`) |
| `VW_EMPRESA_CAPACIDAD` | Capacidad total, vehículos y conductores disponibles por empresa | `GET /empresas` (entidad `VistaEmpresaCapacidad`) |
| `V_RESERVAS_RESUMEN` | Número de viajeros y saldo de cada reserva | `GET /reservas` (entidad `VistaReservaResumen`) |
| `trg_salida_biu` | Calcula `fecha_fin` y `cupos_disponibles` | Crear/editar salidas (el backend envía `fecha_fin` nula y relee la fila) |
| `trg_salida_confirmar` | Solo confirma una salida con transporte suficiente | `PUT /salidas/{id}` con estado CONFIRMADA |
| `trg_detalle_reserva_biud` | Valida salida y cupos, congela el precio, suma `monto_total` y controla el transporte de salidas confirmadas | `POST /reservas` |
| `trg_pasajeros_cupo` | No más pasajeros que la cantidad del detalle | `POST /reservas` |
| `trg_reserva_estado_bu` | Al anular devuelve los cupos; una anulada no se reactiva | `PATCH /reservas/{id}/anular` |
| `trg_operacion_transporte` | Código `KMP-###`, llegada estimada, transiciones, recursos obligatorios, ruta activa, cruces, descanso, mantenimiento, capacidad e historial | `POST/PUT /operaciones`, `PATCH /operaciones/{id}/estado`, `PATCH /operaciones/{id}/asignar` |

- Los mensajes de `RAISE_APPLICATION_ERROR` (ORA-20xxx) llegan tal cual al usuario (respuesta 400), y las restricciones
  UNIQUE/CHECK/FK se traducen por nombre (`exception/ErroresOracle.java`). Ejemplo:
  `KMP-008 - vehículo: capacidad insuficiente (4 < 10 pax)`.
- Las columnas que mantienen los triggers (`cupos_disponibles`, `monto_total`, `codigo_operacion`, `subtotal`, `monto_pen`)
  están mapeadas con `insertable = false, updatable = false`, y las entidades usan `@DynamicUpdate` para que los triggers
  `UPDATE OF ...` solo se disparen con las columnas que realmente cambian.
- El historial de tráfico guarda quién hizo cada cambio: el frontend envía el encabezado `X-Usuario` y el backend lo pone
  en `DBMS_SESSION.SET_IDENTIFIER` (el trigger lee `USERENV.CLIENT_IDENTIFIER`).
- Todas las entidades y vistas se verificaron contra el esquema v3 con `spring.jpa.hibernate.ddl-auto=validate`.

### Ajuste al script v3

Una sola línea de `01_schema_v3.sql` cambia respecto al archivo original: en
`INSERT INTO DOMINIO_TRANSICIONES VALUES ('ASIGNADO', 'PROGRAMADO');   -- desasignar` el comentario va ahora en su propia
línea. En **SQL*Plus**, un comentario después del `;` hace que la sentencia se una con la siguiente y **fallan las dos**
(se perdían las transiciones ASIGNADO → PROGRAMADO y ASIGNADO → EMBARQUE). En SQL Developer (F5) no pasaba.

### Extensión (`02_extension_kampu.sql`)

| Regla / función | Cómo se aplica |
|---|---|
| **Teléfonos sin repetir** en clientes, guías, conductores y empresas | Índices únicos `uq_cli_telefono`, `uq_guia_telefono`, `uq_cond_telefono`, `uq_emp_telefono` sobre los dígitos (`TRANSLATE(telefono, '0+-() ', '0')`): `+51 987 654 321` = `+51987654321`. El backend y los formularios avisan antes. |
| **Documento del cliente sin repetir** (DNI, CE, pasaporte o RUC) | Índice único `uq_cli_numero_doc` sobre `UPPER(numero_documento)`, sin importar el tipo. |
| **Un pasajero no se repite en la misma salida** | Trigger `trg_pasajeros_doc_salida` (reservas activas; en otra salida sí puede viajar). |
| **Rutas hacia un paquete** | `RUTAS_PROVEEDOR.paquete_id` y `PAQUETES_TOUR.lugar_destino_id`. En *Logística → Rutas*, el destino puede ser un **paquete**: el destino es el lugar del paquete (`trg_ruta_biu`) y al crear la ruta **se crea automáticamente su salida programada** (fecha, cupos y guía en el mismo formulario). |
| **Duración real de la ruta** | `LUGARES.latitud/longitud` (coordenadas reales). Al dejar la duración vacía o pulsar *Calcular*, el backend consulta **OSRM** (red vial de OpenStreetMap, `GET /rutas/estimar-duracion`); sin internet usa `PKG_RUTAS.duracion_estimada_min` (distancia haversine calibrada con tiempos reales). Si faltan coordenadas, `trg_ruta_biu` pide indicarla a mano. |

- OSRM se configura en `application.yaml` (`OSRM_URL`, `OSRM_HABILITADO`). El servidor público es para pruebas; en
  producción conviene uno propio.
- La estimación sin internet es aproximada (±20 % en la mayoría de rutas de prueba; en carreteras costeras rectas,
  como Paracas → Huacachina, sobreestima). Ajusta las constantes de `PKG_RUTAS` o escribe la duración a mano.
- **Base v3 que ya tiene datos:** ejecuta solo `02_extension_kampu.sql` (no borra nada). Antes revisa las consultas de su
  sección 0: si hay teléfonos o documentos repetidos, la creación del índice falla hasta corregirlos. Luego registra las
  coordenadas de tus lugares (Logística → Lugares) y el lugar de destino de cada paquete.

### Pantallas nuevas o cambiadas (frontend)

- **Tráfico** (`/operaciones`): tablero de operaciones con filtros, asignación automática (`PKG_ASIGNACION`) o manual
  (con el motivo de cada recurso no elegible), cambio de estado según las transiciones permitidas, historial y observaciones.
- **Logística** (`/recursos`): Guías, Empresas (con capacidad de la vista), Vehículos, Conductores, Lugares y Rutas.
- **Salidas**: la fecha fin la calcula la base de datos; la columna Transporte muestra las operaciones de la salida y si
  falta capacidad (`faltante_transporte`). Vehículo y chofer ya no se eligen aquí.
- **Reserva**: muestra las operaciones de su salida, el precio por persona y el descuento del detalle.
- **Clientes**: admite RUC (razón social). Login: nueva cuenta demo `operador`.

## Rediseño visual del frontend (UI/UX)

Esta versión solo cambia la **apariencia** del frontend (`kampu-frontend`). El backend, la base de datos, la API,
los servicios, las rutas y la lógica de los componentes son los mismos.

- **Identidad visual**: nuevo logotipo Kampu (isotipo "K" atravesada por un camino + wordmark con hoja) en `src/app/shared/logo.component.*` y como favicon.
- **Sistema de diseño** (`src/index.html` + `src/styles.css`): esquinas redondeadas, sombras suaves, paleta KAMPU
  del logotipo (verde oliva `#4B5B1D`, hoja lima `#C4BD45` y fondo crema), tipografía Nunito y clases reutilizables: `btn-dark` (principal), `btn-brand` (guardar/confirmar),
  `btn-light` (secundario/cancelar), `btn-danger` (eliminar/anular), `icon-btn*`, `field`, `label`/`label req`,
  `form-section`, `form-actions`, `card`, `tbl`, `seg`/`seg-btn` (filtros), `alert-*`, `empty`, `progress`, `chip`.
- **Barra lateral fija y compacta**: en escritorio muestra solo los iconos y se expande suavemente al pasar el cursor
  (o con el teclado), resaltando la sección activa; en móvil se abre como menú lateral.
- **Formularios** organizados en secciones, con campos obligatorios (`*`) y opcionales marcados, foco visible,
  selects y fechas estilizados.
- **Tablas, tarjetas, KPIs, modales y alertas (SweetAlert2)** rediseñados, con animaciones discretas
  (se desactivan si el sistema pide "reducir movimiento") y diseño responsive.

## Validación de formularios (frontend + backend)

Las mismas reglas se aplican en ambos lados: `kampu-frontend/src/app/core/utils/validacion.ts` y
`kampu-backend/src/main/java/pe/edu/upeu/lp2_gp2/dto/Reglas.java`.

| Campo | Regla |
|---|---|
| Nombres de personas (cliente, guía, pasajero) | Letras (con tildes y ñ), espacios y `. ' -` |
| DNI / CE / Pasaporte / RUC | DNI: 8 dígitos · CE: 8 a 12 letras o números · Pasaporte: 6 a 12 letras o números · RUC (solo clientes y empresas): 11 dígitos que empiezan con 10, 15, 17 o 20 |
| Teléfono (opcional) | Números con `+ ( ) -`, entre 6 y 15 dígitos |
| Correo (opcional) | Formato `nombre@dominio.com` |
| Placa | Letras, números y guion (3 a 10, `VEHICULO.placa` es `VARCHAR2(10)`) |
| Días / noches / cupos / capacidad | Enteros: 1–99 / 0–99 (y noches ≤ días) / 1–999 / 1–999 |
| Precio base / monto | Mayor a 0, máximo 2 decimales (cabe en `NUMBER(10,2)`); el pago aprobado no puede exceder el saldo |
| Fecha de nacimiento (opcional) | Anterior a hoy y desde 1900 |

- En el navegador, las funciones de `core/utils/filtro.ts` (enlazadas con `(beforeinput)` y `(paste)` en las plantillas) impiden escribir o pegar caracteres inválidos
  (por ejemplo letras o la "e" en campos numéricos) y los mensajes aparecen debajo de cada campo.
- En el backend, Bean Validation rechaza los datos con estado 400 y un mensaje claro; si llega un tipo incorrecto
  (letras o decimales en un campo entero, fechas mal escritas) se indica qué campo falló.
- Los tipos y longitudes coinciden con `01_schema_v3.sql`; las reglas que dependen de otros datos (cupos, cruces de
  horario, transiciones, capacidad) las valida la base de datos con sus triggers.

## 1. Base de datos (Oracle 12c o superior)

1. Crea (o usa) el usuario dueño del esquema. Por defecto el backend usa `LP2` / `admin123` en `localhost:1522/XE`.
   Necesita poder crear tablas, secuencias, vistas, paquetes y triggers (`CREATE PROCEDURE`, `CREATE TRIGGER`, `CREATE VIEW`).
2. Conectado como ese usuario (SQL Developer o SQL*Plus), ejecuta **en orden**:
   1. `kampu-backend/src/main/resources/db/00_reset.sql` — **solo si ya tenías la base de la versión anterior**
      (tablas `TRANSPORTE`/`CHOFERES`) o una instalación previa de la v3. **Borra todos los datos.**
   2. `kampu-backend/src/main/resources/db/01_schema_v3.sql` — tablas, índices, `PKG_ASIGNACION`, vistas y triggers.
      Al final lista los objetos: todos deben salir `VALID`.
   3. `kampu-backend/src/main/resources/db/02_extension_kampu.sql` — unicidad de teléfonos y documentos, rutas hacia
      paquetes, coordenadas y `PKG_RUTAS`.
   4. `kampu-backend/src/main/resources/db/03_data.sql` (opcional) — datos demo: 4 empresas, 14 lugares con coordenadas
      reales, 11 rutas (5 hacia paquetes) con duraciones reales, 8 vehículos, 7 conductores, 9 salidas, 8 reservas y
      9 operaciones de transporte con su historial.

   En SQL Developer usa **"Ejecutar script" (F5)**, no "Ejecutar sentencia" (el paquete y los triggers terminan en `/`).
   En SQL*Plus, para que las tildes se guarden bien: `set NLS_LANG=AMERICAN_AMERICA.AL32UTF8` antes de entrar.
3. Si no cargas `03_data.sql`, al iniciar el backend se crean solos los usuarios `admin` (ADMIN), `ventas` (VENDEDOR)
   y `operador` (OPERADOR) si la tabla está vacía.

> Desde la versión anterior (con `TRANSPORTE`/`CHOFERES`) se reinstala con
> `00_reset.sql` → `01_schema_v3.sql` → `02_extension_kampu.sql` → `03_data.sql`. Si necesitas conservar datos reales, expórtalos antes.

> `03_data.sql` usa IDs fijos (para que coincidan las relaciones) y al final reinicia las identidades con
> `ALTER TABLE ... MODIFY ... (START WITH LIMIT VALUE)`, para que los registros nuevos continúen desde el último ID.
> Las fechas de la demo son de octubre a diciembre de 2026: si ya pasaron, crea salidas nuevas para reservar.

### Correo (boleta informativa)

La boleta se envía sola al **confirmar una reserva** o al **aprobar un pago**, y se puede reenviar desde el detalle.
El correo sale por SMTP (Gmail por defecto) con credenciales en variables de entorno, para no subirlas a GitHub:

```bash
export MAIL_USERNAME=tu_correo@gmail.com
export MAIL_PASSWORD=tu_contraseña_de_aplicación   # Gmail → Seguridad → Contraseñas de aplicaciones
```

En IntelliJ: *Run → Edit Configurations → Environment variables*. Sin `MAIL_USERNAME` el envío queda desactivado
(la reserva funciona igual) y el botón "Ver" del detalle muestra la boleta en el navegador.

## 2. Backend (`kampu-backend`)

Conexión en `src/main/resources/application.yaml`. Puedes editarla ahí o usar variables de entorno:

| Variable      | Valor por defecto                        |
|---------------|------------------------------------------|
| `DB_URL`      | `jdbc:oracle:thin:@localhost:1522/XE`    |
| `DB_USERNAME` | `LP2`                                    |
| `DB_PASSWORD` | `admin123`                               |

Ejemplos de URL: Oracle XE 21c → `jdbc:oracle:thin:@localhost:1521/XEPDB1`; Oracle 23 Free → `jdbc:oracle:thin:@localhost:1521/FREEPDB1`.

```bash
cd kampu-backend
./mvnw spring-boot:run
```

(En Windows: `mvnw.cmd spring-boot:run`, o ejecutar `Lp2Gp2Application` desde IntelliJ.)

- Swagger: http://localhost:8082/lp2_gp2/swagger-ui/index.html .
- Imágenes de paquetes: se guardan en la carpeta `images/` (propiedad `storage.location`) y se sirven en `/lp2_gp2/images/...`.
- `spring.jpa.hibernate.ddl-auto` está en **`none`**: Hibernate no crea ni altera tablas; manda el script.

## 3. Frontend (`kampu-frontend`)

```bash
cd kampu-frontend
npm install
npm start
```

Abre http://localhost:4200 → usuario `admin` / `admin123` (o `ventas` / `ventas123`, `operador` / `operador123`).

`npm start` usa `proxy.config.json` (ya existía): `/api` → `http://localhost:8082/lp2_gp2/api` y
`/lp2_gp2/images` → `http://localhost:8082`. Por eso el frontend llama a rutas relativas y no hay problemas de CORS.
**Orden de arranque:** Oracle → backend → frontend.

## 4. API REST (todas bajo `/lp2_gp2/api/v1`, JSON con los mismos nombres que `core/models.ts`)

Errores: `{ "status": 400, "error": "mensaje", "timestamp": ... }`
(400 regla de negocio/validación/login incorrecto, 404 no existe, 409 duplicado).

| Recurso | Endpoints |
|---|---|
| Auth | `POST /auth/login` `{username,password}` → `usuario` |
| Usuarios | `GET /usuarios` (sin `password_hash`) |
| Paquetes | `GET/POST /paquetes` · `GET/PUT/DELETE /paquetes/{id}` · `POST /paquetes/imagen` (multipart `file`) → `{imagen}` |
| Guías | `GET/POST /guias` · `GET/PUT/DELETE /guias/{id}` |
| Empresas de transporte | `GET/POST /empresas` · `GET/PUT/DELETE /empresas/{id}` (incluye `totalVehiculos`, `capacidadTotalTransporte`, `conductoresDisponibles` de `VW_EMPRESA_CAPACIDAD`) |
| Lugares | `GET/POST /lugares` · `GET/PUT/DELETE /lugares/{id}` (con `latitud`/`longitud`) |
| Rutas | `GET/POST /rutas` · `GET/PUT/DELETE /rutas/{id}` · `GET /rutas/estimar-duracion?origenId=&destinoId=&tipoServicio=`. Con `paqueteId`, el POST pide `fechaSalida`, `cuposSalida` (y `guiaSalidaId` opcional), crea la salida y devuelve `salidaCreadaId`. `duracionEstimadaMin` vacía = se calcula. |
| Vehículos | `GET/POST /vehiculos` · `GET/PUT/DELETE /vehiculos/{id}` |
| Conductores | `GET/POST /conductores` · `GET/PUT/DELETE /conductores/{id}` |
| Operaciones (tráfico) | `GET /operaciones[?salidaId=]` · `GET /operaciones/estados` (estados y transiciones) · `GET/POST /operaciones` · `GET/PUT /operaciones/{id}` · `PATCH /operaciones/{id}/estado` `{estado, observacion}` · `POST /operaciones/{id}/asignar-automatico` · `PATCH /operaciones/{id}/asignar` `{vehiculoId, conductorId}` · `GET /operaciones/{id}/candidatos` · `GET /operaciones/{id}/recursos-disponibles` · `GET /operaciones/{id}/historial`. Encabezado opcional `X-Usuario`. |
| Clientes | `GET/POST /clientes` · `GET/PUT/DELETE /clientes/{id}` · `GET /clientes/buscar?q=` (autocompletado: nombre, documento o correo; máx. 8) |
| Salidas | `GET/POST /salidas` · `GET/PUT/DELETE /salidas/{id}` (sin `fechaFin` en el cuerpo: la calcula el trigger; devuelve `operaciones` y `faltanteTransporte`) |
| Reservas | `GET/POST /reservas` (POST incluye `salidaId`, `pasajeros[]` y `clienteId` **o** `contacto {nombre, email, telefono}` para invitados) · `GET /reservas/{id}/boleta` (HTML) · `POST /reservas/{id}/boleta` (envía por correo) · `GET /reservas/{id}` · `GET /reservas/{id}/pasajeros` · `PATCH /reservas/{id}/confirmar` · `PATCH /reservas/{id}/completar` · `PATCH /reservas/{id}/anular` `{formaReembolso, usuarioId}` (obligatorio) |
| Pasajeros | `GET /pasajeros[?reservaId=]` |
| Pagos | `GET /pagos[?reservaId=]` · `POST /pagos` · `PATCH /pagos/{id}/resolver` `{estado: APROBADO\|RECHAZADO}` (devuelven `tipoCambio` y `montoPen`) |

Reglas que aplica el **backend** (además de las de la base de datos, ver "Versión 3"):
`monto_pagado` = suma de `PAGOS.monto_pen` APROBADOS y `estado_pago` se deriva (PENDIENTE/PARCIAL/PAGADO);
un pago aprobado no puede exceder el saldo; anular con dinero pagado exige `forma_reembolso`, marca los pagos como
REEMBOLSADO y guarda `usuario_anula_id` (el usuario logueado que envía el frontend); no se reserva una salida que ya
inició; sin cruce de guía en fechas; documento único por reserva; fecha de nacimiento < hoy; vehículo y conductor se
asignan juntos; no se eliminan registros en uso (cliente con reservas, paquete/guía con salidas, salida con reservas u
operaciones, empresa con flota/rutas/operaciones, vehículo/conductor/ruta/lugar con operaciones).
La aplicación registra **una salida por reserva** (un `DETALLE_RESERVA`); el esquema admite varias.

## 5. Compatibilidad verificada

| Elemento | Frontend | Backend | Base de datos |
|---|---|---|---|
| Tablas y vistas | `core/models.ts` | 17 entidades `@Table` + 3 vistas `@Immutable` (validadas con `ddl-auto=validate`) | 17 tablas y 4 vistas de `01_schema_v3.sql` |
| Columnas | camelCase (`fechaInicio`) | `@Column(name="fecha_inicio")` | snake_case |
| Fechas `DATE` | `'yyyy-MM-dd'` | `LocalDate` | `DATE` |
| `TIMESTAMP` | string ISO | `LocalDateTime` | `TIMESTAMP` |
| Montos | `number` | `BigDecimal` (10,2) | `NUMBER(10,2)` |
| Estados / monedas / tipo doc. | tipos unión TS | `@Pattern` con los mismos valores | `CHECK ... IN (...)` y `DOMINIO_ESTADOS` |
| `DATE` con hora (operaciones) | `'yyyy-MM-ddTHH:mm'` | `LocalDateTime` | `DATE` |
| Longitudes | `maxlength` en los formularios | `@Size` | `VARCHAR2(n)` |
| Opcionales vacíos | `''` | `''` ↔ `NULL` (Oracle guarda `''` como NULL) | columnas sin `NOT NULL` |
| Tipos de cambio | `core/utils/moneda.ts` (`TASAS`) | constantes en `ConversorMoneda.java` | — (deben coincidir) |

## 6. Hallazgos y cómo se resolvieron

0. **v3 (este cambio):** ver la sección "Versión 3" (ajuste de SQL*Plus en `01_schema_v3.sql`, roles `VENDEDOR`/`OPERADOR`,
   `fecha_fin` calculada por trigger). El `application.yaml` ahora sí lee `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`
   (la tabla de la sección 2 ya los mencionaba pero el archivo tenía los valores fijos).

Historial de versiones anteriores:

1. **El ZIP del backend era otra aplicación.** Contenía una plantilla de e-commerce (`producto`, `pedidos`, `persona`,
   `usuario`) sin relación con el script ni con el frontend, y con `ddl-auto: update` (habría creado esas tablas en tu esquema).
   Se reemplazó por el backend del sistema de tours, conservando el proyecto `lp2_gp2`: paquete `pe.edu.upeu.lp2_gp2`,
   estructura `controller/services/repository/entity/dto/exception/config`, puerto 8082, context-path `/lp2_gp2`,
   Swagger, CORS y el servicio de imágenes. El ZIP también traía el frontend antiguo de e-commerce; aquí se usa el nuevo.
2. **El script no se ejecuta en Oracle tal cual.** `CONSTRAINT chk_pas_fecha_nac CHECK (fecha_nacimiento < SYSDATE)`
   produce **ORA-02436** (Oracle no permite `SYSDATE` en un CHECK). Era el único cambio en el `01_schema.sql` anterior: esa línea quedaba
   comentada y la regla se aplicaba con el trigger `TRG_PAS_FECHA_NAC`. En la v3 ese trigger no existe: la valida el backend.
3. **El frontend no tenía backend** (guardaba todo en `localStorage`). Cambios en el frontend, sin tocar HTML ni CSS:
   - `TourStore` ahora llama a la API (signals con los datos; las escrituras devuelven el `Observable` de `HttpClient`).
   - `AuthService` hace login contra el backend y guarda el usuario en `localStorage`.
   - `ImagenService` sube la imagen al backend (antes la guardaba en `localStorage`).
   - Componentes: `subscribe({ next, error })` en guardar/eliminar, carga inicial en el shell y recarga directa de `/reservas/:id`.
   - Eliminados `core/data/seed.ts` (pasó a `02_data.sql`), `local-store.ts` y `hash.ts`.
4. **Contraseñas:** se guardan en texto plano en `password_hash` y se comparan en el backend (todavía no se ve el tema
   de seguridad: sin interceptor, token ni BCrypt).

## 7. Pendientes / decisiones a confirmar

- **Roles:** `ADMIN`, `VENDEDOR` y `OPERADOR` tienen hoy los mismos permisos (el frontend no los diferencia). Si se
  necesita, se puede restringir por rol (por ejemplo, que solo `OPERADOR`/`ADMIN` cambien estados de tráfico).
- **Parámetros de asignación:** el tope de 10 h de conducción por día y el margen de 30 min entre servicios están en las
  constantes `c_max_horas_dia` y `c_margen_min` de `PKG_ASIGNACION` (ajústalas a la norma MTC).
- **Detalle múltiple:** el esquema permite varias salidas por reserva; la pantalla de nueva reserva crea una sola.
- **Tipos de cambio fijos:** `TASAS` (frontend) y `ConversorMoneda` (backend) deben mantenerse iguales.
- **Producción:** `ng build` genera archivos estáticos que llaman a `/api/v1` y `/lp2_gp2/images` en el mismo origen;
  hay que servirlos detrás de un proxy (Nginx/Apache) que redirija esas rutas al backend.
- La hora de `fecha_reserva` / `fecha_pago` es la del servidor donde corre el backend.
