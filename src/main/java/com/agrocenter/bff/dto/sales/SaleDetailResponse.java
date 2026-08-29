package com.agrocenter.bff.dto.sales;

import java.math.BigDecimal;

public record SaleDetailResponse(
        Long id,
        Long productoId,
        String sku,
        String nombreProducto,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}
