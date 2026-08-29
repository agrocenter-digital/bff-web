package com.agrocenter.bff.dto.inventory;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String sku,
        String nombre,
        String descripcion,
        String categoria,
        BigDecimal precioVenta,
        Integer stockActual,
        Integer stockMinimo,
        boolean stockBajo,
        boolean activo,
        Instant createdAt,
        Instant updatedAt
) {
}
