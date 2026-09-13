package com.agrocenter.bff.config;

import com.agrocenter.bff.client.DownstreamErrorMapper;
import com.agrocenter.bff.client.ForwardedHeadersInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
public class RestClientConfig {

    @Bean
    @Qualifier("inventoryRestClient")
    RestClient inventoryRestClient(
            RestClient.Builder builder,
            ServiceProperties properties,
            ForwardedHeadersInterceptor forwardedHeadersInterceptor,
            DownstreamErrorMapper errorMapper
    ) {
        return build(
                builder,
                properties.inventory(),
                "ms-inventario",
                forwardedHeadersInterceptor,
                errorMapper
        );
    }

    @Bean
    @Qualifier("salesRestClient")
    RestClient salesRestClient(
            RestClient.Builder builder,
            ServiceProperties properties,
            ForwardedHeadersInterceptor forwardedHeadersInterceptor,
            DownstreamErrorMapper errorMapper
    ) {
        return build(
                builder,
                properties.sales(),
                "ms-ventas",
                forwardedHeadersInterceptor,
                errorMapper
        );
    }

    @Bean
    @Qualifier("purchasesRestClient")
    RestClient purchasesRestClient(
            RestClient.Builder builder,
            ServiceProperties properties,
            ForwardedHeadersInterceptor forwardedHeadersInterceptor,
            DownstreamErrorMapper errorMapper
    ) {
        return build(
                builder,
                properties.purchases(),
                "ms-compras",
                forwardedHeadersInterceptor,
                errorMapper
        );
    }

    private RestClient build(
            RestClient.Builder builder,
            ServiceProperties.Endpoint properties,
            String service,
            ForwardedHeadersInterceptor forwardedHeadersInterceptor,
            DownstreamErrorMapper errorMapper
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.responseTimeout());

        String resolvedUrl = sanitizeBaseUrl(properties.baseUrl(), service);
        org.springframework.web.util.DefaultUriBuilderFactory uriBuilderFactory =
                new org.springframework.web.util.DefaultUriBuilderFactory(resolvedUrl);
        uriBuilderFactory.setEncodingMode(org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode.URI_COMPONENT);

        return builder.clone()
                .baseUrl(resolvedUrl)
                .uriBuilderFactory(uriBuilderFactory)
                .requestFactory(requestFactory)
                .requestInterceptor(forwardedHeadersInterceptor)
                .defaultStatusHandler(
                        HttpStatusCode::isError,
                        (request, response) -> errorMapper.handle(service, request, response)
                )
                .build();
    }

    private String sanitizeBaseUrl(String url, String service) {
        if (url == null || url.isBlank() || ServiceProperties.isUnresolvedPlaceholder(url)) {
            return switch (service) {
                case "ms-inventario" -> "http://ms-inventario-svc:8081";
                case "ms-ventas" -> "http://ms-ventas-svc:8082";
                case "ms-compras" -> "http://ms-compras-svc:8083";
                default -> "http://localhost:8080";
            };
        }
        return url.trim().replaceAll("/+$", "");
    }
}
