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
        inventory = sanitizeEndpoint(inventory, "inventory", "http://ms-inventario-svc:8081");
        sales = sanitizeEndpoint(sales, "sales", "http://ms-ventas-svc:8082");
        purchases = sanitizeEndpoint(purchases, "purchases", "http://ms-compras-svc:8083");
    }

    private static Endpoint sanitizeEndpoint(Endpoint endpoint, String name, String defaultUrl) {
        if (endpoint == null) {
            return new Endpoint(defaultUrl, Duration.ofSeconds(5), Duration.ofSeconds(20));
        }
        String url = endpoint.baseUrl();
        if (url == null || url.isBlank() || isUnresolvedPlaceholder(url)) {
            return new Endpoint(defaultUrl, endpoint.connectTimeout(), endpoint.responseTimeout());
        }
        return endpoint;
    }

    public static boolean isUnresolvedPlaceholder(String value) {
        if (value == null) {
            return true;
        }
        String trimmed = value.trim();
        return trimmed.contains("${") || (trimmed.startsWith("{") && trimmed.endsWith("}")) || trimmed.contains("{MS_");
    }

    public record Endpoint(
            String baseUrl,
            Duration connectTimeout,
            Duration responseTimeout
    ) {
        public Endpoint {
            baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
            connectTimeout = connectTimeout == null ? Duration.ofSeconds(5) : connectTimeout;
            responseTimeout = responseTimeout == null ? Duration.ofSeconds(20) : responseTimeout;
            if (connectTimeout.isNegative() || connectTimeout.isZero()) {
                throw new IllegalArgumentException("El connection timeout debe ser positivo");
            }
            if (responseTimeout.isNegative() || responseTimeout.isZero()) {
                throw new IllegalArgumentException("El response timeout debe ser positivo");
            }
        }
    }
}
