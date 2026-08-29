package com.agrocenter.bff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "services")
public record ServiceProperties(
        Endpoint inventory,
        Endpoint sales,
        Endpoint purchases
) {
    public ServiceProperties {
        inventory = requireEndpoint(inventory, "inventory");
        sales = requireEndpoint(sales, "sales");
        purchases = requireEndpoint(purchases, "purchases");
    }

    private static Endpoint requireEndpoint(Endpoint endpoint, String name) {
        if (endpoint == null) {
            throw new IllegalArgumentException("Falta la configuracion services." + name);
        }
        return endpoint;
    }

    public record Endpoint(
            String baseUrl,
            Duration connectTimeout,
            Duration responseTimeout
    ) {
        public Endpoint {
            if (baseUrl == null || baseUrl.isBlank()) {
                throw new IllegalArgumentException("La URL del microservicio es obligatoria");
            }
            connectTimeout = connectTimeout == null ? Duration.ofSeconds(2) : connectTimeout;
            responseTimeout = responseTimeout == null ? Duration.ofSeconds(4) : responseTimeout;
            if (connectTimeout.isNegative() || connectTimeout.isZero()) {
                throw new IllegalArgumentException("El connection timeout debe ser positivo");
            }
            if (responseTimeout.isNegative() || responseTimeout.isZero()) {
                throw new IllegalArgumentException("El response timeout debe ser positivo");
            }
        }
    }
}
