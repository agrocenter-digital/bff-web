package com.agrocenter.bff.client;

import com.agrocenter.bff.exception.DownstreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.util.function.Supplier;

@Component
public class ClientCallExecutor {

    private static final Logger log = LoggerFactory.getLogger(ClientCallExecutor.class);

    public <T> T execute(String service, String operation, Supplier<T> call) {
        long startedAt = System.nanoTime();
        try {
            T result = call.get();
            if (result == null) {
                throw DownstreamException.badGateway(service);
            }
            log.info(
                    "Llamada a {} operacion={} completada durationMs={}",
                    service,
                    operation,
                    elapsedMillis(startedAt)
            );
            return result;
        } catch (DownstreamException exception) {
            log.warn(
                    "Error controlado en {} operacion={} status={} durationMs={}",
                    service,
                    operation,
                    exception.getStatus().value(),
                    elapsedMillis(startedAt)
            );
            throw exception;
        } catch (ResourceAccessException exception) {
            log.warn(
                    "Timeout o fallo de transporte en {} operacion={} durationMs={}",
                    service,
                    operation,
                    elapsedMillis(startedAt)
            );
            throw DownstreamException.unavailable(service);
        } catch (RestClientException exception) {
            log.warn(
                    "Respuesta HTTP no procesable de {} operacion={} durationMs={}",
                    service,
                    operation,
                    elapsedMillis(startedAt)
            );
            throw DownstreamException.badGateway(service);
        }
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
