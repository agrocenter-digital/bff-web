package com.agrocenter.bff.dto.consultas;

import java.time.Instant;

public record ConsultaB2BResponse(
        Long id,
        String skuProducto,
        String nombreProducto,
        Integer cantidad,
        String nombreCliente,
        String correoContacto,
        String mensaje,
        String origen,
        String estado,
        Instant fechaCreacion
) {
}
