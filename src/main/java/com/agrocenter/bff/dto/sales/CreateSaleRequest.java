package com.agrocenter.bff.dto.sales;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateSaleRequest(
        @NotEmpty @Size(max = 50) List<@NotNull @Valid SaleItemRequest> items
) {
}
