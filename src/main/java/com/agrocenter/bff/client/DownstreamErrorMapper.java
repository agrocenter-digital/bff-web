package com.agrocenter.bff.client;

import com.agrocenter.bff.exception.DownstreamException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class DownstreamErrorMapper {

    private final ObjectMapper objectMapper;

    public DownstreamErrorMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void handle(
            String service,
            HttpRequest request,
            ClientHttpResponse response
    ) throws IOException {
        int originalStatus = response.getStatusCode().value();
        String downstreamMessage = safeMessage(response);

        if (originalStatus == HttpStatus.BAD_REQUEST.value()) {
            throw mapped(HttpStatus.BAD_REQUEST, "DOWNSTREAM_BAD_REQUEST", downstreamMessage,
                    "El servicio rechazo los datos enviados", service);
        }
        if (originalStatus == HttpStatus.NOT_FOUND.value()) {
            throw mapped(HttpStatus.NOT_FOUND, "NOT_FOUND", downstreamMessage,
                    "El recurso solicitado no existe", service);
        }
        if (originalStatus == HttpStatus.CONFLICT.value()) {
            throw mapped(HttpStatus.CONFLICT, "CONFLICT", downstreamMessage,
                    "La operacion entra en conflicto con el estado actual", service);
        }
        if (originalStatus == HttpStatus.SERVICE_UNAVAILABLE.value()) {
            throw DownstreamException.unavailable(service);
        }
        if (originalStatus == HttpStatus.UNAUTHORIZED.value()
                || originalStatus == HttpStatus.FORBIDDEN.value()) {
            throw new DownstreamException(
                    HttpStatus.BAD_GATEWAY,
                    "DOWNSTREAM_SECURITY_MISMATCH",
                    "La autenticacion entre el BFF y " + service + " fue rechazada",
                    service
            );
        }
        throw DownstreamException.badGateway(service);
    }

    private DownstreamException mapped(
            HttpStatus status,
            String code,
            String downstreamMessage,
            String fallback,
            String service
    ) {
        return new DownstreamException(
                status,
                code,
                downstreamMessage == null ? fallback : downstreamMessage,
                service
        );
    }

    private String safeMessage(ClientHttpResponse response) throws IOException {
        String body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        if (body.isBlank()) {
            return null;
        }
        try {
            JsonNode message = objectMapper.readTree(body).path("message");
            if (message.isTextual() && !message.textValue().isBlank()) {
                String value = message.textValue().trim();
                return value.length() <= 300 ? value : value.substring(0, 300);
            }
        } catch (Exception ignored) {
            // El cuerpo puede no ser JSON; nunca se reenvia sin validar.
        }
        return null;
    }
}
