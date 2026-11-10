package pe.edu.upeu.lp2_gp2.itinerario.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.edu.upeu.lp2_gp2.entity.Lugar;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * Orden óptimo de las paradas de un viaje (hoteles a recoger o dejar) con OSRM:
 *   GET {osrm}/trip/v1/driving/{lon,lat;...}?roundtrip=false&source=first|any&destination=last|any
 * Si OSRM no responde (sin internet, servidor caído) se usa "vecino más cercano" con distancia
 * en línea recta, para que el operador siempre tenga un manifiesto ordenado.
 * Configuración compartida con DuracionRutaService: kampu.rutas.osrm-url / osrm-habilitado.
 */
@Service
public class RutaOptimizadaService {
    private static final double KMH_CIUDAD = 30.0;   // velocidad media urbana para el respaldo

    public record Resultado(List<Lugar> orden, List<Integer> minutosTramo, String fuente) {
        public Integer total() {
            return minutosTramo.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
        }
    }

    private final String osrmUrl;
    private final boolean osrmHabilitado;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private final ObjectMapper json = new ObjectMapper();

    public RutaOptimizadaService(@Value("${kampu.rutas.osrm-url:https://router.project-osrm.org}") String osrmUrl,
                                 @Value("${kampu.rutas.osrm-habilitado:true}") boolean osrmHabilitado) {
        this.osrmUrl = osrmUrl.endsWith("/") ? osrmUrl.substring(0, osrmUrl.length() - 1) : osrmUrl;
        this.osrmHabilitado = osrmHabilitado;
    }

    /**
     * @param inicio  punto de partida fijo (aeropuerto en Transfer In) o null
     * @param paradas paradas intermedias a ordenar (hoteles); se eliminan duplicados
     * @param fin     punto final fijo (atractivo en Tour, aeropuerto en Transfer Out) o null
     * @return orden completo [inicio?, paradas..., fin?] y minutos de cada tramo (el primero es 0)
     */
    public Resultado optimizar(Lugar inicio, List<Lugar> paradas, Lugar fin) {
        Map<Long, Lugar> sinRepetir = new LinkedHashMap<>();
        for (Lugar l : paradas) sinRepetir.putIfAbsent(l.getLugarId(), l);
        List<Lugar> unicas = new ArrayList<>(sinRepetir.values());
        List<Lugar> todos = new ArrayList<>();
        if (inicio != null) todos.add(inicio);
        todos.addAll(unicas);
        if (fin != null) todos.add(fin);
        if (todos.size() <= 1 || todos.stream().anyMatch(l -> l.getLatitud() == null || l.getLongitud() == null)) {
            return new Resultado(todos, ceros(todos.size()), "SIN_COORDENADAS");
        }
        Resultado osrm = osrm(todos, inicio != null, fin != null);
        return osrm != null ? osrm : vecinoMasCercano(inicio, unicas, fin);
    }

    private Resultado osrm(List<Lugar> todos, boolean inicioFijo, boolean finFijo) {
        if (!osrmHabilitado) return null;
        StringJoiner coords = new StringJoiner(";");
        for (Lugar l : todos) coords.add(l.getLongitud().toPlainString() + "," + l.getLatitud().toPlainString());
        String url = osrmUrl + "/trip/v1/driving/" + coords + "?roundtrip=false&overview=false"
                + "&source=" + (inicioFijo ? "first" : "any") + "&destination=" + (finFijo ? "last" : "any");
        try {
            HttpResponse<String> r = http.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(5)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() != 200) return null;
            JsonNode raiz = json.readTree(r.body());
            if (!"Ok".equals(raiz.path("code").asText())) return null;
            // waypoints[i].waypoint_index = posición del punto i dentro del recorrido
            Lugar[] orden = new Lugar[todos.size()];
            JsonNode wps = raiz.path("waypoints");
            for (int i = 0; i < wps.size(); i++) orden[wps.get(i).path("waypoint_index").asInt()] = todos.get(i);
            List<Integer> tramos = new ArrayList<>(List.of(0));
            for (JsonNode leg : raiz.path("trips").path(0).path("legs")) {
                tramos.add((int) Math.round(leg.path("duration").asDouble() / 60.0));
            }
            if (Arrays.stream(orden).anyMatch(Objects::isNull) || tramos.size() != orden.length) return null;
            return new Resultado(List.of(orden), tramos, "OSRM");
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return null;
        }
    }

    // Respaldo: desde el inicio (o desde la parada más lejana al fin) siempre a la más cercana
    private Resultado vecinoMasCercano(Lugar inicio, List<Lugar> paradas, Lugar fin) {
        List<Lugar> pendientes = new ArrayList<>(paradas);
        List<Lugar> orden = new ArrayList<>();
        Lugar actual = inicio;
        if (actual == null && !pendientes.isEmpty()) {
            Lugar ref = fin != null ? fin : pendientes.get(0);
            actual = pendientes.stream().max(Comparator.comparingDouble(l -> km(l, ref))).orElseThrow();
            pendientes.remove(actual);
        }
        orden.add(actual);
        while (!pendientes.isEmpty()) {
            Lugar desde = actual;
            Lugar sig = pendientes.stream().min(Comparator.comparingDouble(l -> km(desde, l))).orElseThrow();
            pendientes.remove(sig);
            orden.add(sig);
            actual = sig;
        }
        if (fin != null) orden.add(fin);
        List<Integer> tramos = new ArrayList<>(List.of(0));
        for (int i = 1; i < orden.size(); i++) {
            tramos.add((int) Math.round(km(orden.get(i - 1), orden.get(i)) / KMH_CIUDAD * 60 + 3));
        }
        return new Resultado(orden, tramos, "ESTIMADA");
    }

    private static List<Integer> ceros(int n) {
        return new ArrayList<>(Collections.nCopies(n, 0));
    }

    private static double km(Lugar a, Lugar b) {
        double lat1 = Math.toRadians(a.getLatitud().doubleValue()), lat2 = Math.toRadians(b.getLatitud().doubleValue());
        double dLat = lat2 - lat1;
        double dLon = Math.toRadians(b.getLongitud().doubleValue() - a.getLongitud().doubleValue());
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 6371 * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
    }
}
