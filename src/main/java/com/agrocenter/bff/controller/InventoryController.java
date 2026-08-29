package com.agrocenter.bff.controller;

import com.agrocenter.bff.client.InventoryClient;
import com.agrocenter.bff.dto.common.PageResponse;
import com.agrocenter.bff.dto.inventory.InventoryMovementResponse;
import com.agrocenter.bff.dto.inventory.ProductCreateRequest;
import com.agrocenter.bff.dto.inventory.ProductResponse;
import com.agrocenter.bff.dto.inventory.ProductStateRequest;
import com.agrocenter.bff.dto.inventory.ProductUpdateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/bff/inventario")
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {

    private final InventoryClient inventoryClient;

    public InventoryController(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    @GetMapping
    public List<ProductResponse> inventory(
            @RequestParam(required = false) @Size(max = 80) String categoria,
            @RequestParam(required = false) @Size(max = 120) String nombre,
            @RequestParam(required = false) Boolean activo
    ) {
        return inventoryClient.listProducts(categoria, nombre, activo);
    }

    @GetMapping("/stock-bajo")
    public List<ProductResponse> lowStock(@RequestParam(required = false) Boolean activo) {
        return inventoryClient.listProducts(null, null, activo).stream()
                .filter(ProductResponse::stockBajo)
                .toList();
    }

    @PostMapping("/productos")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        ResponseEntity<ProductResponse> downstream = inventoryClient.createProduct(request);
        ProductResponse product = downstream.getBody();
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(product.id())
                .toUri();
        return ResponseEntity.created(location).body(product);
    }

    @PutMapping("/productos/{id}")
    public ProductResponse updateProduct(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return inventoryClient.updateProduct(id, request);
    }

    @PatchMapping("/productos/{id}/estado")
    public ProductResponse changeProductState(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ProductStateRequest request
    ) {
        return inventoryClient.changeProductState(id, request);
    }

    @GetMapping("/movimientos")
    public PageResponse<InventoryMovementResponse> movements(
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanio
    ) {
        return inventoryClient.listMovements(pagina, tamanio);
    }

    @GetMapping("/productos/{id}/movimientos")
    public PageResponse<InventoryMovementResponse> productMovements(
            @PathVariable @Positive Long id,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanio
    ) {
        return inventoryClient.listProductMovements(id, pagina, tamanio);
    }
}
