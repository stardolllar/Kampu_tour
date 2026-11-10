-- =====================================================================
-- KAMPU / TOUR MANAGER - EXTENSIÓN sobre el script v3
-- Ejecutar DESPUÉS de 01_schema_v3.sql (y antes de 03_data.sql), como script
-- (F5), con el usuario dueño. También sirve para una base v3 que ya tiene
-- datos: no borra nada (si hay teléfonos o documentos repetidos, la creación
-- del índice único falla; ver las consultas de la sección 0).
--
-- Contenido
--   1. Teléfonos y documentos sin repetir    (índices únicos)
--   2. Un pasajero no se repite en la misma salida  (trigger)
--   3. Coordenadas de LUGARES y destino de cada paquete
--   4. Rutas hacia un paquete                (RUTAS_PROVEEDOR.paquete_id)
--   5. PKG_RUTAS: duración estimada con datos reales (coordenadas)
--   6. trg_ruta_biu: destino del paquete y duración automática
-- =====================================================================

-- ---------------------------------------------------------------------
-- 0. (Base con datos) Revisa que estas consultas no devuelvan filas
-- ---------------------------------------------------------------------
-- SELECT TRANSLATE(telefono, '0+-() ', '0') tel, COUNT(*) FROM CLIENTES GROUP BY TRANSLATE(telefono, '0+-() ', '0') HAVING COUNT(*) > 1;
-- SELECT UPPER(numero_documento) doc, COUNT(*) FROM CLIENTES GROUP BY UPPER(numero_documento) HAVING COUNT(*) > 1;
-- SELECT TRANSLATE(telefono, '0+-() ', '0') tel, COUNT(*) FROM GUIAS WHERE telefono IS NOT NULL GROUP BY TRANSLATE(telefono, '0+-() ', '0') HAVING COUNT(*) > 1;
-- SELECT TRANSLATE(telefono, '0+-() ', '0') tel, COUNT(*) FROM CONDUCTOR WHERE telefono IS NOT NULL GROUP BY TRANSLATE(telefono, '0+-() ', '0') HAVING COUNT(*) > 1;
-- SELECT TRANSLATE(contacto_telefono, '0+-() ', '0') tel, COUNT(*) FROM EMPRESA_TRANSPORTE GROUP BY TRANSLATE(contacto_telefono, '0+-() ', '0') HAVING COUNT(*) > 1;

-- ---------------------------------------------------------------------
-- 1. TELÉFONOS Y DOCUMENTOS SIN REPETIR
--    Se comparan solo los dígitos (TRANSLATE quita + - ( ) y espacios):
--    '+51 987 654 321' = '+51987654321'. (REGEXP_REPLACE no se puede indexar.)
--    (Los NULL no cuentan: guías y conductores pueden no tener teléfono.)
-- ---------------------------------------------------------------------
CREATE UNIQUE INDEX uq_cli_telefono  ON CLIENTES (TRANSLATE(telefono, '0+-() ', '0'));
CREATE UNIQUE INDEX uq_guia_telefono ON GUIAS (TRANSLATE(telefono, '0+-() ', '0'));
CREATE UNIQUE INDEX uq_cond_telefono ON CONDUCTOR (TRANSLATE(telefono, '0+-() ', '0'));
CREATE UNIQUE INDEX uq_emp_telefono  ON EMPRESA_TRANSPORTE (TRANSLATE(contacto_telefono, '0+-() ', '0'));
-- El número de documento del cliente es único sin importar el tipo
-- (uq_cli_tipo_doc del v3 solo impedía repetir el mismo tipo + número)
CREATE UNIQUE INDEX uq_cli_numero_doc ON CLIENTES (UPPER(numero_documento));

-- ---------------------------------------------------------------------
-- 2. UN PASAJERO (DNI/CE/PASAPORTE) NO SE REPITE EN LA MISMA SALIDA
--    entre reservas activas (las ANULADAS no cuentan). En salidas distintas
--    la misma persona sí puede viajar.
-- ---------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_pasajeros_doc_salida
FOR INSERT OR UPDATE OF detalle_id, tipo_documento, numero_documento ON PASAJEROS_RESERVA
COMPOUND TRIGGER
    TYPE t_ids IS TABLE OF NUMBER INDEX BY PLS_INTEGER;
    g_ids t_ids;

    AFTER EACH ROW IS
    BEGIN
        g_ids(g_ids.COUNT + 1) := :NEW.pasajero_id;
    END AFTER EACH ROW;

    AFTER STATEMENT IS
        v_total NUMBER;
    BEGIN
        FOR i IN 1 .. g_ids.COUNT LOOP
            FOR p IN (SELECT pr.tipo_documento, pr.numero_documento, pr.nombre_completo, d.salida_id
                        FROM PASAJEROS_RESERVA pr
                        JOIN DETALLE_RESERVA d ON d.detalle_id = pr.detalle_id
                       WHERE pr.pasajero_id = g_ids(i)) LOOP
                SELECT COUNT(*) INTO v_total
                  FROM PASAJEROS_RESERVA x
                  JOIN DETALLE_RESERVA d ON d.detalle_id = x.detalle_id
                  JOIN RESERVAS r        ON r.reserva_id = d.reserva_id
                 WHERE d.salida_id = p.salida_id
                   AND r.estado_reserva <> 'ANULADA'
                   AND x.tipo_documento = p.tipo_documento
                   AND UPPER(x.numero_documento) = UPPER(p.numero_documento);
                IF v_total > 1 THEN
                    RAISE_APPLICATION_ERROR(-20042,
                        'El pasajero ' || p.nombre_completo || ' (' || p.tipo_documento || ' ' || p.numero_documento
                        || ') ya tiene una reserva activa en la salida ' || p.salida_id);
                END IF;
            END LOOP;
        END LOOP;
    END AFTER STATEMENT;
