package com.agrocenter.bff.service;

import com.agrocenter.bff.config.MessagingProperties;
import com.agrocenter.bff.dto.messaging.RoutingRuleRequest;
import com.agrocenter.bff.dto.messaging.RoutingRuleResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReglasEncaminamientoService {

    private final Map<String, RoutingRuleResponse> reglas = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();
    private final MessagingProperties properties;

    public ReglasEncaminamientoService(MessagingProperties properties) {
        this.properties = properties;
    }

    public List<RoutingRuleResponse> listar() {
        return reglas.values().stream()
                .sorted(Comparator.comparing(RoutingRuleResponse::fechaCreacion).reversed())
                .toList();
    }

    public RoutingRuleResponse crear(RoutingRuleRequest request) {
        String origen = request.topicoOrigen().trim();
        if (!properties.topicos().contains(origen)) {
            throw new IllegalArgumentException(
                    "El topico de origen '" + origen + "' no es un topico conocido del sistema"
            );
        }

        String destino = normalizar(request.topicoDestino());
        String estado = request.estado() == null || request.estado().isBlank() ? "ACTIVA" : request.estado();

        String id = "REGLA-" + secuencia.incrementAndGet();
        RoutingRuleResponse regla = new RoutingRuleResponse(
                id,
                request.nombre().trim(),
                origen,
                destino,
                normalizar(request.condicion()),
                estado,
                normalizar(request.descripcion()),
                Instant.now()
        );
        reglas.put(id, regla);
        return regla;
    }

    public boolean eliminar(String id) {
        return reglas.remove(id) != null;
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
