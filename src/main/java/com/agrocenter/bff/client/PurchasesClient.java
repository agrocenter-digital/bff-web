package com.agrocenter.bff.client;

import com.agrocenter.bff.dto.purchases.CreatePurchaseRequest;
import com.agrocenter.bff.dto.purchases.PurchaseResponse;
import com.agrocenter.bff.exception.DownstreamException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class PurchasesClient {

    private static final ParameterizedTypeReference<List<PurchaseResponse>> PURCHASE_LIST_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;
    private final ClientCallExecutor executor;

    public PurchasesClient(
            @Qualifier("purchasesRestClient") RestClient restClient,
            ClientCallExecutor executor
    ) {
        this.restClient = restClient;
        this.executor = executor;
    }

    public PurchaseResponse create(CreatePurchaseRequest request) {
        ResponseEntity<PurchaseResponse> response = executor.execute("ms-compras", "crear compra", () ->
                restClient.post()
                        .uri("/api/compras/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .toEntity(PurchaseResponse.class)
        );
        if (response.getBody() == null) {
            throw DownstreamException.badGateway("ms-compras");
        }
        return response.getBody();
    }

    public List<PurchaseResponse> list() {
        return executor.execute("ms-compras", "listar compras", () ->
                restClient.get()
                        .uri("/api/compras/")
                        .retrieve()
                        .body(PURCHASE_LIST_TYPE)
        );
    }

    public PurchaseResponse get(Long id) {
        return executor.execute("ms-compras", "obtener compra", () ->
                restClient.get()
                        .uri("/api/compras/{id}", id)
                        .retrieve()
                        .body(PurchaseResponse.class)
        );
    }
}
