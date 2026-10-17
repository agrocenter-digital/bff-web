package com.agrocenter.bff.dto.inventory;

import java.time.Instant;

public record StockOperationResponse(
        Long movimientoId,
        Long productoId,
        String sku,
        String tipoMovimiento,
        Integer cantidad,
        Integer stockAnterior,
        Integer stockPosterior,
        String referencia,
        boolean duplicada,
        Instant fecha
) {
}
