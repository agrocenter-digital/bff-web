package com.agrocenter.bff.dto.purchases;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseResponse(
        Long id,
        Long proveedorId,
        LocalDateTime fechaCreacion,
        String estado,
        BigDecimal total,
        List<PurchaseDetailResponse> detalles
) {
}
