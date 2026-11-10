-- =====================================================================
-- KAMPU / TOUR MANAGER v3 - Datos de demostración
-- Ejecutar DESPUÉS de 01_schema_v3.sql, como script (F5), con el usuario dueño.
--
-- Los datos pasan por los triggers de la v3, por eso el orden importa:
--   * Las salidas se crean PROGRAMADAS (fecha_fin y cupos_disponibles los
--     calcula trg_salida_biu) y se confirman/completan al final, cuando ya
--     tienen reservas y transporte (trg_salida_confirmar lo verifica).
--   * Las reservas se crean PENDIENTES; DETALLE_RESERVA descuenta cupos y
--     calcula monto_total (trg_detalle_reserva_biud) y luego se cambia el
--     estado (la anulación devuelve los cupos: trg_reserva_estado_bu).
--   * Las operaciones de transporte reciben su código KMP-### y su historial
--     de estados desde trg_operacion_transporte.
-- =====================================================================

-- Quien registra el historial de tráfico (USERENV.CLIENT_IDENTIFIER)
EXEC DBMS_SESSION.SET_IDENTIFIER('operador');

-- ---------------------------------------------------------------------
-- 1. Maestras comerciales
-- ---------------------------------------------------------------------
INSERT INTO USUARIOS_SISTEMA (usuario_id, username, password_hash, rol, estado) VALUES (1, 'admin', 'admin123', 'ADMIN', 'ACTIVO');
INSERT INTO USUARIOS_SISTEMA (usuario_id, username, password_hash, rol, estado) VALUES (2, 'ventas', 'ventas123', 'VENDEDOR', 'ACTIVO');
INSERT INTO USUARIOS_SISTEMA (usuario_id, username, password_hash, rol, estado) VALUES (3, 'operador', 'operador123', 'OPERADOR', 'ACTIVO');

INSERT INTO CLIENTES (cliente_id, nombre_completo, tipo_documento, numero_documento, email, telefono) VALUES (1, 'Ana Ramírez Soto', 'DNI', '45871236', 'ana.ramirez@mail.com', '+51 987654321');
INSERT INTO CLIENTES (cliente_id, nombre_completo, tipo_documento, numero_documento, email, telefono) VALUES (2, 'Lucía Fernández Cruz', 'DNI', '71452368', 'lucia.fernandez@mail.com', '+51 956874123');
INSERT INTO CLIENTES (cliente_id, nombre_completo, tipo_documento, numero_documento, email, telefono) VALUES (3, 'John Miller', 'PASAPORTE', 'X4587126', 'john.miller@mail.com', '+1 3055550142');
INSERT INTO CLIENTES (cliente_id, nombre_completo, tipo_documento, numero_documento, email, telefono) VALUES (4, 'Sofía Castillo Rojas', 'CE', '001245789', 'sofia.castillo@mail.com', '+51 945123789');
INSERT INTO CLIENTES (cliente_id, nombre_completo, tipo_documento, numero_documento, email, telefono) VALUES (5, 'Diego Salazar Luna', 'DNI', '43698521', 'diego.salazar@mail.com', '+51 965478123');
INSERT INTO CLIENTES (cliente_id, nombre_completo, tipo_documento, numero_documento, email, telefono) VALUES (6, 'Pedro Huertas Vela', 'DNI', '42587136', 'pedro.huertas@mail.com', '+51 934521876');
INSERT INTO CLIENTES (cliente_id, nombre_completo, tipo_documento, numero_documento, email, telefono) VALUES (7, 'Luis Paredes Gil', 'DNI', '47125836', 'luis.paredes@mail.com', '+51 912008745');
INSERT INTO CLIENTES (cliente_id, nombre_completo, tipo_documento, numero_documento, email, telefono) VALUES (8, 'Andrea Campos Ruiz', 'DNI', '70418529', 'andrea.campos@mail.com', '+51 999120334');
INSERT INTO CLIENTES (cliente_id, nombre_completo, tipo_documento, numero_documento, email, telefono) VALUES (9, 'Viajes Andinos SAC', 'RUC', '20556677889', 'reservas@viajesandinos.pe', '+51 014445566');

INSERT INTO GUIAS (guia_id, nombre_completo, especialidad, telefono) VALUES (1, 'Carlos Quispe', 'Cultura e historia andina', '984112233');
INSERT INTO GUIAS (guia_id, nombre_completo, especialidad, telefono) VALUES (2, 'María Torres', 'Gastronomía y city tours', '991445120');
INSERT INTO GUIAS (guia_id, nombre_completo, especialidad, telefono) VALUES (3, 'Luis Valdivia', 'Aventura y trekking', '958302771');
INSERT INTO GUIAS (guia_id, nombre_completo, especialidad, telefono) VALUES (4, 'Rosa Huamán', 'Naturaleza y selva', '976880415');

