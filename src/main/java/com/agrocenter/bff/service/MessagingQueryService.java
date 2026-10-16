package com.agrocenter.bff.service;

import com.agrocenter.bff.client.MessagingClient;
import com.agrocenter.bff.dto.messaging.EventTraceResponse;
import com.agrocenter.bff.dto.messaging.EventosTrazabilidadResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

@Service
public class MessagingQueryService {

    private final MessagingClient messagingClient;

    public MessagingQueryService(MessagingClient messagingClient) {
        this.messagingClient = messagingClient;
    }

    public EventosTrazabilidadResponse eventos(int limite) {
        List<EventTraceResponse> eventos = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();

        recolectar(advertencias, eventos, "ms-ventas", () -> messagingClient.trazasVentas(limite));
        recolectar(advertencias, eventos, "ms-inventario", () -> messagingClient.trazasInventario(limite));
        recolectar(advertencias, eventos, "ms-compras", () -> messagingClient.trazasCompras(limite));

        List<EventTraceResponse> ordenados = eventos.stream()
                .sorted(Comparator.comparing(
                        EventTraceResponse::fecha,
                        Comparator.nullsLast(Comparator.<Instant>naturalOrder())
                ).reversed())
                .limit(limite)
                .toList();

        return new EventosTrazabilidadResponse(ordenados, advertencias);
    }

    private void recolectar(
            List<String> advertencias,
            List<EventTraceResponse> destino,
            String servicio,
            Supplier<List<EventTraceResponse>> proveedor
    ) {
        try {
            destino.addAll(proveedor.get());
        } catch (RuntimeException exception) {
            advertencias.add(servicio + ": " + exception.getMessage());
        }
    }
}
