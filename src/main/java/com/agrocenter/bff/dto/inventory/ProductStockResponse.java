package com.agrocenter.bff.dto.inventory;

public record ProductStockResponse(
        Long productoId,
        String sku,
        String nombre,
        Integer stockActual,
        Integer stockMinimo,
        boolean stockBajo
) {
}