INSERT INTO PAQUETES_TOUR (paquete_id, nombre, descripcion, precio_base, dias, noches, imagen, estado) VALUES (1, 'Machu Picchu Clásico', 'Valle Sagrado, tren a Aguas Calientes y visita guiada a la ciudadela.', 1850, 4, 3, NULL, 'ACTIVO');
INSERT INTO PAQUETES_TOUR (paquete_id, nombre, descripcion, precio_base, dias, noches, imagen, estado) VALUES (2, 'Lima Gastronómica', 'Centro histórico, mercados, ceviche y degustación en Barranco.', 420, 2, 1, NULL, 'ACTIVO');
INSERT INTO PAQUETES_TOUR (paquete_id, nombre, descripcion, precio_base, dias, noches, imagen, estado) VALUES (3, 'Arequipa y Cañón del Colca', 'Ciudad blanca, Cruz del Cóndor y termas de Chivay.', 980, 3, 2, NULL, 'ACTIVO');
INSERT INTO PAQUETES_TOUR (paquete_id, nombre, descripcion, precio_base, dias, noches, imagen, estado) VALUES (4, 'Paracas e Ica Aventura', 'Islas Ballestas, reserva de Paracas, Huacachina y sandboard.', 380, 2, 1, NULL, 'ACTIVO');
INSERT INTO PAQUETES_TOUR (paquete_id, nombre, descripcion, precio_base, dias, noches, imagen, estado) VALUES (5, 'Amazonía Tambopata', 'Lodge en la selva, lago Sandoval y avistamiento de fauna.', 1650, 4, 3, NULL, 'ACTIVO');
INSERT INTO PAQUETES_TOUR (paquete_id, nombre, descripcion, precio_base, dias, noches, imagen, estado) VALUES (6, 'Lago Titicaca y Puno', 'Islas flotantes de los Uros, Taquile y Sillustani.', 890, 3, 2, NULL, 'INACTIVO');

-- ---------------------------------------------------------------------
-- 2. Proveedores de transporte
-- ---------------------------------------------------------------------
INSERT INTO EMPRESA_TRANSPORTE (empresa_id, nombre_comercial, ruc, contacto_nombre, contacto_telefono, contacto_email, direccion, estado_afiliacion, disponibilidad_operativa, fecha_afiliacion)
VALUES (1, 'Transportes Inka Express SAC', '20512345678', 'Julia Ccahuana', '+51 984100200', 'operaciones@inkaexpress.pe', 'Av. El Sol 820, Cusco', 'ACTIVA', 'DISPONIBLE', DATE '2025-03-01');
INSERT INTO EMPRESA_TRANSPORTE (empresa_id, nombre_comercial, ruc, contacto_nombre, contacto_telefono, contacto_email, direccion, estado_afiliacion, disponibilidad_operativa, fecha_afiliacion)
VALUES (2, 'Lima Tours Movilidad SAC', '20601234567', 'Óscar Ríos', '+51 014223344', 'flota@limatoursmov.pe', 'Av. Larco 1150, Miraflores', 'ACTIVA', 'DISPONIBLE', DATE '2025-05-15');
INSERT INTO EMPRESA_TRANSPORTE (empresa_id, nombre_comercial, ruc, contacto_nombre, contacto_telefono, contacto_email, direccion, estado_afiliacion, disponibilidad_operativa, fecha_afiliacion)
VALUES (3, 'Colca Bus EIRL', '20498765432', 'Hernán Apaza', '+51 959887766', 'colcabus@mail.com', 'Calle Mercaderes 310, Arequipa', 'ACTIVA', 'LIMITADA', DATE '2025-08-20');
INSERT INTO EMPRESA_TRANSPORTE (empresa_id, nombre_comercial, ruc, contacto_nombre, contacto_telefono, contacto_email, direccion, estado_afiliacion, disponibilidad_operativa, fecha_afiliacion)
VALUES (4, 'Selva Trans SRL', '20187654321', 'Raúl Shapiama', '+51 982554433', NULL, 'Jr. Loreto 145, Puerto Maldonado', 'PENDIENTE', 'DISPONIBLE', DATE '2026-10-01');

INSERT INTO LUGARES (lugar_id, nombre, tipo_lugar, ciudad) VALUES (1, 'Aeropuerto Alejandro Velasco Astete', 'AEROPUERTO', 'Cusco');
INSERT INTO LUGARES (lugar_id, nombre, tipo_lugar, ciudad) VALUES (2, 'Hotel Plaza de Armas', 'HOTEL', 'Cusco');
INSERT INTO LUGARES (lugar_id, nombre, tipo_lugar, ciudad) VALUES (3, 'Estación Ollantaytambo', 'TERMINAL', 'Ollantaytambo');
INSERT INTO LUGARES (lugar_id, nombre, tipo_lugar, ciudad) VALUES (4, 'Aeropuerto Jorge Chávez', 'AEROPUERTO', 'Lima');
INSERT INTO LUGARES (lugar_id, nombre, tipo_lugar, ciudad) VALUES (5, 'Hotel Miraflores Park', 'HOTEL', 'Lima');
INSERT INTO LUGARES (lugar_id, nombre, tipo_lugar, ciudad) VALUES (6, 'Barranco', 'ATRACTIVO', 'Lima');
INSERT INTO LUGARES (lugar_id, nombre, tipo_lugar, ciudad) VALUES (7, 'Aeropuerto Rodríguez Ballón', 'AEROPUERTO', 'Arequipa');
INSERT INTO LUGARES (lugar_id, nombre, tipo_lugar, ciudad) VALUES (8, 'Hotel Casona Arequipa', 'HOTEL', 'Arequipa');
INSERT INTO LUGARES (lugar_id, nombre, tipo_lugar, ciudad) VALUES (9, 'Chivay', 'CIUDAD', 'Caylloma');
INSERT INTO LUGARES (lugar_id, nombre, tipo_lugar, ciudad) VALUES (10, 'Hotel Paracas Bay', 'HOTEL', 'Paracas');
INSERT INTO LUGARES (lugar_id, nombre, tipo_lugar, ciudad) VALUES (11, 'Oasis de Huacachina', 'ATRACTIVO', 'Ica');
INSERT INTO LUGARES (lugar_id, nombre, tipo_lugar, ciudad) VALUES (12, 'Aeropuerto Padre Aldamiz', 'AEROPUERTO', 'Puerto Maldonado');
INSERT INTO LUGARES (lugar_id, nombre, tipo_lugar, ciudad) VALUES (13, 'Tambopata Eco Lodge', 'HOTEL', 'Puerto Maldonado');

