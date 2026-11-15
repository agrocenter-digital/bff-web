package com.agrocenter.bff.client;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.agrocenter.bff.exception.DownstreamException;
import com.agrocenter.bff.observability.CorrelationIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class ObservabilityClientTest {
    @AfterEach void limpiar() { SecurityContextHolder.clearContext(); RequestContextHolder.resetRequestAttributes(); }

    @Test void propagaJwtCorrelacionYParametrosSinPermitirRutasArbitrarias() {
        var analyticsBuilder = RestClient.builder().baseUrl("http://analytics.test");
        var auditBuilder = RestClient.builder().baseUrl("http://audit.test");
        var analyticsServer = MockRestServiceServer.bindTo(analyticsBuilder).build();
        var auditServer = MockRestServiceServer.bindTo(auditBuilder).build();
        var interceptor = new ForwardedHeadersInterceptor();
        var mapper = new DownstreamErrorMapper(new ObjectMapper());
        var analytics = analyticsBuilder.requestInterceptor(interceptor)
                .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> mapper.handle("ms-analiticas", req, res)).build();
        var audit = auditBuilder.requestInterceptor(interceptor)
                .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> mapper.handle("ms-auditoria", req, res)).build();
        var client = new ObservabilityClient(analytics, audit, new ClientCallExecutor());
        var jwt = Jwt.withTokenValue("verified-token").header("alg", "RS256").subject("admin")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(600)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        var request = new MockHttpServletRequest();
        request.setAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE, "trace-123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        analyticsServer.expect(requestTo("http://analytics.test/api/analiticas/ventas?desde=2026-11-12"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer verified-token"))
                .andExpect(header(CorrelationIdFilter.HEADER_NAME, "trace-123"))
                .andRespond(withSuccess("{\"elementos\":[]}", MediaType.APPLICATION_JSON));
        assertThat(client.analytics("ventas", Map.of("desde", "2026-11-12")).path("elementos").isArray()).isTrue();
        auditServer.expect(requestTo("http://audit.test/api/auditoria/eventos/evt-1"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer verified-token"))
                .andExpect(header(CorrelationIdFilter.HEADER_NAME, "trace-123"))
                .andRespond(withSuccess("{\"eventId\":\"evt-1\"}", MediaType.APPLICATION_JSON));
        assertThat(client.auditDetail("eventos", "evt-1").path("eventId").asText()).isEqualTo("evt-1");
        assertThatThrownBy(() -> client.analytics("http://externo", Map.of())).isInstanceOf(IllegalArgumentException.class);
        analyticsServer.verify(); auditServer.verify();
    }

    @Test void errorDownstreamNoSePresentaComoExito() {
        var builder = RestClient.builder().baseUrl("http://audit.test");
        var server = MockRestServiceServer.bindTo(builder).build();
        var mapper = new DownstreamErrorMapper(new ObjectMapper());
        var rest = builder.defaultStatusHandler(HttpStatusCode::isError, (req, res) -> mapper.handle("ms-auditoria", req, res)).build();
        var client = new ObservabilityClient(rest, rest, new ClientCallExecutor());
        server.expect(requestTo("http://audit.test/api/auditoria/eventos")).andRespond(withServerError());
        assertThatThrownBy(() -> client.audit("eventos", Map.of())).isInstanceOf(DownstreamException.class);
        server.verify();
    }
}
