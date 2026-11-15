package com.agrocenter.bff.client;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Proxy de lectura limitado a rutas conocidas; no acepta URLs arbitrarias. */
@Component
public class ObservabilityClient {
    private static final Set<String> ANALYTICS = Set.of("resumen", "ventas", "productos", "b2b", "alertas-stock", "cuarentena", "estado-consumidor");
    private static final Set<String> AUDIT = Set.of("eventos", "cuarentena", "estado-consumidor");
    private final RestClient analytics;
    private final RestClient audit;
    private final ClientCallExecutor executor;

    public ObservabilityClient(@Qualifier("analyticsRestClient") RestClient analytics,
                               @Qualifier("auditRestClient") RestClient audit, ClientCallExecutor executor) {
        this.analytics = analytics; this.audit = audit; this.executor = executor;
    }

    public JsonNode analytics(String recurso, Map<String, String> params) {
        if (!ANALYTICS.contains(recurso)) throw new IllegalArgumentException("Recurso no permitido");
        return get(analytics, "ms-analiticas", "/api/analiticas/" + recurso, params);
    }
    public JsonNode audit(String recurso, Map<String, String> params) {
        if (!AUDIT.contains(recurso)) throw new IllegalArgumentException("Recurso no permitido");
        return get(audit, "ms-auditoria", "/api/auditoria/" + recurso, params);
    }
    public JsonNode auditDetail(String recurso, String id) {
        if (!Set.of("eventos", "cuarentena").contains(recurso)) throw new IllegalArgumentException("Recurso no permitido");
        return executor.execute("ms-auditoria", "consultar detalle", () -> audit.get()
                .uri(builder -> builder.path("/api/auditoria/" + recurso + "/{id}").build(id))
                .retrieve().body(JsonNode.class));
    }
    public JsonNode analyticsQuarantineDetail(String id) {
        return executor.execute("ms-analiticas", "consultar cuarentena", () -> analytics.get()
                .uri(builder -> builder.path("/api/analiticas/cuarentena/{id}").build(id))
                .retrieve().body(JsonNode.class));
    }
    private JsonNode get(RestClient client, String service, String path, Map<String, String> params) {
        return executor.execute(service, "consultar", () -> client.get().uri(builder -> {
            builder.path(path);
            params.forEach(builder::queryParam);
            return builder.build();
        }).retrieve().body(JsonNode.class));
    }
}
