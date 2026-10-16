package com.agrocenter.bff.client;

import com.agrocenter.bff.dto.messaging.EventTraceResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class MessagingClient {

    private static final ParameterizedTypeReference<List<EventTraceResponse>> TRACE_LIST_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient inventoryRestClient;
    private final RestClient salesRestClient;
    private final RestClient purchasesRestClient;
    private final ClientCallExecutor executor;

    public MessagingClient(
            @Qualifier("inventoryRestClient") RestClient inventoryRestClient,
            @Qualifier("salesRestClient") RestClient salesRestClient,
            @Qualifier("purchasesRestClient") RestClient purchasesRestClient,
            ClientCallExecutor executor
    ) {
        this.inventoryRestClient = inventoryRestClient;
        this.salesRestClient = salesRestClient;
        this.purchasesRestClient = purchasesRestClient;
        this.executor = executor;
    }

    public List<EventTraceResponse> trazasInventario(int limite) {
        return fetch(
                inventoryRestClient,
                "ms-inventario",
                "/api/inventario/admin/mensajeria/eventos?limite=" + limite
        );
    }

    public List<EventTraceResponse> trazasVentas(int limite) {
        return fetch(
                salesRestClient,
                "ms-ventas",
                "/api/ventas/admin/mensajeria/eventos?limite=" + limite
        );
    }

    public List<EventTraceResponse> trazasCompras(int limite) {
        return fetch(
                purchasesRestClient,
                "ms-compras",
                "/api/compras/admin/mensajeria/eventos?limite=" + limite
        );
    }

    private List<EventTraceResponse> fetch(RestClient restClient, String service, String uri) {
        List<EventTraceResponse> resultado = executor.execute(service, "trazas de mensajeria", () ->
                restClient.get()
                        .uri(uri)
                        .retrieve()
                        .body(TRACE_LIST_TYPE)
        );
        return resultado == null ? List.of() : resultado;
    }
}
