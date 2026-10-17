package com.agrocenter.bff.dto.purchases;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ConfirmarPurchaseRequest(
        @Positive Long proveedorId,
        @Positive Integer cantidad,
        @DecimalMin("0.00") BigDecimal montoTotal,
        @Size(max = 100) List<@NotNull @Valid PurchaseDetailRequest> detalles
) {
}
