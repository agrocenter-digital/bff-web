package com.agrocenter.bff.dto.dashboard;

import java.time.Instant;

public record AdminDashboardResponse(
        Instant generadoEn,
        InventorySummary inventario,
        SalesSummary ventas,
        PurchasesSummary compras
) {
    public record InventorySummary(long productos, long productosStockBajo) {
    }

    public record SalesSummary(long ventasRegistradas) {
    }

    public record PurchasesSummary(long comprasRegistradas) {
    }
}
