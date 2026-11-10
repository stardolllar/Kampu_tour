-- =====================================================================
-- KAMPU / TOUR MANAGER v4 - PERMISOS DEL USUARIO DUEÑO DEL ESQUEMA
-- Ejecutar UNA SOLA VEZ, conectado como SYSTEM (o SYS / un DBA),
-- en la misma base (PDB) donde está el usuario LP2. Ejemplos:
--   SQL Developer: conexión SYSTEM con servicio XEPDB1 / FREEPDB1 / XE
--   SQL*Plus     : sqlplus system/<clave>@localhost:1521/XEPDB1
-- Si tu usuario no se llama LP2, reemplázalo abajo.
--
-- Sin estos permisos aparecen errores como:
--   ORA-01031: privilegios insuficientes  (al crear VW_PAQUETES_CLIENTE /
--              VW_RESUMEN_PAQUETES_POR_ROL, o los roles del script 03)
--   ORA-01950: no existen privilegios en tablespace 'USERS' (al insertar)
-- =====================================================================

-- Objetos de 01_schema_v4.sql
GRANT CREATE SESSION   TO LP2;
GRANT CREATE TABLE     TO LP2;
GRANT CREATE SEQUENCE  TO LP2;
GRANT CREATE VIEW      TO LP2;   -- VW_PAQUETES_CLIENTE, VW_RESUMEN_PAQUETES_POR_ROL
GRANT CREATE PROCEDURE TO LP2;   -- FN_, SP_ y PKG_ (funciones, procedimientos y paquetes)
GRANT CREATE TRIGGER   TO LP2;   -- TRG_AUDITORIA_ESTADO_PAQUETE, TRG_VALIDAR_PAGO_PAQUETE

-- Roles por nivel de usuario de 03_particionamiento_roles.sql
GRANT CREATE ROLE      TO LP2;

-- Espacio para guardar datos
ALTER USER LP2 QUOTA UNLIMITED ON USERS;

-- Verificación (conectado como LP2): SELECT privilege FROM USER_SYS_PRIVS ORDER BY 1;
