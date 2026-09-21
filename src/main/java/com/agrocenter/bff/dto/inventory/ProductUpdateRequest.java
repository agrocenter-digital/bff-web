package com.agrocenter.bff.dto.inventory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductUpdateRequest(
        @NotBlank @Size(max = 50) String sku,
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 500) String descripcion,
        @NotBlank @Size(max = 80) String categoria,
        @NotNull @DecimalMin("0.00") @Digits(integer = 13, fraction = 2) BigDecimal precioVenta,
        @NotNull @PositiveOrZero Integer stockMinimo,
        @PositiveOrZero Integer stockActual
) {
}