-- Rutas que opera cada empresa (duración en minutos, tarifa en soles)
INSERT INTO RUTAS_PROVEEDOR (ruta_id, empresa_id, origen_id, destino_id, tipo_servicio, duracion_estimada_min, tarifa_referencial, estado) VALUES (1, 1, 1, 2, 'TRASLADO', 25, 60, 'ACTIVA');
INSERT INTO RUTAS_PROVEEDOR (ruta_id, empresa_id, origen_id, destino_id, tipo_servicio, duracion_estimada_min, tarifa_referencial, estado) VALUES (2, 1, 2, 3, 'TOUR', 120, 250, 'ACTIVA');
INSERT INTO RUTAS_PROVEEDOR (ruta_id, empresa_id, origen_id, destino_id, tipo_servicio, duracion_estimada_min, tarifa_referencial, estado) VALUES (3, 1, 3, 2, 'TOUR', 120, 250, 'ACTIVA');
INSERT INTO RUTAS_PROVEEDOR (ruta_id, empresa_id, origen_id, destino_id, tipo_servicio, duracion_estimada_min, tarifa_referencial, estado) VALUES (4, 1, 2, 1, 'TRASLADO', 25, 60, 'ACTIVA');
INSERT INTO RUTAS_PROVEEDOR (ruta_id, empresa_id, origen_id, destino_id, tipo_servicio, duracion_estimada_min, tarifa_referencial, estado) VALUES (5, 2, 4, 5, 'TRASLADO', 45, 90, 'ACTIVA');
INSERT INTO RUTAS_PROVEEDOR (ruta_id, empresa_id, origen_id, destino_id, tipo_servicio, duracion_estimada_min, tarifa_referencial, estado) VALUES (6, 2, 5, 6, 'TOUR', 20, 50, 'ACTIVA');
INSERT INTO RUTAS_PROVEEDOR (ruta_id, empresa_id, origen_id, destino_id, tipo_servicio, duracion_estimada_min, tarifa_referencial, estado) VALUES (7, 2, 5, 10, 'INTERPROVINCIAL', 240, 450, 'ACTIVA');
INSERT INTO RUTAS_PROVEEDOR (ruta_id, empresa_id, origen_id, destino_id, tipo_servicio, duracion_estimada_min, tarifa_referencial, estado) VALUES (8, 2, 10, 11, 'TOUR', 90, 200, 'ACTIVA');
INSERT INTO RUTAS_PROVEEDOR (ruta_id, empresa_id, origen_id, destino_id, tipo_servicio, duracion_estimada_min, tarifa_referencial, estado) VALUES (9, 3, 7, 8, 'TRASLADO', 20, 50, 'ACTIVA');
INSERT INTO RUTAS_PROVEEDOR (ruta_id, empresa_id, origen_id, destino_id, tipo_servicio, duracion_estimada_min, tarifa_referencial, estado) VALUES (10, 3, 8, 9, 'INTERPROVINCIAL', 180, 400, 'ACTIVA');
INSERT INTO RUTAS_PROVEEDOR (ruta_id, empresa_id, origen_id, destino_id, tipo_servicio, duracion_estimada_min, tarifa_referencial, estado) VALUES (11, 4, 12, 13, 'TRASLADO', 90, 180, 'ACTIVA');

INSERT INTO VEHICULO (vehiculo_id, empresa_id, placa, tipo_vehiculo, marca_modelo, capacidad_pax, fecha_prox_mantenimiento, estado_operativo) VALUES (1, 1, 'BUS-401', 'BUS', 'Mercedes-Benz O500', 40, DATE '2027-06-30', 'OPERATIVO');
INSERT INTO VEHICULO (vehiculo_id, empresa_id, placa, tipo_vehiculo, marca_modelo, capacidad_pax, fecha_prox_mantenimiento, estado_operativo) VALUES (2, 1, 'VAN-152', 'VAN', 'Toyota Hiace', 15, DATE '2027-03-15', 'OPERATIVO');
INSERT INTO VEHICULO (vehiculo_id, empresa_id, placa, tipo_vehiculo, marca_modelo, capacidad_pax, fecha_prox_mantenimiento, estado_operativo) VALUES (3, 1, 'CAM-084', 'CAMIONETA_4X4', 'Toyota Hilux 4x4', 4, DATE '2027-01-20', 'OPERATIVO');
INSERT INTO VEHICULO (vehiculo_id, empresa_id, placa, tipo_vehiculo, marca_modelo, capacidad_pax, fecha_prox_mantenimiento, estado_operativo) VALUES (4, 2, 'BUS-302', 'BUS', 'Volvo B7R', 30, DATE '2027-05-01', 'OPERATIVO');
INSERT INTO VEHICULO (vehiculo_id, empresa_id, placa, tipo_vehiculo, marca_modelo, capacidad_pax, fecha_prox_mantenimiento, estado_operativo) VALUES (5, 2, 'VAN-210', 'VAN', 'Hyundai H1', 10, DATE '2026-11-20', 'OPERATIVO');
INSERT INTO VEHICULO (vehiculo_id, empresa_id, placa, tipo_vehiculo, marca_modelo, capacidad_pax, fecha_prox_mantenimiento, estado_operativo) VALUES (6, 3, 'MIN-077', 'MINIBUS', 'Mercedes-Benz Sprinter', 20, DATE '2027-02-01', 'OPERATIVO');
INSERT INTO VEHICULO (vehiculo_id, empresa_id, placa, tipo_vehiculo, marca_modelo, capacidad_pax, fecha_prox_mantenimiento, estado_operativo) VALUES (7, 3, 'BUS-509', 'BUS', 'Scania K360', 45, DATE '2026-12-15', 'EN_MANTENIMIENTO');
INSERT INTO VEHICULO (vehiculo_id, empresa_id, placa, tipo_vehiculo, marca_modelo, capacidad_pax, fecha_prox_mantenimiento, estado_operativo) VALUES (8, 4, 'VAN-330', 'VAN', 'Toyota Hiace', 12, DATE '2027-04-10', 'OPERATIVO');

