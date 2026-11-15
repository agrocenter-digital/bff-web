package com.agrocenter.bff.controller;

import com.agrocenter.bff.client.ObservabilityClient;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bff/admin")
public class ObservabilityController {
    private static final Set<String> ANALYTICS = Set.of("resumen", "ventas", "productos", "b2b", "alertas-stock", "cuarentena", "estado-consumidor");
    private static final Set<String> AUDIT = Set.of("eventos", "cuarentena", "estado-consumidor");
    private static final Set<String> PARAMETROS = Set.of("desde", "hasta", "tipo", "productor", "correlacion", "pagina", "tamano");
    private final ObservabilityClient client;
    public ObservabilityController(ObservabilityClient client) { this.client = client; }

    @GetMapping("/analiticas/{recurso:resumen|ventas|productos|b2b|alertas-stock|cuarentena|estado-consumidor}")
    public JsonNode analytics(@PathVariable String recurso, @RequestParam Map<String, String> params) {
        validar(recurso, ANALYTICS, params); return client.analytics(recurso, params);
    }
    @GetMapping("/auditoria/{recurso:eventos|cuarentena|estado-consumidor}")
    public JsonNode audit(@PathVariable String recurso, @RequestParam Map<String, String> params) {
        validar(recurso, AUDIT, params); return client.audit(recurso, params);
    }
    @GetMapping("/auditoria/{recurso:eventos|cuarentena}/{id}")
    public JsonNode auditDetail(@PathVariable String recurso, @PathVariable String id) {
        validarId(id); return client.auditDetail(recurso, id);
    }
    @GetMapping("/analiticas/cuarentena/{id}")
    public JsonNode analyticsQuarantineDetail(@PathVariable String id) {
        validarId(id); return client.analyticsQuarantineDetail(id);
    }
    private void validar(String recurso, Set<String> recursos, Map<String, String> params) {
        if (!recursos.contains(recurso) || params.size() > PARAMETROS.size()
                || params.entrySet().stream().anyMatch(e -> !PARAMETROS.contains(e.getKey()) || e.getValue().length() > 100))
            throw new IllegalArgumentException("Parametros invalidos");
    }
    private void validarId(String id) {
        if (id.isBlank() || id.length() > 100 || id.contains("/") || id.contains("\\"))
            throw new IllegalArgumentException("Identificador invalido");
    }
}
