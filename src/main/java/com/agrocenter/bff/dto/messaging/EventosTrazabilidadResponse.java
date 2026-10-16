package com.agrocenter.bff.dto.messaging;

import java.util.List;

public record EventosTrazabilidadResponse(
        List<EventTraceResponse> eventos,
        List<String> advertencias
) {
}
