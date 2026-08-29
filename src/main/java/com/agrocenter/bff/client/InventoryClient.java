package com.agrocenter.bff.client;

import com.agrocenter.bff.dto.common.PageResponse;
import com.agrocenter.bff.dto.inventory.InventoryMovementResponse;
import com.agrocenter.bff.dto.inventory.ProductCreateRequest;
import com.agrocenter.bff.dto.inventory.ProductResponse;
import com.agrocenter.bff.dto.inventory.ProductStateRequest;
import com.agrocenter.bff.dto.inventory.ProductStockResponse;
import com.agrocenter.bff.dto.inventory.ProductUpdateRequest;
import com.agrocenter.bff.exception.DownstreamException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class InventoryClient {

    private static final ParameterizedTypeReference<List<ProductResponse>> PRODUCT_LIST_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<PageResponse<InventoryMovementResponse>> MOVEMENT_PAGE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;
    private final ClientCallExecutor executor;

    public InventoryClient(
            @Qualifier("inventoryRestClient") RestClient restClient,
            ClientCallExecutor executor
    ) {
        this.restClient = restClient;
        this.executor = executor;
    }

    public List<ProductResponse> listProducts(String categoria, String nombre, Boolean activo) {
        return executor.execute("ms-inventario", "listar productos", () ->
                restClient.get()
                        .uri(builder -> {
                            builder.path("/api/inventario/productos");
                            if (categoria != null) {
                                builder.queryParam("categoria", categoria);
                            }
                            if (nombre != null) {
                                builder.queryParam("nombre", nombre);
                            }
                            if (activo != null) {
                                builder.queryParam("activo", activo);
                            }
                            return builder.build();
                        })
                        .retrieve()
                        .body(PRODUCT_LIST_TYPE)
        );
    }

    public ProductResponse getProduct(Long id) {
        return executor.execute("ms-inventario", "obtener producto", () ->
                restClient.get()
                        .uri("/api/inventario/productos/{id}", id)
                        .retrieve()
                        .body(ProductResponse.class)
        );
    }

    public ProductResponse getProductBySku(String sku) {
        return executor.execute("ms-inventario", "obtener producto por SKU", () ->
                restClient.get()
                        .uri("/api/inventario/productos/sku/{sku}", sku)
                        .retrieve()
                        .body(ProductResponse.class)
        );
    }

    public ProductStockResponse getStock(Long id) {
        return executor.execute("ms-inventario", "consultar stock", () ->
                restClient.get()
                        .uri("/api/inventario/productos/{id}/stock", id)
                        .retrieve()
                        .body(ProductStockResponse.class)
        );
    }

    public ResponseEntity<ProductResponse> createProduct(ProductCreateRequest request) {
        ResponseEntity<ProductResponse> response = executor.execute("ms-inventario", "crear producto", () ->
                restClient.post()
                        .uri("/api/inventario/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .toEntity(ProductResponse.class)
        );
        if (response.getBody() == null) {
            throw DownstreamException.badGateway("ms-inventario");
        }
        return response;
    }

    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        return executor.execute("ms-inventario", "actualizar producto", () ->
                restClient.put()
                        .uri("/api/inventario/productos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(ProductResponse.class)
        );
    }

    public ProductResponse changeProductState(Long id, ProductStateRequest request) {
        return executor.execute("ms-inventario", "cambiar estado de producto", () ->
                restClient.patch()
                        .uri("/api/inventario/productos/{id}/estado", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(ProductResponse.class)
        );
    }

    public PageResponse<InventoryMovementResponse> listMovements(int page, int size) {
        return executor.execute("ms-inventario", "listar movimientos", () ->
                restClient.get()
                        .uri(builder -> builder
                                .path("/api/inventario/movimientos")
                                .queryParam("pagina", page)
                                .queryParam("tamanio", size)
                                .build())
                        .retrieve()
                        .body(MOVEMENT_PAGE_TYPE)
        );
    }

    public PageResponse<InventoryMovementResponse> listProductMovements(Long id, int page, int size) {
        return executor.execute("ms-inventario", "listar movimientos de producto", () ->
                restClient.get()
                        .uri(builder -> builder
                                .path("/api/inventario/productos/{id}/movimientos")
                                .queryParam("pagina", page)
                                .queryParam("tamanio", size)
                                .build(id))
                        .retrieve()
                        .body(MOVEMENT_PAGE_TYPE)
        );
    }
}
