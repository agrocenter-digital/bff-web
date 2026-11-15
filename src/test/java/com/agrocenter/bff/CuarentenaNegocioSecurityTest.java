package com.agrocenter.bff;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.agrocenter.bff.client.CuarentenaNegocioClient;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class CuarentenaNegocioSecurityTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockitoBean JwtDecoder decoder;
    @MockitoBean CuarentenaNegocioClient client;

    @Test void soloAdminPuedeConsultarRutasConocidas() throws Exception {
        mvc.perform(get("/api/bff/admin/inventario/cuarentena")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/bff/admin/compras/cuarentena").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden());
        var admin = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(client.consultar(true, 0, 10, true)).thenReturn(mapper.readTree("{\"elementos\":[],\"hayMas\":false}"));
        when(client.consultar(false, 0, 25, false)).thenReturn(mapper.readTree("{\"elementos\":[],\"hayMas\":false}"));
        mvc.perform(get("/api/bff/admin/inventario/cuarentena").param("tamano", "10").with(admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.hayMas").value(false));
        mvc.perform(get("/api/bff/admin/compras/cuarentena").param("pendientes", "false").with(admin)).andExpect(status().isOk());
        mvc.perform(get("/api/bff/admin/compras/cuarentena").param("tamano", "101").with(admin)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/bff/admin/ventas/cuarentena").with(admin)).andExpect(status().isNotFound());
        mvc.perform(post("/api/bff/admin/inventario/cuarentena").with(admin)).andExpect(status().isMethodNotAllowed());
        verify(client).consultar(true, 0, 10, true);
        verify(client).consultar(false, 0, 25, false);
    }
}