INSERT INTO CONDUCTOR (conductor_id, empresa_id, nombres, numero_licencia, categoria_licencia, fecha_venc_licencia, telefono, estado_disponibilidad) VALUES (1, 1, 'Julio Quispe Mamani', 'Q45871236', 'A-IIIc', DATE '2028-05-01', '+51 984112233', 'DISPONIBLE');
INSERT INTO CONDUCTOR (conductor_id, empresa_id, nombres, numero_licencia, categoria_licencia, fecha_venc_licencia, telefono, estado_disponibilidad) VALUES (2, 1, 'Rosa Huamán Ccori', 'H71452368', 'A-IIb', DATE '2027-09-10', '+51 984223344', 'DISPONIBLE');
INSERT INTO CONDUCTOR (conductor_id, empresa_id, nombres, numero_licencia, categoria_licencia, fecha_venc_licencia, telefono, estado_disponibilidad) VALUES (3, 1, 'Wilber Tito Yupanqui', 'T47125836', 'A-IIIc', DATE '2027-08-01', '+51 984556677', 'DISPONIBLE');
INSERT INTO CONDUCTOR (conductor_id, empresa_id, nombres, numero_licencia, categoria_licencia, fecha_venc_licencia, telefono, estado_disponibilidad) VALUES (4, 2, 'Marco Condori Apaza', 'C43698521', 'A-IIIb', DATE '2028-01-15', '+51 984334455', 'DISPONIBLE');
INSERT INTO CONDUCTOR (conductor_id, empresa_id, nombres, numero_licencia, categoria_licencia, fecha_venc_licencia, telefono, estado_disponibilidad) VALUES (5, 2, 'Elmer Choque Puma', 'C42587136', 'A-IIb', DATE '2027-07-20', '+51 984445566', 'DESCANSO');
INSERT INTO CONDUCTOR (conductor_id, empresa_id, nombres, numero_licencia, categoria_licencia, fecha_venc_licencia, telefono, estado_disponibilidad) VALUES (6, 3, 'Lucio Mamani Flores', 'M40125879', 'A-IIIb', DATE '2027-11-30', '+51 959112233', 'DISPONIBLE');
INSERT INTO CONDUCTOR (conductor_id, empresa_id, nombres, numero_licencia, categoria_licencia, fecha_venc_licencia, telefono, estado_disponibilidad) VALUES (7, 4, 'Raúl Shapiama Tuesta', 'S46325871', 'A-IIb', DATE '2028-03-03', '+51 982554433', 'DISPONIBLE');

-- ---------------------------------------------------------------------
-- 3. Salidas (fecha_fin NULL: la calcula el trigger con los días del paquete)
-- ---------------------------------------------------------------------
INSERT INTO SALIDAS_PROGRAMADAS (salida_id, paquete_id, fecha_inicio, fecha_fin, cupos_maximos, cupos_disponibles, guia_id, estado) VALUES (1, 1, DATE '2026-11-05', NULL, 20, 20, 1, 'PROGRAMADA');
INSERT INTO SALIDAS_PROGRAMADAS (salida_id, paquete_id, fecha_inicio, fecha_fin, cupos_maximos, cupos_disponibles, guia_id, estado) VALUES (2, 1, DATE '2026-12-10', NULL, 20, 20, 1, 'PROGRAMADA');
INSERT INTO SALIDAS_PROGRAMADAS (salida_id, paquete_id, fecha_inicio, fecha_fin, cupos_maximos, cupos_disponibles, guia_id, estado) VALUES (3, 2, DATE '2026-11-14', NULL, 15, 15, 2, 'PROGRAMADA');
INSERT INTO SALIDAS_PROGRAMADAS (salida_id, paquete_id, fecha_inicio, fecha_fin, cupos_maximos, cupos_disponibles, guia_id, estado) VALUES (4, 3, DATE '2026-11-20', NULL, 16, 16, 3, 'PROGRAMADA');
INSERT INTO SALIDAS_PROGRAMADAS (salida_id, paquete_id, fecha_inicio, fecha_fin, cupos_maximos, cupos_disponibles, guia_id, estado) VALUES (5, 4, DATE '2026-11-07', NULL, 25, 25, 2, 'PROGRAMADA');
INSERT INTO SALIDAS_PROGRAMADAS (salida_id, paquete_id, fecha_inicio, fecha_fin, cupos_maximos, cupos_disponibles, guia_id, estado) VALUES (6, 5, DATE '2026-12-03', NULL, 12, 12, 4, 'PROGRAMADA');
INSERT INTO SALIDAS_PROGRAMADAS (salida_id, paquete_id, fecha_inicio, fecha_fin, cupos_maximos, cupos_disponibles, guia_id, estado) VALUES (7, 1, DATE '2026-10-08', NULL, 20, 20, 1, 'PROGRAMADA');
INSERT INTO SALIDAS_PROGRAMADAS (salida_id, paquete_id, fecha_inicio, fecha_fin, cupos_maximos, cupos_disponibles, guia_id, estado) VALUES (8, 3, DATE '2026-10-21', NULL, 16, 16, 3, 'PROGRAMADA');
INSERT INTO SALIDAS_PROGRAMADAS (salida_id, paquete_id, fecha_inicio, fecha_fin, cupos_maximos, cupos_disponibles, guia_id, estado) VALUES (9, 6, DATE '2027-01-15', NULL, 18, 18, 4, 'PROGRAMADA');

