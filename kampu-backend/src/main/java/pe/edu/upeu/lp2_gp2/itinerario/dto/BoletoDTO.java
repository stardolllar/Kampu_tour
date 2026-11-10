package pe.edu.upeu.lp2_gp2.itinerario.dto;

import pe.edu.upeu.lp2_gp2.itinerario.entity.BoletoAereo;

import java.time.LocalDateTime;

// Sin la ruta interna del archivo: el PDF se descarga por su endpoint
public record BoletoDTO(
        Long boletoId, Long reservaId, String tipoVuelo, String aerolinea, String numeroVuelo, String codigoPnr,
        String aeropuertoOrigen, String aeropuertoDestino, LocalDateTime fechaHoraSalida, LocalDateTime fechaHoraLlegada,
        boolean tienePdf, String nombreArchivo, Long tamanioBytes, String registradoPor, LocalDateTime fechaRegistro
) {
    public static BoletoDTO desde(BoletoAereo b) {
        return new BoletoDTO(b.getBoletoId(), b.getReservaMaster().getReservaId(), b.getTipoVuelo().name(),
                b.getAerolinea().name(), b.getNumeroVuelo(), b.getCodigoPnr(), b.getAeropuertoOrigen(),
                b.getAeropuertoDestino(), b.getFechaHoraSalida(), b.getFechaHoraLlegada(), b.getArchivoPdf() != null,
                b.getNombreArchivo(), b.getTamanioBytes(), b.getRegistradoPor(), b.getFechaRegistro());
    }
}