END trg_pasajeros_doc_salida;
/

-- ---------------------------------------------------------------------
-- 3. COORDENADAS DE LOS LUGARES Y DESTINO DE CADA PAQUETE
-- ---------------------------------------------------------------------
ALTER TABLE LUGARES ADD (
    latitud  NUMBER(9,6),
    longitud NUMBER(9,6),
    CONSTRAINT chk_lug_latitud  CHECK (latitud  BETWEEN -90  AND 90),
    CONSTRAINT chk_lug_longitud CHECK (longitud BETWEEN -180 AND 180),
    CONSTRAINT chk_lug_coords   CHECK ((latitud IS NULL AND longitud IS NULL)
                                       OR (latitud IS NOT NULL AND longitud IS NOT NULL))
);

-- Lugar al que llega el paquete (estación, hotel, atractivo...)
ALTER TABLE PAQUETES_TOUR ADD (
    lugar_destino_id NUMBER,
    CONSTRAINT fk_paq_lugar_destino FOREIGN KEY (lugar_destino_id) REFERENCES LUGARES(lugar_id)
);
CREATE INDEX idx_paq_lugar_destino ON PAQUETES_TOUR (lugar_destino_id);

-- ---------------------------------------------------------------------
-- 4. RUTAS HACIA UN PAQUETE
--    Si la ruta lleva paquete_id, su destino es el lugar del paquete
--    (lo pone trg_ruta_biu) y el backend crea la salida programada.
-- ---------------------------------------------------------------------
ALTER TABLE RUTAS_PROVEEDOR ADD (
    paquete_id NUMBER,
    CONSTRAINT fk_ruta_paquete FOREIGN KEY (paquete_id) REFERENCES PAQUETES_TOUR(paquete_id)
);
CREATE INDEX idx_ruta_paquete ON RUTAS_PROVEEDOR (paquete_id);

-- ---------------------------------------------------------------------
-- 5. PKG_RUTAS: distancia y duración estimada con las coordenadas reales
--    El backend consulta primero OSRM (red vial de OpenStreetMap); si no hay
--    conexión, usa esta estimación: distancia en línea recta (haversine)
--    x factor de desvío de la carretera / velocidad media + margen.
-- ---------------------------------------------------------------------
CREATE OR REPLACE PACKAGE PKG_RUTAS AS
    c_radio_tierra_km CONSTANT NUMBER := 6371;

    -- Distancia en línea recta (km) entre dos lugares; NULL si falta alguna coordenada
    FUNCTION distancia_km (p_origen_id NUMBER, p_destino_id NUMBER) RETURN NUMBER;

    -- Distancia aproximada por carretera (km) según el tipo de servicio
    FUNCTION distancia_vial_km (p_origen_id NUMBER, p_destino_id NUMBER, p_tipo_servicio VARCHAR2) RETURN NUMBER;

    -- Minutos estimados (redondeados a 5); NULL si falta alguna coordenada
    FUNCTION duracion_estimada_min (p_origen_id NUMBER, p_destino_id NUMBER,
                                    p_tipo_servicio VARCHAR2 DEFAULT 'TRASLADO') RETURN NUMBER;
END PKG_RUTAS;
/

