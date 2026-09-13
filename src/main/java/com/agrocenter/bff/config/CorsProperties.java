package com.agrocenter.bff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "agrocenter.cors")
public record CorsProperties(
        List<String> allowedOrigins,
        List<String> allowedOriginPatterns
) {
    public CorsProperties {
        allowedOrigins = allowedOrigins == null
                ? List.of()
                : allowedOrigins.stream().map(String::trim).filter(value -> !value.isBlank()).toList();
        allowedOriginPatterns = allowedOriginPatterns == null
                ? List.of()
                : allowedOriginPatterns.stream().map(String::trim).filter(value -> !value.isBlank()).toList();
    }
}