-- ---------------------------------------------------------------------
-- 4. Reservas: cabecera PENDIENTE + detalle (cupos y monto_total por trigger)
--    + pasajeros; al final se fija el estado de cada reserva
-- ---------------------------------------------------------------------
INSERT INTO RESERVAS (reserva_id, cliente_id, fecha_reserva, observaciones) VALUES (1, 1, TIMESTAMP '2026-09-12 15:30:00', NULL);
INSERT INTO RESERVAS (reserva_id, cliente_id, fecha_reserva, observaciones) VALUES (2, 2, TIMESTAMP '2026-09-30 10:05:00', 'Un menor de edad en el grupo.');
INSERT INTO RESERVAS (reserva_id, cliente_id, fecha_reserva, observaciones) VALUES (3, 3, TIMESTAMP '2026-10-02 18:40:00', 'Habitación matrimonial.');
INSERT INTO RESERVAS (reserva_id, cliente_id, fecha_reserva, observaciones) VALUES (4, 4, TIMESTAMP '2026-10-15 09:12:00', NULL);
INSERT INTO RESERVAS (reserva_id, cliente_id, fecha_reserva, observaciones) VALUES (5, 5, TIMESTAMP '2026-10-10 14:20:00', NULL);
INSERT INTO RESERVAS (reserva_id, cliente_id, fecha_reserva, observaciones) VALUES (6, 6, TIMESTAMP '2026-10-18 16:55:00', 'Dieta vegetariana.');
INSERT INTO RESERVAS (reserva_id, cliente_id, fecha_reserva, observaciones) VALUES (7, 7, TIMESTAMP '2026-10-05 11:00:00', 'Anulada a pedido del cliente.');
-- Reserva de un invitado (sin cliente registrado)
INSERT INTO RESERVAS (reserva_id, cliente_id, fecha_reserva, observaciones, contacto_nombre, contacto_email, contacto_telefono)
VALUES (8, NULL, TIMESTAMP '2026-10-25 12:15:00', NULL, 'Grupo Ruiz', 'grupo.ruiz@mail.com', '+51 987001122');

-- precio_unitario NULL: el trigger copia PAQUETES_TOUR.precio_base
INSERT INTO DETALLE_RESERVA (detalle_id, reserva_id, salida_id, cantidad, precio_unitario, descuento) VALUES (1, 1, 7, 2, NULL, 0);
INSERT INTO DETALLE_RESERVA (detalle_id, reserva_id, salida_id, cantidad, precio_unitario, descuento) VALUES (2, 2, 8, 3, NULL, 0);
INSERT INTO DETALLE_RESERVA (detalle_id, reserva_id, salida_id, cantidad, precio_unitario, descuento) VALUES (3, 3, 1, 2, NULL, 0);
INSERT INTO DETALLE_RESERVA (detalle_id, reserva_id, salida_id, cantidad, precio_unitario, descuento) VALUES (4, 4, 1, 1, NULL, 0);
INSERT INTO DETALLE_RESERVA (detalle_id, reserva_id, salida_id, cantidad, precio_unitario, descuento) VALUES (5, 5, 3, 3, NULL, 0);
INSERT INTO DETALLE_RESERVA (detalle_id, reserva_id, salida_id, cantidad, precio_unitario, descuento) VALUES (6, 6, 6, 2, NULL, 0);
INSERT INTO DETALLE_RESERVA (detalle_id, reserva_id, salida_id, cantidad, precio_unitario, descuento) VALUES (7, 7, 4, 1, NULL, 0);
INSERT INTO DETALLE_RESERVA (detalle_id, reserva_id, salida_id, cantidad, precio_unitario, descuento) VALUES (8, 8, 5, 4, NULL, 0);

