package pe.edu.upeu.lp2_gp2.dto;

// Respuesta del login y del registro. token va en "Authorization: Bearer <token>".
public record SesionDTO(
        Long usuarioId,
        Long clienteId,
        String username,
        String nombre,
        String rol,
        String estado,
        String token
) {
}
