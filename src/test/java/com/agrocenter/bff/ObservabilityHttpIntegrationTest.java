package com.agrocenter.bff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ObservabilityHttpIntegrationTest {
    private static final AtomicReference<String> auth = new AtomicReference<>();
    private static final AtomicReference<String> correlation = new AtomicReference<>();
    private static final AtomicReference<String> query = new AtomicReference<>();
    private static final AtomicReference<String> kinesisMethod = new AtomicReference<>();
    private static final AtomicReference<String> kinesisPath = new AtomicReference<>();
    private static final HttpServer upstream = server();

    @Autowired MockMvc mvc;
    @MockitoBean JwtDecoder decoder;

    private static HttpServer server() {
        try {
            var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/api/analiticas/resumen", exchange -> {
                auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
                correlation.set(exchange.getRequestHeaders().getFirst("X-Correlation-ID"));
                query.set(exchange.getRequestURI().getRawQuery());
                byte[] body = "{\"ventas\":2,\"monto\":100}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                try (var output = exchange.getResponseBody()) { output.write(body); }
            });
            server.createContext("/api/auditoria/eventos", exchange -> {
                exchange.sendResponseHeaders(503, -1);
                exchange.close();
            });
            server.createContext("/api/kinesis-admin/streams", exchange -> {
                auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
                kinesisMethod.set(exchange.getRequestMethod());
                kinesisPath.set(exchange.getRequestURI().toString());
                boolean reading = exchange.getRequestMethod().equals("GET");
                byte[] body = (reading ? "[{\"nombre\":\"agrocenter-prueba\",\"estado\":\"ACTIVE\"}]" : "{\"mensaje\":\"Solicitud aceptada\"}").getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(reading ? 200 : 202, body.length);
                try (var output = exchange.getResponseBody()) { output.write(body); }
            });
            server.createContext("/api/inventario/admin/cuarentena", exchange -> {
                auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
                correlation.set(exchange.getRequestHeaders().getFirst("X-Correlation-ID"));
                query.set(exchange.getRequestURI().getRawQuery());
                byte[] body = "{\"elementos\":[],\"hayMas\":false}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                try (var output = exchange.getResponseBody()) { output.write(body); }
            });
            server.start();
            return server;
        } catch (IOException e) { throw new ExceptionInInitializerError(e); }
    }

    @DynamicPropertySource
    static void endpoints(DynamicPropertyRegistry registry) {
        String url = "http://127.0.0.1:" + upstream.getAddress().getPort();
        registry.add("services.analytics.base-url", () -> url);
        registry.add("services.audit.base-url", () -> url);
        registry.add("services.inventory.base-url", () -> url);
        registry.add("services.kinesis-admin.base-url", () -> url);
    }

    @AfterAll static void stop() { upstream.stop(0); }

    @Test void rutaRealPropagaTokenCorrelacionYFiltro() throws Exception {
        mvc.perform(get("/api/bff/admin/analiticas/resumen").param("desde", "2026-11-01")
                .header("X-Correlation-ID", "test-trace-123")
                .with(jwt().jwt(token -> token.tokenValue("test-token"))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ventas").value(2));
        assertThat(auth.get()).isEqualTo("Bearer test-token");
        assertThat(correlation.get()).isEqualTo("test-trace-123");
        assertThat(query.get()).isEqualTo("desde=2026-11-01");
    }

    @Test void fallaDelServicioNoApareceComoListaVacia() throws Exception {
        mvc.perform(get("/api/bff/admin/auditoria/eventos")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().is5xxServerError());
    }

    @Test void kinesisSoloAdminYPropagaToken() throws Exception {
        mvc.perform(get("/api/bff/admin/kinesis/streams")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/bff/admin/kinesis/streams").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))).andExpect(status().isForbidden());
        mvc.perform(get("/api/bff/admin/kinesis/streams").with(jwt().jwt(token -> token.tokenValue("test-token"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].nombre").value("agrocenter-prueba"));
        assertThat(auth.get()).isEqualTo("Bearer test-token");
    }

    @Test void kinesisEscriturasRequierenAdminYPropaganAceptacion() throws Exception {
        String path = "/api/bff/admin/kinesis/streams";
        mvc.perform(post(path).contentType("application/json").content("{\"nombre\":\"agrocenter-prueba\",\"shards\":1}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post(path).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType("application/json").content("{}")) .andExpect(status().isForbidden());
        mvc.perform(post(path).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType("application/json").content("{\"nombre\":\"agrocenter-prueba\",\"shards\":1}"))
                .andExpect(status().isAccepted()).andExpect(jsonPath("$.mensaje").value("Solicitud aceptada"));
        assertThat(kinesisMethod.get()).isEqualTo("POST");
        mvc.perform(patch(path + "/agrocenter-prueba/retencion").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType("application/json").content("{\"retencionHoras\":48}")).andExpect(status().isAccepted());
        assertThat(kinesisPath.get()).endsWith("/agrocenter-prueba/retencion");
        mvc.perform(delete(path + "/agrocenter-prueba").param("confirmacion", "agrocenter-prueba")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))).andExpect(status().isAccepted());
        assertThat(kinesisMethod.get()).isEqualTo("DELETE");
        assertThat(kinesisPath.get()).contains("confirmacion=agrocenter-prueba");
    }

    @Test void cuarentenaInventarioUsaClienteRealYPropagaCredenciales() throws Exception {
        mvc.perform(get("/api/bff/admin/inventario/cuarentena").param("tamano", "10")
                .header("X-Correlation-ID", "test-trace-456")
                .with(jwt().jwt(token -> token.tokenValue("test-token"))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.hayMas").value(false));
        assertThat(auth.get()).isEqualTo("Bearer test-token");
        assertThat(correlation.get()).isEqualTo("test-trace-456");
        assertThat(query.get()).isEqualTo("pagina=0&tamano=10&pendientes=true");
    }
}
