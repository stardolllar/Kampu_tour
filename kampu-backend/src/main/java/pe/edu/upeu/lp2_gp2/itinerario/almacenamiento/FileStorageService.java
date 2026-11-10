package pe.edu.upeu.lp2_gp2.itinerario.almacenamiento;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

/**
 * Almacenamiento seguro de los PDF de boletos aéreos (boarding pass / e-ticket).
 *
 * Seguridad:
 *  - Tamaño máximo propio (kampu.boletos.max-bytes, 5 MB). El límite global de Spring
 *    (spring.servlet.multipart.max-file-size) lanza MaxUploadSizeExceededException antes de
 *    llegar aquí; GlobalExceptionHandler la convierte en 413.
 *  - Solo PDF: extensión .pdf, tipo MIME declarado y FIRMA REAL del contenido ("%PDF-"),
 *    así un .exe renombrado a .pdf no pasa.
 *  - El nombre del usuario NUNCA se usa como ruta: se guarda como <año>/<mes>/<uuid>.pdf.
 *  - Toda ruta se normaliza y debe quedar dentro de la carpeta base (sin "../").
 *  - En la BD se guarda la ruta relativa: la carpeta base puede moverse sin migrar datos.
 */
@Service
public class FileStorageService {
    private static final byte[] FIRMA_PDF = "%PDF-".getBytes();

    private final Path base;
    private final long maxBytes;

    public FileStorageService(@Value("${kampu.boletos.directorio:uploads/boletos}") String directorio,
                              @Value("${kampu.boletos.max-bytes:5242880}") long maxBytes) {
        this.base = Path.of(directorio).toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
        try {
            Files.createDirectories(base);
        } catch (IOException e) {
            throw new AlmacenamientoException("No se pudo crear la carpeta de boletos: " + base, e);
        }
    }

    public long getMaxBytes() {
        return maxBytes;
    }

    /** Valida y guarda el PDF. Devuelve la ruta relativa para la BD. */
    public ArchivoGuardado guardarPdf(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ArchivoInvalidoException("Adjunta el PDF del boleto (boarding pass o e-ticket).");
        }
        if (archivo.getSize() > maxBytes) {
            throw new ArchivoInvalidoException("El PDF pesa " + mb(archivo.getSize()) + " MB; el máximo es "
                    + mb(maxBytes) + " MB.", true);
        }
        String nombre = nombreSeguro(archivo.getOriginalFilename());
        if (!nombre.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new ArchivoInvalidoException("Solo se aceptan archivos .pdf.");
        }
        String tipo = archivo.getContentType();
        if (tipo != null && !tipo.equalsIgnoreCase("application/pdf") && !tipo.equalsIgnoreCase("application/octet-stream")) {
            throw new ArchivoInvalidoException("El archivo no es un PDF (tipo " + tipo + ").");
        }
        if (!empiezaComoPdf(archivo)) {
            throw new ArchivoInvalidoException("El contenido del archivo no es un PDF válido.");
        }

        LocalDate hoy = LocalDate.now();
        Path relativa = Path.of(String.valueOf(hoy.getYear()), String.format("%02d", hoy.getMonthValue()),
                UUID.randomUUID() + ".pdf");
        Path destino = resolver(relativa.toString());
        try {
            Files.createDirectories(destino.getParent());
            try (InputStream in = archivo.getInputStream()) {
                Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new AlmacenamientoException("No se pudo guardar el PDF.", e);
        }
        return new ArchivoGuardado(relativa.toString().replace('\\', '/'), nombre, archivo.getSize());
    }

    /** Archivo para descargar (404 si ya no existe en disco) */
    public Resource cargar(String rutaRelativa) {
        Path ruta = resolver(rutaRelativa);
        try {
            Resource r = new UrlResource(ruta.toUri());
            if (!r.exists() || !r.isReadable()) {
                throw new AlmacenamientoException("El PDF del boleto ya no está disponible en el servidor.");
            }
            return r;
        } catch (MalformedURLException e) {
            throw new AlmacenamientoException("Ruta de archivo inválida.", e);
        }
    }

    /** Borra un archivo reemplazado (si falla, solo queda huérfano: no se interrumpe la operación) */
    public void eliminar(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.isBlank()) return;
        try {
            Files.deleteIfExists(resolver(rutaRelativa));
        } catch (IOException | AlmacenamientoException e) {
            // archivo huérfano: no afecta a los datos
        }
    }

    // Ruta absoluta dentro de la carpeta base; rechaza cualquier intento de salir de ella
    private Path resolver(String rutaRelativa) {
        Path p = base.resolve(rutaRelativa).normalize();
        if (!p.startsWith(base)) {
            throw new AlmacenamientoException("Ruta de archivo no permitida.");
        }
        return p;
    }

    private static boolean empiezaComoPdf(MultipartFile archivo) {
        try (InputStream in = archivo.getInputStream()) {
            byte[] cabecera = in.readNBytes(FIRMA_PDF.length);
            return Arrays.equals(cabecera, FIRMA_PDF);
        } catch (IOException e) {
            return false;
        }
    }

    // Solo el nombre (sin carpetas) y sin caracteres raros; se guarda para mostrarlo/descargarlo
    private static String nombreSeguro(String original) {
        String n = original == null ? "boleto.pdf" : original.replace('\\', '/');
        n = n.substring(n.lastIndexOf('/') + 1).replaceAll("[^A-Za-z0-9._ -]", "_").trim();
        if (n.isEmpty() || n.startsWith(".")) n = "boleto.pdf";
        return n.length() > 200 ? n.substring(n.length() - 200) : n;
    }

    private static String mb(long bytes) {
        return String.format(Locale.ROOT, "%.1f", bytes / 1024.0 / 1024.0);
    }
}