INSERT INTO PASAJEROS_RESERVA (pasajero_id, detalle_id, nombre_completo, tipo_documento, numero_documento, nacionalidad, fecha_nacimiento) VALUES (1, 1, 'Ana Ramírez Soto', 'DNI', '45871236', 'Peruana', DATE '1990-04-12');
INSERT INTO PASAJEROS_RESERVA (pasajero_id, detalle_id, nombre_completo, tipo_documento, numero_documento, nacionalidad, fecha_nacimiento) VALUES (2, 1, 'Jorge Mendoza Paredes', 'DNI', '40125879', 'Peruana', DATE '1985-09-30');
INSERT INTO PASAJEROS_RESERVA (pasajero_id, detalle_id, nombre_completo, tipo_documento, numero_documento, nacionalidad, fecha_nacimiento) VALUES (3, 2, 'Lucía Fernández Cruz', 'DNI', '71452368', 'Peruana', DATE '1995-01-22');
INSERT INTO PASAJEROS_RESERVA (pasajero_id, detalle_id, nombre_completo, tipo_documento, numero_documento, nacionalidad, fecha_nacimiento) VALUES (4, 2, 'Mateo Fernández Cruz', 'DNI', '71452369', 'Peruana', DATE '2012-06-15');
INSERT INTO PASAJEROS_RESERVA (pasajero_id, detalle_id, nombre_completo, tipo_documento, numero_documento, nacionalidad, fecha_nacimiento) VALUES (5, 2, 'Carla Vega Ruiz', 'DNI', '46325871', 'Peruana', DATE '1992-11-03');
INSERT INTO PASAJEROS_RESERVA (pasajero_id, detalle_id, nombre_completo, tipo_documento, numero_documento, nacionalidad, fecha_nacimiento) VALUES (6, 3, 'John Miller', 'PASAPORTE', 'X4587126', 'Estadounidense', DATE '1980-02-18');
INSERT INTO PASAJEROS_RESERVA (pasajero_id, detalle_id, nombre_completo, tipo_documento, numero_documento, nacionalidad, fecha_nacimiento) VALUES (7, 3, 'Emily Miller', 'PASAPORTE', 'X4587127', 'Estadounidense', DATE '1983-07-09');
INSERT INTO PASAJEROS_RESERVA (pasajero_id, detalle_id, nombre_completo, tipo_documento, numero_documento, nacionalidad, fecha_nacimiento) VALUES (8, 4, 'Sofía Castillo Rojas', 'CE', '001245789', 'Colombiana', DATE '1998-12-25');
INSERT INTO PASAJEROS_RESERVA (pasajero_id, detalle_id, nombre_completo, tipo_documento, numero_documento, nacionalidad, fecha_nacimiento) VALUES (9, 5, 'Diego Salazar Luna', 'DNI', '43698521', 'Peruana', DATE '1988-03-07');
INSERT INTO PASAJEROS_RESERVA (pasajero_id, detalle_id, nombre_completo, tipo_documento, numero_documento, nacionalidad, fecha_nacimiento) VALUES (10, 5, 'Valeria Salazar Luna', 'DNI', '43698522', 'Peruana', DATE '1991-08-19');
INSERT INTO PASAJEROS_RESERVA (pasajero_id, detalle_id, nombre_completo, tipo_documento, numero_documento, nacionalidad, fecha_nacimiento) VALUES (11, 5, 'Camila Salazar Luna', 'DNI', '43698523', 'Peruana', DATE '2015-02-11');
INSERT INTO PASAJEROS_RESERVA (pasajero_id, detalle_id, nombre_completo, tipo_documento, numero_documento, nacionalidad, fecha_nacimiento) VALUES (12, 6, 'Pedro Huertas Vela', 'DNI', '42587136', 'Peruana', DATE '1979-05-30');
INSERT INTO PASAJEROS_RESERVA (pasajero_id, detalle_id, nombre_completo, tipo_documento, numero_documento, nacionalidad, fecha_nacimiento) VALUES (13, 6, 'Marta Huertas Vela', 'DNI', '42587137', 'Peruana', DATE '1982-10-14');
INSERT INTO PASAJEROS_RESERVA (pasajero_id, detalle_id, nombre_completo, tipo_documento, numero_documento, nacionalidad, fecha_nacimiento) VALUES (14, 7, 'Luis Paredes Gil', 'DNI', '47125836', 'Peruana', DATE '1986-01-09');
INSERT INTO PASAJEROS_RESERVA (pasajero_id, detalle_id, nombre_completo, tipo_documento, numero_documento, nacionalidad, fecha_nacimiento) VALUES (15, 8, 'Martín Ruiz Peña', 'DNI', '41258963', 'Peruana', DATE '1978-03-21');
INSERT INTO PASAJEROS_RESERVA (pasajero_id, detalle_id, nombre_completo, tipo_documento, numero_documento, nacionalidad, fecha_nacimiento) VALUES (16, 8, 'Carmen Ruiz Peña', 'DNI', '41258964', 'Peruana', DATE '1981-07-02');

