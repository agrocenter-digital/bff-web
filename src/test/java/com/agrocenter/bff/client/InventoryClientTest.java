package com.agrocenter.bff.client;

import com.agrocenter.bff.dto.inventory.ProductResponse;
import com.agrocenter.bff.exception.DownstreamException;
import com.agrocenter.bff.observability.CorrelationIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class InventoryClientTest {

    private MockRestServiceServer server;
    private InventoryClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://inventory.test");
        server = MockRestServiceServer.bindTo(builder).build();
        DownstreamErrorMapper errorMapper = new DownstreamErrorMapper(new ObjectMapper());
        RestClient restClient = builder
                .requestInterceptor(new ForwardedHeadersInterceptor())
                .defaultStatusHandler(
                        HttpStatusCode::isError,
                        (request, response) -> errorMapper.handle("ms-inventario", request, response)
                )
                .build();
        client = new InventoryClient(restClient, new ClientCallExecutor());

        Jwt jwt = Jwt.withTokenValue("verified-token")
                .header("alg", "RS256")
                .subject("user-123")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        ));
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE, "test-correlation");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void returnsProductAndPropagatesVerifiedIdentity() {
        server.expect(requestTo("http://inventory.test/api/inventario/productos/7"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer verified-token"))
                .andExpect(header(CorrelationIdFilter.HEADER_NAME, "test-correlation"))
                .andRespond(withSuccess("""
                        {
                          "id": 7,
                          "sku": "SEM-007",
                          "nombre": "Semilla",
                          "descripcion": null,
                          "categoria": "Semillas",
                          "precioVenta": 1000.00,
                          "stockActual": 20,
                          "stockMinimo": 5,
                          "stockBajo": false,
                          "activo": true,
                          "createdAt": "2026-08-28T12:00:00Z",
                          "updatedAt": "2026-08-28T12:00:00Z"
                        }
                        """, MediaType.APPLICATION_JSON));

        ProductResponse response = client.getProduct(7L);

        assertThat(response.sku()).isEqualTo("SEM-007");
        server.verify();
    }

    @Test
    void mapsDownstreamServerErrorToBadGateway() {
        server.expect(requestTo("http://inventory.test/api/inventario/productos/7"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.getProduct(7L))
                .isInstanceOfSatisfying(DownstreamException.class, exception -> {
                    assertThat(exception.getStatus().value()).isEqualTo(502);
                    assertThat(exception.getCode()).isEqualTo("BAD_GATEWAY");
                });
        server.verify();
    }
}
