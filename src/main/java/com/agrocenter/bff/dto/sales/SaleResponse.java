package com.agrocenter.bff.dto.sales;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SaleResponse(
        Long id,
        String clienteId,
        Instant fechaCreacion,
        String estado,
        BigDecimal subtotal,
        BigDecimal total,
        String motivoCancelacion,
        List<SaleDetailResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
}
