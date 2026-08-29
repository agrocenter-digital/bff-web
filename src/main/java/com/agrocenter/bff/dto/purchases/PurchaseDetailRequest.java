package com.agrocenter.bff.dto.purchases;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PurchaseDetailRequest(
        @NotNull @Positive Long productoId,
        @NotNull @Positive Integer cantidad,
        @NotNull @Positive BigDecimal precioUnitario
) {
}
