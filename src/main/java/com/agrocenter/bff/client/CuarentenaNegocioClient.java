package com.agrocenter.bff.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CuarentenaNegocioClient {
    private final RestClient inventario;
    private final RestClient compras;
    private final ClientCallExecutor executor;

    public CuarentenaNegocioClient(@Qualifier("inventoryRestClient") RestClient inventario,
                                   @Qualifier("purchasesRestClient") RestClient compras, ClientCallExecutor executor) {
        this.inventario = inventario; this.compras = compras; this.executor = executor;
    }

    public JsonNode consultar(boolean esInventario, int pagina, int tamano, boolean pendientes) {
        RestClient client = esInventario ? inventario : compras;
        String servicio = esInventario ? "ms-inventario" : "ms-compras";
        String path = esInventario ? "/api/inventario/admin/cuarentena" : "/api/compras/admin/cuarentena";
        return executor.execute(servicio, "consultar cuarentena", () -> client.get().uri(builder -> builder.path(path)
                .queryParam("pagina", pagina).queryParam("tamano", tamano).queryParam("pendientes", pendientes).build())
                .retrieve().body(JsonNode.class));
    }
}
