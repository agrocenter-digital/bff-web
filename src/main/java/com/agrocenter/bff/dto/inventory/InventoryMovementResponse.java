package com.agrocenter.bff.dto.inventory;

import java.time.Instant;

public record InventoryMovementResponse(
        Long id,
        Long productoId,
        String sku,
        String tipoMovimiento,
        Integer cantidad,
        Integer stockAnterior,
        Integer stockPosterior,
        String referencia,
        String origen,
        Instant fecha
) {
}
