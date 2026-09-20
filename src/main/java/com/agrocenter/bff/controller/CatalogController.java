package com.agrocenter.bff.controller;

import com.agrocenter.bff.client.InventoryClient;
import com.agrocenter.bff.dto.inventory.ProductResponse;
import com.agrocenter.bff.dto.inventory.ProductStockResponse;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/bff")
public class CatalogController {

    private final InventoryClient inventoryClient;

    public CatalogController(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    @GetMapping("/catalogo")
    public List<ProductResponse> catalog(
            @RequestParam(required = false) @Size(max = 80) String categoria,
            @RequestParam(required = false) @Size(max = 120) String nombre
    ) {
        return inventoryClient.listProducts(categoria, nombre, true);
    }

    @GetMapping("/productos/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ProductResponse getProduct(@PathVariable @Positive Long id) {
        return inventoryClient.getProduct(id);
    }

    @GetMapping("/productos/sku/{sku}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ProductResponse getProductBySku(@PathVariable @Size(max = 50) String sku) {
        return inventoryClient.getProductBySku(sku);
    }

    @GetMapping("/productos/{id}/stock")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ProductStockResponse getStock(@PathVariable @Positive Long id) {
        return inventoryClient.getStock(id);
    }
}
