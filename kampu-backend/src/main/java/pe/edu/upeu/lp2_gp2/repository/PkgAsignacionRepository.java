package pe.edu.upeu.lp2_gp2.repository;

import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.lp2_gp2.dto.RecursoCandidatoDTO;
import pe.edu.upeu.lp2_gp2.dto.RecursoDisponibleDTO;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Acceso a la lógica PL/SQL de 01_schema_v3.sql que no se mapea con JPA:
 * el paquete PKG_ASIGNACION, la vista VW_RECURSOS_DISPONIBLES (sin clave única),
 * el catálogo DOMINIO_TRANSICIONES y el identificador de sesión que usa
 * trg_operacion_transporte para llenar HISTORIAL_TRAFICO_OPERACION.usuario_registro.
 *
 * Dentro de un @Transactional, JdbcTemplate usa la misma conexión que JPA, así que
 * todo queda en la misma transacción.
 */
@Repository
public class PkgAsignacionRepository {
    private final JdbcTemplate jdbc;

    public PkgAsignacionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ---------- PKG_ASIGNACION ----------

    /** PKG_ASIGNACION.asignar_automatico: elige vehículo + conductor y deja la operación en ASIGNADO. */
    public void asignarAutomatico(Long operacionId) {
        jdbc.execute("{call PKG_ASIGNACION.asignar_automatico(?)}", (CallableStatementCallback<Void>) cs -> {
            cs.setLong(1, operacionId);
            cs.execute();
            return null;
        });
    }

    /** PKG_ASIGNACION.faltante_transporte: NULL si el transporte alcanza; si no, el primer tramo que no alcanza. */
    public String faltanteTransporte(Long salidaId, int pasajeros) {
        return jdbc.queryForObject("SELECT PKG_ASIGNACION.faltante_transporte(?, ?) FROM dual",
                String.class, salidaId, pasajeros);
    }

    /** faltante_transporte de todas las salidas activas (PROGRAMADA o CONFIRMADA) en una sola consulta. */
    public Map<Long, String> faltantesPorSalida() {
        Map<Long, String> mapa = new HashMap<>();
        jdbc.query("""
                SELECT s.salida_id,
                       PKG_ASIGNACION.faltante_transporte(s.salida_id, s.cupos_maximos - s.cupos_disponibles) AS faltante
                  FROM SALIDAS_PROGRAMADAS s
                 WHERE s.estado IN ('PROGRAMADA', 'CONFIRMADA')""",
                rs -> {
                    mapa.put(rs.getLong("salida_id"), rs.getString("faltante"));
                });
        return mapa;
    }

    /** Operaciones vigentes (estados que ocupan recursos) por salida. */
    public Map<Long, Integer> operacionesPorSalida() {
        Map<Long, Integer> mapa = new HashMap<>();
        jdbc.query("""
                SELECT o.salida_id, COUNT(*) AS total
                  FROM OPERACION_TRANSPORTE o
                  JOIN DOMINIO_ESTADOS e ON e.codigo_estado = o.estado_actual
                 WHERE o.salida_id IS NOT NULL AND e.ocupa_recursos = 'S'
                 GROUP BY o.salida_id""",
                rs -> {
                    mapa.put(rs.getLong("salida_id"), rs.getInt("total"));
                });
        return mapa;
    }

    /** Vista VW_RECURSOS_DISPONIBLES: recursos elegibles para una operación PROGRAMADA. */
    public List<RecursoDisponibleDTO> recursosDisponibles(Long operacionId) {
        return jdbc.query("""
                SELECT tipo_recurso, recurso_id, empresa_id, empresa, descripcion, capacidad_pax, holgura_pax, horas_dia
                  FROM VW_RECURSOS_DISPONIBLES
                 WHERE operacion_id = ?
                 ORDER BY tipo_recurso DESC, holgura_pax, horas_dia, recurso_id""",
                (rs, i) -> new RecursoDisponibleDTO(rs.getString("tipo_recurso"), rs.getLong("recurso_id"),
                        rs.getLong("empresa_id"), rs.getString("empresa"), rs.getString("descripcion"),
                        entero(rs.getObject("capacidad_pax")), entero(rs.getObject("holgura_pax")),
                        (BigDecimal) rs.getObject("horas_dia")),
                operacionId);
    }

