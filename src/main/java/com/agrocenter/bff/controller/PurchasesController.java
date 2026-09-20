package com.agrocenter.bff.controller;

import com.agrocenter.bff.client.PurchasesClient;
import com.agrocenter.bff.dto.purchases.CreatePurchaseRequest;
import com.agrocenter.bff.dto.purchases.PurchaseResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping({"/api/bff/compras", "/api/bff/admin/compras"})
@PreAuthorize("hasRole('ADMIN')")
public class PurchasesController {

    private final PurchasesClient purchasesClient;

    public PurchasesController(PurchasesClient purchasesClient) {
        this.purchasesClient = purchasesClient;
    }

    @PostMapping
    public ResponseEntity<PurchaseResponse> create(
            @Valid @RequestBody CreatePurchaseRequest request
    ) {
        PurchaseResponse purchase = purchasesClient.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(purchase.id())
                .toUri();
        return ResponseEntity.created(location).body(purchase);
    }

    @GetMapping
    public List<PurchaseResponse> list() {
        return purchasesClient.list();
    }

    @GetMapping("/{id}")
    public PurchaseResponse get(@PathVariable @Positive Long id) {
        return purchasesClient.get(id);
    }
}
