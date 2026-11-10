-- =====================================================================
-- KAMPU - Borra TODO el esquema (versión anterior y v3) para reinstalar.
-- Úsalo solo si ya tenías la base creada (con TRANSPORTE/CHOFERES o una
-- instalación previa de la v3). Luego ejecuta 01_schema_v3.sql, 02_extension_kampu.sql
-- y 03_data.sql.
-- ¡Elimina los datos! Ejecutar como script (F5) con el usuario dueño.
-- =====================================================================
BEGIN
    FOR o IN (SELECT 'VIEW' AS tipo, column_value AS nombre
                FROM TABLE(sys.odcivarchar2list(
                     'VW_RECURSOS_DISPONIBLES', 'VW_TABLERO_OPERACIONES',
                     'VW_EMPRESA_CAPACIDAD', 'V_RESERVAS_RESUMEN'))
              UNION ALL
              SELECT 'PACKAGE', 'PKG_ASIGNACION' FROM dual
              UNION ALL
              SELECT 'PACKAGE', 'PKG_RUTAS' FROM dual
              UNION ALL
              SELECT 'TABLE', column_value
                FROM TABLE(sys.odcivarchar2list(
                     'HISTORIAL_TRAFICO_OPERACION', 'OPERACION_TRANSPORTE', 'PAGOS',
                     'PASAJEROS_RESERVA', 'DETALLE_RESERVA', 'RESERVAS', 'SALIDAS_PROGRAMADAS',
                     'DOMINIO_TRANSICIONES', 'DOMINIO_ESTADOS', 'CONDUCTOR', 'VEHICULO',
                     'RUTAS_PROVEEDOR', 'LUGARES', 'EMPRESA_TRANSPORTE',
                     'CHOFERES', 'TRANSPORTE',          -- versión anterior
                     'PAQUETES_TOUR', 'GUIAS', 'CLIENTES', 'USUARIOS_SISTEMA'))
              UNION ALL
              SELECT 'SEQUENCE', 'SEQ_CODIGO_OPERACION' FROM dual) LOOP
        BEGIN
            IF o.tipo = 'TABLE' THEN
                EXECUTE IMMEDIATE 'DROP TABLE ' || o.nombre || ' CASCADE CONSTRAINTS PURGE';
            ELSE
                EXECUTE IMMEDIATE 'DROP ' || o.tipo || ' ' || o.nombre;
            END IF;
        EXCEPTION
            WHEN OTHERS THEN NULL;   -- no existía
        END;
    END LOOP;
END;
/