-- Pagos (tipo_cambio = soles por unidad; monto_pen es columna virtual)
INSERT INTO PAGOS (pago_id, reserva_id, monto, moneda, tipo_cambio, metodo_pago, codigo_transaccion, fecha_pago, estado_transaccion) VALUES (1, 1, 1850, 'PEN', 1, 'Transferencia', 'OP-883421', TIMESTAMP '2026-09-12 15:45:00', 'APROBADO');
INSERT INTO PAGOS (pago_id, reserva_id, monto, moneda, tipo_cambio, metodo_pago, codigo_transaccion, fecha_pago, estado_transaccion) VALUES (2, 1, 1850, 'PEN', 1, 'Tarjeta', 'VISA-1127', TIMESTAMP '2026-09-28 12:10:00', 'APROBADO');
INSERT INTO PAGOS (pago_id, reserva_id, monto, moneda, tipo_cambio, metodo_pago, codigo_transaccion, fecha_pago, estado_transaccion) VALUES (3, 2, 1470, 'PEN', 1, 'Yape', 'YP-55120', TIMESTAMP '2026-09-30 10:20:00', 'APROBADO');
INSERT INTO PAGOS (pago_id, reserva_id, monto, moneda, tipo_cambio, metodo_pago, codigo_transaccion, fecha_pago, estado_transaccion) VALUES (4, 2, 1470, 'PEN', 1, 'Efectivo', 'EF-00012', TIMESTAMP '2026-10-12 09:00:00', 'APROBADO');
INSERT INTO PAGOS (pago_id, reserva_id, monto, moneda, tipo_cambio, metodo_pago, codigo_transaccion, fecha_pago, estado_transaccion) VALUES (5, 3, 1850, 'PEN', 1, 'Tarjeta', 'MC-9034', TIMESTAMP '2026-10-02 18:50:00', 'APROBADO');
INSERT INTO PAGOS (pago_id, reserva_id, monto, moneda, tipo_cambio, metodo_pago, codigo_transaccion, fecha_pago, estado_transaccion) VALUES (6, 4, 300, 'PEN', 1, 'Tarjeta', 'VISA-7781', TIMESTAMP '2026-10-15 09:20:00', 'RECHAZADO');
INSERT INTO PAGOS (pago_id, reserva_id, monto, moneda, tipo_cambio, metodo_pago, codigo_transaccion, fecha_pago, estado_transaccion) VALUES (7, 4, 925, 'PEN', 1, 'Transferencia', 'OP-901122', TIMESTAMP '2026-10-21 17:30:00', 'PENDIENTE');
INSERT INTO PAGOS (pago_id, reserva_id, monto, moneda, tipo_cambio, metodo_pago, codigo_transaccion, fecha_pago, estado_transaccion) VALUES (8, 5, 630, 'PEN', 1, 'Efectivo', 'EF-00013', TIMESTAMP '2026-10-10 14:30:00', 'APROBADO');
INSERT INTO PAGOS (pago_id, reserva_id, monto, moneda, tipo_cambio, metodo_pago, codigo_transaccion, fecha_pago, estado_transaccion) VALUES (9, 6, 264, 'USD', 3.75, 'Transferencia', 'OP-902277', TIMESTAMP '2026-10-18 17:05:00', 'APROBADO');
INSERT INTO PAGOS (pago_id, reserva_id, monto, moneda, tipo_cambio, metodo_pago, codigo_transaccion, fecha_pago, estado_transaccion) VALUES (10, 7, 300, 'PEN', 1, 'Yape', 'YP-54001', TIMESTAMP '2026-10-05 11:10:00', 'REEMBOLSADO');

-- Montos pagados (suma de pagos APROBADOS en soles) y estados
UPDATE RESERVAS SET monto_pagado = 3700, estado_pago = 'PAGADO',  estado_reserva = 'COMPLETADA' WHERE reserva_id = 1;
UPDATE RESERVAS SET monto_pagado = 2940, estado_pago = 'PAGADO',  estado_reserva = 'CONFIRMADA' WHERE reserva_id = 2;
UPDATE RESERVAS SET monto_pagado = 1850, estado_pago = 'PARCIAL', estado_reserva = 'CONFIRMADA' WHERE reserva_id = 3;
UPDATE RESERVAS SET monto_pagado = 630,  estado_pago = 'PARCIAL', estado_reserva = 'CONFIRMADA' WHERE reserva_id = 5;
UPDATE RESERVAS SET monto_pagado = 990,  estado_pago = 'PARCIAL'                                WHERE reserva_id = 6;
-- Anulación: trg_reserva_estado_bu devuelve el cupo a la salida 4
UPDATE RESERVAS SET estado_reserva = 'ANULADA', estado_pago = 'REEMBOLSADO', forma_reembolso = 'Yape', usuario_anula_id = 1 WHERE reserva_id = 7;

-- ---------------------------------------------------------------------
-- 5. Operaciones de transporte (codigo_operacion lo genera el trigger)
-- ---------------------------------------------------------------------
-- Salida 7 (Machu Picchu, octubre): ya realizada
INSERT INTO OPERACION_TRANSPORTE (operacion_id, salida_id, origen_id, destino_id, empresa_id, vehiculo_id, conductor_id, pax_programados, hora_programada_salida, estado_actual, observaciones)
VALUES (1, 7, 1, 2, 1, 1, 1, 2, TO_DATE('2026-10-08 09:00', 'YYYY-MM-DD HH24:MI'), 'ASIGNADO', 'Recojo en aeropuerto');
-- Salida 8 (Colca): dos tramos, ya realizados
INSERT INTO OPERACION_TRANSPORTE (operacion_id, salida_id, origen_id, destino_id, empresa_id, vehiculo_id, conductor_id, pax_programados, hora_programada_salida, estado_actual)
VALUES (2, 8, 7, 8, 3, 6, 6, 3, TO_DATE('2026-10-21 10:00', 'YYYY-MM-DD HH24:MI'), 'ASIGNADO');
INSERT INTO OPERACION_TRANSPORTE (operacion_id, salida_id, origen_id, destino_id, empresa_id, vehiculo_id, conductor_id, pax_programados, hora_programada_salida, estado_actual)
VALUES (3, 8, 8, 9, 3, 6, 6, 3, TO_DATE('2026-10-22 06:00', 'YYYY-MM-DD HH24:MI'), 'ASIGNADO');
-- Salida 1 (Machu Picchu, noviembre)
INSERT INTO OPERACION_TRANSPORTE (operacion_id, salida_id, origen_id, destino_id, empresa_id, vehiculo_id, conductor_id, pax_programados, hora_programada_salida, estado_actual)
VALUES (4, 1, 1, 2, 1, 2, 2, 3, TO_DATE('2026-11-05 09:00', 'YYYY-MM-DD HH24:MI'), 'ASIGNADO');
INSERT INTO OPERACION_TRANSPORTE (operacion_id, salida_id, origen_id, destino_id, empresa_id, vehiculo_id, conductor_id, pax_programados, hora_programada_salida, estado_actual)
VALUES (5, 1, 2, 3, 1, 2, 2, 3, TO_DATE('2026-11-06 07:00', 'YYYY-MM-DD HH24:MI'), 'ASIGNADO');
-- Salida 3 (Lima Gastronómica): asignada
INSERT INTO OPERACION_TRANSPORTE (operacion_id, salida_id, origen_id, destino_id, empresa_id, vehiculo_id, conductor_id, pax_programados, hora_programada_salida, estado_actual)
VALUES (6, 3, 4, 5, 2, 5, 4, 3, TO_DATE('2026-11-14 08:00', 'YYYY-MM-DD HH24:MI'), 'ASIGNADO');
-- Salida 5 (Paracas): pendiente de asignar -> PKG_ASIGNACION.asignar_automatico
INSERT INTO OPERACION_TRANSPORTE (operacion_id, salida_id, origen_id, destino_id, pax_programados, hora_programada_salida, observaciones)
VALUES (7, 5, 5, 10, 4, TO_DATE('2026-11-07 06:30', 'YYYY-MM-DD HH24:MI'), 'Salida temprano por la Panamericana Sur');
-- Salida 2 (Machu Picchu, diciembre): pendiente de asignar
INSERT INTO OPERACION_TRANSPORTE (operacion_id, salida_id, origen_id, destino_id, pax_programados, hora_programada_salida)
VALUES (8, 2, 1, 2, 10, TO_DATE('2026-12-10 10:00', 'YYYY-MM-DD HH24:MI'));
-- Salida 6 (Tambopata): la única empresa con esa ruta está PENDIENTE de afiliación
INSERT INTO OPERACION_TRANSPORTE (operacion_id, salida_id, origen_id, destino_id, pax_programados, hora_programada_salida)
VALUES (9, 6, 12, 13, 2, TO_DATE('2026-12-03 11:00', 'YYYY-MM-DD HH24:MI'));

