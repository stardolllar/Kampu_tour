package pe.edu.upeu.lp2_gp2.exception;

import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Traduce los errores de Oracle a mensajes para el usuario:
 *  - ORA-20000..20999: RAISE_APPLICATION_ERROR de los triggers y de PKG_ASIGNACION
 *    (01_schema_v3.sql); su texto ya está en español y se devuelve tal cual.
 *  - ORA-00001 / 02290 / 02291 / 02292: restricciones UNIQUE, CHECK y FK, con un
 *    mensaje por nombre de restricción.
 */
public final class ErroresOracle {
    private ErroresOracle() {
    }

    private static final Pattern RESTRICCION = Pattern.compile("\\(([A-Za-z0-9_$#]+)\\.([A-Za-z0-9_$#]+)\\)");

    private static final Map<String, String> MENSAJES = Map.ofEntries(
            // UNIQUE
            Map.entry("UQ_EMP_RUC", "Ya existe una empresa con ese RUC."),
            Map.entry("UQ_VEH_PLACA", "Ya existe un vehículo con esa placa."),
            Map.entry("UQ_COND_LICENCIA", "Ya existe un conductor con ese número de licencia."),
            Map.entry("UQ_LUG_NOMBRE", "Ya existe un lugar con ese nombre en esa ciudad."),
            Map.entry("UQ_RUTA_EMP_OD", "La empresa ya tiene registrada esa ruta."),
            Map.entry("UQ_CLI_TIPO_DOC", "Ya existe un cliente con ese tipo y número de documento."),
            Map.entry("UQ_DET_RESERVA_SALIDA", "La reserva ya incluye esa salida."),
            Map.entry("UQ_PAS_DETALLE_DOC", "El documento de un pasajero está repetido en la reserva."),
            Map.entry("UQ_OP_CODIGO", "El código de operación ya existe."),
            // 02_extension_kampu.sql
            Map.entry("UQ_CLI_TELEFONO", "Ese teléfono ya está registrado para otro cliente."),
            Map.entry("UQ_CLI_NUMERO_DOC", "Ese número de documento ya está registrado para otro cliente."),
            Map.entry("UQ_GUIA_TELEFONO", "Ese teléfono ya está registrado para otro guía."),
            Map.entry("UQ_COND_TELEFONO", "Ese teléfono ya está registrado para otro conductor."),
            Map.entry("UQ_EMP_TELEFONO", "Ese teléfono ya está registrado para otra empresa."),
            Map.entry("CHK_LUG_COORDS", "Indica la latitud y la longitud juntas (o deja ambas vacías)."),
            Map.entry("FK_RUTA_PAQUETE", "El paquete indicado no existe."),
            Map.entry("FK_PAQ_LUGAR_DESTINO", "El lugar de destino indicado no existe."),
            // CHECK
            Map.entry("CHK_EMP_RUC", "El RUC tiene 11 dígitos y empieza con 10, 15, 17 o 20."),
            Map.entry("CHK_USU_ROL", "Rol no válido (ADMIN, VENDEDOR u OPERADOR)."),
            Map.entry("CHK_PAQ_DIAS", "Las noches no pueden ser más que los días."),
            Map.entry("CHK_RUTA_OD", "El origen y el destino deben ser distintos."),
            Map.entry("CHK_OP_OD", "El origen y el destino deben ser distintos."),
            Map.entry("CHK_OP_HORAS", "La llegada estimada debe ser posterior a la hora de salida."),
            Map.entry("CHK_OP_VEH_EMP", "Indica la empresa del vehículo."),
            Map.entry("CHK_OP_COND_EMP", "Indica la empresa del conductor."),
            Map.entry("CHK_SALIDA_CUPOS", "Los cupos disponibles no pueden ser negativos ni superar los cupos máximos."),
            Map.entry("CHK_RES_MONTO_PAGADO", "El monto pagado no puede superar el total de la reserva."),
            Map.entry("CHK_RES_CLIENTE_O_CONTACTO", "Selecciona un cliente o ingresa los datos del invitado."),
            Map.entry("CHK_RES_ANULACION", "Solo una reserva anulada registra usuario y forma de reembolso."),
            Map.entry("CHK_DET_DESCUENTO", "El descuento no puede superar el importe del detalle."),
            Map.entry("CHK_PAGO_TC", "El tipo de cambio de un pago en soles debe ser 1."),
            // FOREIGN KEY: padre inexistente (02291)
            Map.entry("FK_OP_RUTA", "La empresa no opera esa ruta (origen → destino). Regístrala en Rutas o elige otra empresa."),
            Map.entry("FK_OP_VEHICULO", "El vehículo no pertenece a la empresa de la operación."),
            Map.entry("FK_OP_CONDUCTOR", "El conductor no pertenece a la empresa del vehículo."),
            Map.entry("FK_OP_SALIDA", "La salida indicada no existe."),
            Map.entry("FK_RUTA_EMPRESA", "La empresa indicada no existe."),
            Map.entry("FK_VEH_EMPRESA", "La empresa indicada no existe."),
            Map.entry("FK_COND_EMPRESA", "La empresa indicada no existe.")
    );

    /** Mensaje para el usuario, o null si la excepción no viene de Oracle. */
    public static String mensaje(Throwable e) {
        SQLException sql = buscarSql(e);
        if (sql == null) return null;
        int codigo = sql.getErrorCode();
        String texto = sql.getMessage() == null ? "" : sql.getMessage();
        if (codigo >= 20000 && codigo <= 20999) {
            return primeraLinea(texto).replaceFirst("^ORA-2\\d{4}:\\s*", "");
        }
        String restriccion = restriccion(texto);
        if (restriccion != null && MENSAJES.containsKey(restriccion)) return MENSAJES.get(restriccion);
        return switch (codigo) {
            case 1 -> "Ya existe un registro con esos datos.";
            case 2290 -> "Los datos no cumplen una regla de la base de datos" + (restriccion == null ? "." : " (" + restriccion + ").");
            case 2291 -> "Un dato relacionado no existe" + (restriccion == null ? "." : " (" + restriccion + ").");
            case 2292 -> "El registro está en uso por otros datos y no se puede eliminar.";
            case 1400 -> "Falta un dato obligatorio.";
            default -> null;
        };
    }

    private static SQLException buscarSql(Throwable e) {
        SQLException ultima = null;
        for (Throwable t = e; t != null && t != t.getCause(); t = t.getCause()) {
            if (t instanceof SQLException s) ultima = s;
        }
        return ultima;
    }

    private static String primeraLinea(String texto) {
        int salto = texto.indexOf('\n');
        return (salto < 0 ? texto : texto.substring(0, salto)).trim();
    }

    private static String restriccion(String texto) {
        Matcher m = RESTRICCION.matcher(texto);
        return m.find() ? m.group(2).toUpperCase() : null;
    }
}
