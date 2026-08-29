package com.agrocenter.bff.service;

import com.agrocenter.bff.client.InventoryClient;
import com.agrocenter.bff.client.PurchasesClient;
import com.agrocenter.bff.client.SalesClient;
import com.agrocenter.bff.dto.common.PageResponse;
import com.agrocenter.bff.dto.dashboard.AdminDashboardResponse;
import com.agrocenter.bff.dto.inventory.ProductResponse;
import com.agrocenter.bff.dto.purchases.PurchaseResponse;
import com.agrocenter.bff.dto.sales.SaleResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class DashboardService {

    private final InventoryClient inventoryClient;
    private final SalesClient salesClient;
    private final PurchasesClient purchasesClient;

    public DashboardService(
            InventoryClient inventoryClient,
            SalesClient salesClient,
            PurchasesClient purchasesClient
    ) {
        this.inventoryClient = inventoryClient;
        this.salesClient = salesClient;
        this.purchasesClient = purchasesClient;
    }

    public AdminDashboardResponse getAdminDashboard() {
        List<ProductResponse> products = inventoryClient.listProducts(null, null, null);
        PageResponse<SaleResponse> sales = salesClient.listAll(0, 1);
        List<PurchaseResponse> purchases = purchasesClient.list();

        long lowStock = products.stream().filter(ProductResponse::stockBajo).count();
        return new AdminDashboardResponse(
                Instant.now(),
                new AdminDashboardResponse.InventorySummary(products.size(), lowStock),
                new AdminDashboardResponse.SalesSummary(sales.totalElementos()),
                new AdminDashboardResponse.PurchasesSummary(purchases.size())
        );
    }
}
