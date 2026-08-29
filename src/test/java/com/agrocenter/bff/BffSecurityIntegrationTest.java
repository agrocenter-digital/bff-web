package com.agrocenter.bff;

import com.agrocenter.bff.client.InventoryClient;
import com.agrocenter.bff.client.PurchasesClient;
import com.agrocenter.bff.client.SalesClient;
import com.agrocenter.bff.dto.common.PageResponse;
import com.agrocenter.bff.dto.inventory.ProductResponse;
import com.agrocenter.bff.dto.purchases.CreatePurchaseRequest;
import com.agrocenter.bff.dto.purchases.PurchaseDetailResponse;
import com.agrocenter.bff.dto.purchases.PurchaseResponse;
import com.agrocenter.bff.dto.sales.SaleResponse;
import com.agrocenter.bff.exception.DownstreamException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BffSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private InventoryClient inventoryClient;

    @MockitoBean
    private SalesClient salesClient;

    @MockitoBean
    private PurchasesClient purchasesClient;

    @Test
    void protectedEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/bff/catalogo"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(header().exists("X-Correlation-ID"));
    }

    @Test
    void fakeTokenReturns401() throws Exception {
        given(jwtDecoder.decode("fake-token")).willThrow(new BadJwtException("invalid signature"));

        mockMvc.perform(get("/api/bff/catalogo")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void expiredTokenReturns401() throws Exception {
        given(jwtDecoder.decode("expired-token")).willThrow(new JwtValidationException(
                "Jwt expired",
                List.of(new OAuth2Error("invalid_token", "Jwt expired", null))
        ));

        mockMvc.perform(get("/api/bff/catalogo")
                        .header("Authorization", "Bearer expired-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void clientCannotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/bff/admin/dashboard").with(jwt().authorities(
                        () -> "ROLE_CLIENTE"
                )))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void adminCanAccessInventory() throws Exception {
        given(inventoryClient.listProducts(null, null, null)).willReturn(List.of(product(1, false)));

        mockMvc.perform(get("/api/bff/inventario").with(jwt().authorities(
                        () -> "ROLE_ADMIN"
                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("SKU-1"));
    }

    @Test
    void clientCanAccessCatalogAndReceivesDownstreamResponse() throws Exception {
        given(inventoryClient.listProducts(null, null, true)).willReturn(List.of(product(2, false)));

        mockMvc.perform(get("/api/bff/catalogo").with(jwt().authorities(
                        () -> "ROLE_CLIENTE"
                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void unavailableMicroserviceReturns503() throws Exception {
        given(inventoryClient.listProducts(null, null, true))
                .willThrow(DownstreamException.unavailable("ms-inventario"));

        mockMvc.perform(get("/api/bff/catalogo").with(jwt().authorities(
                        () -> "ROLE_CLIENTE"
                )))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("DOWNSTREAM_UNAVAILABLE"))
                .andExpect(jsonPath("$.correlationId").isNotEmpty());
    }

    @Test
    void invalidSaleBodyReturns400BeforeCallingSales() throws Exception {
        mockMvc.perform(post("/api/bff/ventas")
                        .with(jwt().authorities(() -> "ROLE_CLIENTE"))
                        .header("Idempotency-Key", "checkout-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"productoId":1,"cantidad":0}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors['items[0].cantidad']").exists());
    }

    @Test
    void adminCanCreatePurchase() throws Exception {
        PurchaseResponse purchase = new PurchaseResponse(
                12L,
                4L,
                LocalDateTime.parse("2026-08-28T12:00:00"),
                "COMPLETADA",
                BigDecimal.valueOf(3000),
                List.of(new PurchaseDetailResponse(
                        21L, 7L, 3, BigDecimal.valueOf(1000), BigDecimal.valueOf(3000)
                ))
        );
        given(purchasesClient.create(any(CreatePurchaseRequest.class))).willReturn(purchase);

        mockMvc.perform(post("/api/bff/compras")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "proveedorId": 4,
                                  "detalles": [
                                    {"productoId": 7, "cantidad": 3, "precioUnitario": 1000.00}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/bff/compras/12"))
                .andExpect(jsonPath("$.estado").value("COMPLETADA"));
    }

    @Test
    void adminDashboardAggregatesInventoryAndSales() throws Exception {
        given(inventoryClient.listProducts(null, null, null)).willReturn(List.of(
                product(1, true),
                product(2, false),
                product(3, true)
        ));
        given(salesClient.listAll(0, 1)).willReturn(new PageResponse<>(
                List.<SaleResponse>of(), 0, 1, 27, 27
        ));
        given(purchasesClient.list()).willReturn(List.of(
                new PurchaseResponse(
                        1L, 5L, LocalDateTime.parse("2026-08-28T12:00:00"),
                        "COMPLETADA", BigDecimal.TEN, List.of()
                ),
                new PurchaseResponse(
                        2L, 6L, LocalDateTime.parse("2026-08-28T13:00:00"),
                        "COMPLETADA", BigDecimal.ONE, List.of()
                )
        ));

        mockMvc.perform(get("/api/bff/admin/dashboard").with(jwt().authorities(
                        () -> "ROLE_ADMIN"
                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inventario.productos").value(3))
                .andExpect(jsonPath("$.inventario.productosStockBajo").value(2))
                .andExpect(jsonPath("$.ventas.ventasRegistradas").value(27))
                .andExpect(jsonPath("$.compras.comprasRegistradas").value(2));
    }

    private ProductResponse product(long id, boolean lowStock) {
        return new ProductResponse(
                id,
                "SKU-" + id,
                "Producto " + id,
                null,
                "Categoria",
                BigDecimal.valueOf(1000),
                10,
                5,
                lowStock,
                true,
                Instant.parse("2026-08-28T12:00:00Z"),
                Instant.parse("2026-08-28T12:00:00Z")
        );
    }
}
