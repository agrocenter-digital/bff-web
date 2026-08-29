package com.agrocenter.bff.dto.purchases;

import java.math.BigDecimal;

public record PurchaseDetailResponse(
        Long id,
        Long productoId,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}
