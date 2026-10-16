package com.agrocenter.bff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "agrocenter.mensajeria")
public record MessagingProperties(List<String> topicos) {

    public MessagingProperties {
        if (topicos == null || topicos.isEmpty()) {
            topicos = List.of("ordenes-compras", "inventario-critico");
        }
    }
}
