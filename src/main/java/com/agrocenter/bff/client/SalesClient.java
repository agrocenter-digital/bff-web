package com.agrocenter.bff.client;

import com.agrocenter.bff.dto.common.PageResponse;
import com.agrocenter.bff.dto.sales.CreateSaleRequest;
import com.agrocenter.bff.dto.sales.SaleCreationResponse;
import com.agrocenter.bff.dto.sales.SaleResponse;
import com.agrocenter.bff.exception.DownstreamException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SalesClient {

    private static final ParameterizedTypeReference<PageResponse<SaleResponse>> SALE_PAGE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;
    private final ClientCallExecutor executor;

    public SalesClient(
            @Qualifier("salesRestClient") RestClient restClient,
            ClientCallExecutor executor
    ) {
        this.restClient = restClient;
        this.executor = executor;
    }

    public SaleCreationResponse create(String idempotencyKey, CreateSaleRequest request) {
        ResponseEntity<SaleResponse> response = executor.execute("ms-ventas", "crear venta", () ->
                restClient.post()
                        .uri("/api/ventas")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .toEntity(SaleResponse.class)
        );
        if (response.getBody() == null) {
            throw DownstreamException.badGateway("ms-ventas");
        }
        boolean replay = "true".equalsIgnoreCase(response.getHeaders().getFirst("Idempotent-Replay"));
        return new SaleCreationResponse(response.getBody(), replay);
    }

    public SaleResponse get(Long id) {
        return executor.execute("ms-ventas", "obtener venta", () ->
                restClient.get()
                        .uri("/api/ventas/{id}", id)
                        .retrieve()
                        .body(SaleResponse.class)
        );
    }

    public PageResponse<SaleResponse> listMine(int page, int size) {
        return list("/api/ventas/mis-pedidos", page, size, "listar pedidos propios");
    }

    public PageResponse<SaleResponse> listAll(int page, int size) {
        return list("/api/ventas", page, size, "listar ventas");
    }

    private PageResponse<SaleResponse> list(String path, int page, int size, String operation) {
        return executor.execute("ms-ventas", operation, () ->
                restClient.get()
                        .uri(builder -> builder
                                .path(path)
                                .queryParam("pagina", page)
                                .queryParam("tamanio", size)
                                .build())
                        .retrieve()
                        .body(SALE_PAGE_TYPE)
        );
    }
}
