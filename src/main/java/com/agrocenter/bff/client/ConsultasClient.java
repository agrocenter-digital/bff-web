package com.agrocenter.bff.client;

import com.agrocenter.bff.dto.consultas.ActualizarEstadoConsultaRequest;
import com.agrocenter.bff.dto.consultas.ConsultaB2BRequest;
import com.agrocenter.bff.dto.consultas.ConsultaB2BResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class ConsultasClient {

    private static final ParameterizedTypeReference<List<ConsultaB2BResponse>> CONSULTAS_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;
    private final ClientCallExecutor executor;

    public ConsultasClient(
            @Qualifier("salesRestClient") RestClient restClient,
            ClientCallExecutor executor
    ) {
        this.restClient = restClient;
        this.executor = executor;
    }

    public ConsultaB2BResponse crear(ConsultaB2BRequest request) {
        return executor.execute("ms-ventas", "crear consulta B2B", () ->
                restClient.post()
                        .uri("/api/ventas/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(ConsultaB2BResponse.class)
        );
    }

    public List<ConsultaB2BResponse> listar(String estado) {
        return executor.execute("ms-ventas", "listar consultas B2B", () ->
                restClient.get()
                        .uri(builder -> {
                            builder.path("/api/ventas/consultas");
                            if (estado != null && !estado.isBlank()) {
                                builder.queryParam("estado", estado);
                            }
                            return builder.build();
                        })
                        .retrieve()
                        .body(CONSULTAS_TYPE)
        );
    }

    public ConsultaB2BResponse actualizarEstado(Long id, ActualizarEstadoConsultaRequest request) {
        return executor.execute("ms-ventas", "actualizar estado consulta B2B", () ->
                restClient.patch()
                        .uri("/api/ventas/consultas/{id}/estado", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(ConsultaB2BResponse.class)
        );
    }
}