    /**
     * Todos los vehículos y conductores de las empresas que tienen la ruta de la operación,
     * con el motivo por el que no son elegibles (PKG_ASIGNACION.motivo_vehiculo / motivo_conductor).
     * motivo = NULL significa que el recurso se puede asignar.
     */
    public List<RecursoCandidatoDTO> candidatos(Long operacionId) {
        return jdbc.query("""
                SELECT 'VEHICULO' AS tipo_recurso, v.vehiculo_id AS recurso_id, e.empresa_id, e.nombre_comercial AS empresa,
                       v.placa || ' · ' || NVL(v.marca_modelo, v.tipo_vehiculo) AS descripcion,
                       v.capacidad_pax, CAST(NULL AS NUMBER) AS horas_dia, r.estado AS estado_ruta,
                       PKG_ASIGNACION.motivo_vehiculo(v.vehiculo_id, o.hora_programada_salida, o.hora_estimada_llegada,
                                                      o.pax_programados, o.operacion_id) AS motivo
                  FROM OPERACION_TRANSPORTE o
                  JOIN RUTAS_PROVEEDOR    r ON r.origen_id = o.origen_id AND r.destino_id = o.destino_id
                  JOIN EMPRESA_TRANSPORTE e ON e.empresa_id = r.empresa_id
                  JOIN VEHICULO           v ON v.empresa_id = r.empresa_id
                 WHERE o.operacion_id = ?
                UNION ALL
                SELECT 'CONDUCTOR', c.conductor_id, e.empresa_id, e.nombre_comercial,
                       c.nombres || ' (' || c.categoria_licencia || ')',
                       CAST(NULL AS NUMBER),
                       PKG_ASIGNACION.horas_conductor_dia(c.conductor_id, o.hora_programada_salida, o.operacion_id),
                       r.estado,
                       PKG_ASIGNACION.motivo_conductor(c.conductor_id, o.hora_programada_salida, o.hora_estimada_llegada,
                                                       o.operacion_id)
                  FROM OPERACION_TRANSPORTE o
                  JOIN RUTAS_PROVEEDOR    r ON r.origen_id = o.origen_id AND r.destino_id = o.destino_id
                  JOIN EMPRESA_TRANSPORTE e ON e.empresa_id = r.empresa_id
                  JOIN CONDUCTOR          c ON c.empresa_id = r.empresa_id
                 WHERE o.operacion_id = ?
                 ORDER BY 1 DESC, 4, 2""",
                (rs, i) -> {
                    String motivo = rs.getString("motivo");
                    if (motivo == null && !"ACTIVA".equals(rs.getString("estado_ruta"))) {
                        motivo = "la ruta de la empresa está INACTIVA";
                    }
                    return new RecursoCandidatoDTO(rs.getString("tipo_recurso"), rs.getLong("recurso_id"),
                            rs.getLong("empresa_id"), rs.getString("empresa"), rs.getString("descripcion"),
                            entero(rs.getObject("capacidad_pax")), (BigDecimal) rs.getObject("horas_dia"), motivo);
                },
                operacionId, operacionId);
    }

    // ---------- Catálogo de estados ----------

    /** DOMINIO_TRANSICIONES agrupadas por estado de origen. */
    public Map<String, List<String>> transiciones() {
        Map<String, List<String>> mapa = new HashMap<>();
        jdbc.query("""
                SELECT t.estado_origen, t.estado_destino
                  FROM DOMINIO_TRANSICIONES t
                  JOIN DOMINIO_ESTADOS d ON d.codigo_estado = t.estado_destino
                 ORDER BY t.estado_origen, d.orden""",
                rs -> {
                    mapa.computeIfAbsent(rs.getString("estado_origen"), k -> new java.util.ArrayList<>())
                            .add(rs.getString("estado_destino"));
                });
        return mapa;
    }

    // ---------- Historial ----------

    /** Guarda una observación en el último registro de historial de la operación (lo creó el trigger). */
    public void observarUltimoCambio(Long operacionId, String observacion) {
        jdbc.update("""
                UPDATE HISTORIAL_TRAFICO_OPERACION
                   SET observacion = ?
                 WHERE historial_id = (SELECT MAX(historial_id) FROM HISTORIAL_TRAFICO_OPERACION WHERE operacion_id = ?)""",
                observacion, operacionId);
    }

    /**
     * USERENV.CLIENT_IDENTIFIER: trg_operacion_transporte lo guarda como usuario_registro.
     * La conexión vuelve al pool, por eso hay que limpiarlo al terminar (ver limpiarUsuario).
     */
    public void identificarUsuario(String usuario) {
        if (usuario == null || usuario.isBlank()) return;
        String id = usuario.trim();
        if (id.length() > 64) id = id.substring(0, 64);
        jdbc.update("BEGIN DBMS_SESSION.SET_IDENTIFIER(?); END;", id);
    }

    public void limpiarUsuario() {
        jdbc.update("BEGIN DBMS_SESSION.CLEAR_IDENTIFIER; END;");
    }

    private static Integer entero(Object valor) {
        return valor == null ? null : ((Number) valor).intValue();
    }
}