-- Avance de estados (cada cambio queda en HISTORIAL_TRAFICO_OPERACION)
UPDATE OPERACION_TRANSPORTE SET estado_actual = 'EMBARQUE'   WHERE operacion_id IN (1, 2, 3, 4, 5);
UPDATE OPERACION_TRANSPORTE SET estado_actual = 'EN_RUTA'    WHERE operacion_id IN (1, 2, 3, 4, 5);
UPDATE OPERACION_TRANSPORTE SET estado_actual = 'FINALIZADO' WHERE operacion_id IN (1, 2, 3, 4);
UPDATE OPERACION_TRANSPORTE SET estado_actual = 'RETRASADO', observaciones = 'Huaico en la vía' WHERE operacion_id = 5;

-- El historial se graba con la hora del sistema: se ajusta a las fechas de la demo
UPDATE HISTORIAL_TRAFICO_OPERACION h
   SET fecha_cambio = (SELECT CAST(o.hora_programada_salida AS TIMESTAMP)
                              + CASE h.estado_nuevo
                                    WHEN 'ASIGNADO'   THEN NUMTODSINTERVAL(-2, 'DAY')
                                    WHEN 'EMBARQUE'   THEN NUMTODSINTERVAL(-10, 'MINUTE')
                                    WHEN 'EN_RUTA'    THEN NUMTODSINTERVAL(5 * o.operacion_id, 'MINUTE')
                                    WHEN 'FINALIZADO' THEN NUMTODSINTERVAL(ROUND((o.hora_estimada_llegada - o.hora_programada_salida) * 1440) + 5 * o.operacion_id + 3, 'MINUTE')
                                    WHEN 'RETRASADO'  THEN NUMTODSINTERVAL(50, 'MINUTE')
                                    ELSE NUMTODSINTERVAL(-3, 'DAY')
                                END
                         FROM OPERACION_TRANSPORTE o
                        WHERE o.operacion_id = h.operacion_id)
 WHERE h.operacion_id IN (1, 2, 3, 4, 5);

-- ---------------------------------------------------------------------
-- 6. Estados finales de las salidas (trg_salida_confirmar valida el transporte)
-- ---------------------------------------------------------------------
UPDATE SALIDAS_PROGRAMADAS SET estado = 'COMPLETADA' WHERE salida_id = 7;
UPDATE SALIDAS_PROGRAMADAS SET estado = 'CONFIRMADA' WHERE salida_id IN (1, 8);
UPDATE SALIDAS_PROGRAMADAS SET estado = 'CANCELADA'  WHERE salida_id = 9;

COMMIT;

EXEC DBMS_SESSION.CLEAR_IDENTIFIER;

-- Las identidades continúan desde el último ID cargado
ALTER TABLE USUARIOS_SISTEMA     MODIFY usuario_id   GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE CLIENTES             MODIFY cliente_id   GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE GUIAS                MODIFY guia_id      GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE PAQUETES_TOUR        MODIFY paquete_id   GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE EMPRESA_TRANSPORTE   MODIFY empresa_id   GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE LUGARES              MODIFY lugar_id     GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE RUTAS_PROVEEDOR      MODIFY ruta_id      GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE VEHICULO             MODIFY vehiculo_id  GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE CONDUCTOR            MODIFY conductor_id GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE SALIDAS_PROGRAMADAS  MODIFY salida_id    GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE RESERVAS             MODIFY reserva_id   GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE DETALLE_RESERVA      MODIFY detalle_id   GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE PASAJEROS_RESERVA    MODIFY pasajero_id  GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE PAGOS                MODIFY pago_id      GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE OPERACION_TRANSPORTE MODIFY operacion_id GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);

-- Comprobación rápida
SELECT codigo_operacion, origen, destino, empresa, placa, estado FROM VW_TABLERO_OPERACIONES ORDER BY operacion_id;
SELECT salida_id, estado, fecha_fin, cupos_maximos, cupos_disponibles FROM SALIDAS_PROGRAMADAS ORDER BY salida_id;