CREATE OR REPLACE PACKAGE BODY PKG_RUTAS AS

    -- Calibrado con tiempos reales de rutas turísticas del Perú (ver 03_data.sql):
    --   carretera = línea recta x 1.3
    --   velocidad media = 18 km/h + 0.3 x km en línea recta, máximo 80 km/h
    --   (tramos cortos = tráfico urbano; tramos largos = carretera)
    --   margen = embarque / desembarque
    c_factor_vial CONSTANT NUMBER := 1.3;
    c_vel_base    CONSTANT NUMBER := 18;
    c_vel_por_km  CONSTANT NUMBER := 0.3;
    c_vel_maxima  CONSTANT NUMBER := 80;

    FUNCTION margen_min (p_tipo VARCHAR2) RETURN NUMBER IS
    BEGIN
        RETURN CASE p_tipo WHEN 'INTERPROVINCIAL' THEN 10 ELSE 5 END;
    END margen_min;

    FUNCTION distancia_km (p_origen_id NUMBER, p_destino_id NUMBER) RETURN NUMBER IS
        v_lat1 NUMBER; v_lon1 NUMBER; v_lat2 NUMBER; v_lon2 NUMBER;
        v_rad  CONSTANT NUMBER := ACOS(-1) / 180;
        v_a    NUMBER;
    BEGIN
        SELECT latitud, longitud INTO v_lat1, v_lon1 FROM LUGARES WHERE lugar_id = p_origen_id;
        SELECT latitud, longitud INTO v_lat2, v_lon2 FROM LUGARES WHERE lugar_id = p_destino_id;
        IF v_lat1 IS NULL OR v_lat2 IS NULL THEN
            RETURN NULL;
        END IF;
        -- Fórmula de haversine
        v_a := POWER(SIN((v_lat2 - v_lat1) * v_rad / 2), 2)
             + COS(v_lat1 * v_rad) * COS(v_lat2 * v_rad) * POWER(SIN((v_lon2 - v_lon1) * v_rad / 2), 2);
        RETURN ROUND(2 * c_radio_tierra_km * ATAN2(SQRT(v_a), SQRT(1 - v_a)), 2);
    EXCEPTION
        WHEN NO_DATA_FOUND THEN RETURN NULL;
    END distancia_km;

    FUNCTION distancia_vial_km (p_origen_id NUMBER, p_destino_id NUMBER, p_tipo_servicio VARCHAR2) RETURN NUMBER IS
    BEGIN
        RETURN ROUND(distancia_km(p_origen_id, p_destino_id) * c_factor_vial, 1);
    END distancia_vial_km;

    FUNCTION duracion_estimada_min (p_origen_id NUMBER, p_destino_id NUMBER,
                                    p_tipo_servicio VARCHAR2 DEFAULT 'TRASLADO') RETURN NUMBER IS
        v_km        NUMBER;
        v_velocidad NUMBER;
    BEGIN
        v_km := distancia_km(p_origen_id, p_destino_id);
        IF v_km IS NULL THEN
            RETURN NULL;
        END IF;
        v_velocidad := LEAST(c_vel_maxima, c_vel_base + c_vel_por_km * v_km);
        RETURN GREATEST(5, CEIL((v_km * c_factor_vial / v_velocidad * 60 + margen_min(p_tipo_servicio)) / 5) * 5);
    END duracion_estimada_min;

END PKG_RUTAS;
/

-- ---------------------------------------------------------------------
-- 6. trg_ruta_biu
--    - Ruta hacia un paquete: el destino es el lugar del paquete.
--    - Duración vacía: se calcula con PKG_RUTAS (coordenadas reales).
-- ---------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_ruta_biu
BEFORE INSERT OR UPDATE OF origen_id, destino_id, tipo_servicio, duracion_estimada_min, paquete_id
ON RUTAS_PROVEEDOR
FOR EACH ROW
DECLARE
    v_lugar  PAQUETES_TOUR.lugar_destino_id%TYPE;
    v_nombre PAQUETES_TOUR.nombre%TYPE;
BEGIN
    IF :NEW.paquete_id IS NOT NULL THEN
        SELECT lugar_destino_id, nombre INTO v_lugar, v_nombre
          FROM PAQUETES_TOUR WHERE paquete_id = :NEW.paquete_id;
        IF v_lugar IS NULL THEN
            RAISE_APPLICATION_ERROR(-20071,
                'El paquete "' || v_nombre || '" no tiene lugar de destino; asígnalo antes de crear la ruta');
        END IF;
        :NEW.destino_id := v_lugar;
    END IF;

    IF :NEW.duracion_estimada_min IS NULL THEN
        :NEW.duracion_estimada_min := PKG_RUTAS.duracion_estimada_min(
                                          :NEW.origen_id, :NEW.destino_id, :NEW.tipo_servicio);
        IF :NEW.duracion_estimada_min IS NULL THEN
            RAISE_APPLICATION_ERROR(-20070,
                'No se puede calcular la duración: registra la latitud y longitud del origen y del destino, o indícala a mano');
        END IF;
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN NULL;   -- paquete inexistente: lo reporta la FK fk_ruta_paquete
END;
/

-- Verificar que todo compiló
SELECT object_type, object_name, status
  FROM USER_OBJECTS
 WHERE object_name IN ('PKG_RUTAS', 'TRG_RUTA_BIU', 'TRG_PASAJEROS_DOC_SALIDA')
 ORDER BY object_type, object_name;
