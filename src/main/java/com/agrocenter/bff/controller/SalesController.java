package com.agrocenter.bff.controller;

import com.agrocenter.bff.client.SalesClient;
import com.agrocenter.bff.dto.common.PageResponse;
import com.agrocenter.bff.dto.sales.CreateSaleRequest;
import com.agrocenter.bff.dto.sales.SaleCreationResponse;
import com.agrocenter.bff.dto.sales.SaleResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Validated
@RestController
@RequestMapping("/api/bff")
public class SalesController {

    private final SalesClient salesClient;

    public SalesController(SalesClient salesClient) {
        this.salesClient = salesClient;
    }

    @PostMapping("/ventas")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<SaleResponse> create(
            @RequestHeader("Idempotency-Key") @NotBlank @Size(max = 100) String idempotencyKey,
            @Valid @RequestBody CreateSaleRequest request
    ) {
        SaleCreationResponse result = salesClient.create(idempotencyKey, request);
        if (result.replay()) {
            return ResponseEntity.ok()
                    .header("Idempotent-Replay", "true")
                    .body(result.sale());
        }
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.sale().id())
                .toUri();
        return ResponseEntity.created(location).body(result.sale());
    }

    @GetMapping("/ventas/mis-pedidos")
    @PreAuthorize("hasRole('CLIENTE')")
    public PageResponse<SaleResponse> mine(
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanio
    ) {
        return salesClient.listMine(pagina, tamanio);
    }

    @GetMapping("/ventas/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public SaleResponse get(@PathVariable @Positive Long id) {
        return salesClient.get(id);
    }

    @GetMapping({"/admin/ventas", "/ventas"})
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<SaleResponse> all(
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanio
    ) {
        return salesClient.listAll(pagina, tamanio);
    }
}
