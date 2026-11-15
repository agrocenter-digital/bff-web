package com.agrocenter.bff;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.agrocenter.bff.client.ObservabilityClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ObservabilitySecurityIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockitoBean JwtDecoder decoder;
    @MockitoBean ObservabilityClient client;

    @Test void soloAdminPuedeConsultarYNoHayEscritura() throws Exception {
        mvc.perform(get("/api/bff/admin/analiticas/ventas")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/bff/admin/auditoria/eventos").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden());
        var admin = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(client.analytics(eq("ventas"), anyMap())).thenReturn(mapper.readTree("{\"elementos\":[],\"hayMas\":false}"));
        mvc.perform(get("/api/bff/admin/analiticas/ventas").param("desde", "2026-11-12").with(admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.hayMas").value(false));
        verify(client).analytics("ventas", Map.of("desde", "2026-11-12"));
        mvc.perform(post("/api/bff/admin/auditoria/eventos").with(admin)).andExpect(status().isMethodNotAllowed());
        mvc.perform(get("/api/bff/admin/analiticas/ventas").param("url", "http://externo").with(admin)).andExpect(status().isBadRequest());
    }

    @Test void detalleSoloPermiteRutasConocidas() throws Exception {
        var admin = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(client.auditDetail("eventos", "evt-1")).thenReturn(mapper.readTree("{\"eventId\":\"evt-1\"}"));
        mvc.perform(get("/api/bff/admin/auditoria/eventos/evt-1").with(admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value("evt-1"));
        mvc.perform(get("/api/bff/admin/auditoria/otra/evt-1").with(admin)).andExpect(status().isNotFound());
    }

    @Test void estadosDeConsumidorSonSoloLecturaAdministrativa() throws Exception {
        var admin = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(client.analytics(eq("estado-consumidor"), anyMap())).thenReturn(mapper.readTree("{\"estado\":\"DESACTIVADO\"}"));
        when(client.audit(eq("estado-consumidor"), anyMap())).thenReturn(mapper.readTree("{\"estado\":\"NO_SALUDABLE\"}"));
        mvc.perform(get("/api/bff/admin/analiticas/estado-consumidor")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/bff/admin/analiticas/estado-consumidor").with(admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DESACTIVADO"));
        mvc.perform(get("/api/bff/admin/auditoria/estado-consumidor").with(admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("NO_SALUDABLE"));
        mvc.perform(get("/api/bff/admin/auditoria/estado-consumidor/otro").with(admin)).andExpect(status().isNotFound());
    }
}
