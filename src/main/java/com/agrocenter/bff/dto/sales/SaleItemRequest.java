package com.agrocenter.bff.dto.sales;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SaleItemRequest(
        @NotNull @Positive Long productoId,
        @NotNull @Positive Integer cantidad
) {
}
