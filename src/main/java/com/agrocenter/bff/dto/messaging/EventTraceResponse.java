package com.agrocenter.bff.dto.messaging;

import java.time.Instant;

public record EventTraceResponse(
        long id,
        Instant fecha,
        String servicio,
        String topico,
        String direccion,
        String clave,
        String estado,
        String detalle
) {
}
