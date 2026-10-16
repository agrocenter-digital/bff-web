package com.agrocenter.bff.dto.messaging;

import java.time.Instant;

public record RoutingRuleResponse(
        String id,
        String nombre,
        String topicoOrigen,
        String topicoDestino,
        String condicion,
        String estado,
        String descripcion,
        Instant fechaCreacion
) {
}
