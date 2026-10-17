package com.agrocenter.bff.dto.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record StockOperationRequest(
        @NotNull Long productoId,
        @NotNull @Positive Integer cantidad,
        @NotBlank @Size(max = 100) String referencia
) {
}
