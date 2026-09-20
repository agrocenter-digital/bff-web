package com.agrocenter.bff.client;

import com.agrocenter.bff.observability.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

@Component
public class ForwardedHeadersInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        HttpServletRequest servletRequest = null;
        boolean isPublicCatalogRequest = false;

        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            servletRequest = attributes.getRequest();
            String incomingUri = servletRequest.getRequestURI();
            if (incomingUri != null && (incomingUri.equals("/api/bff/catalogo") || incomingUri.startsWith("/api/bff/catalogo/"))) {
                isPublicCatalogRequest = true;
            }
        }

        // Para el catálogo público no se exige ni propaga token downstream, permitiendo
        // acceso anónimo limpio sin que el microservicio falle por credenciales inválidas.
        // En rutas privadas, se propaga el Bearer token recibido desde el frontend o contexto.
        if (!isPublicCatalogRequest) {
            String token = null;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null
                    && authentication.isAuthenticated()
                    && !(authentication instanceof AnonymousAuthenticationToken)) {

                if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
                    token = jwtAuthentication.getToken().getTokenValue();
                } else if (authentication.getPrincipal() instanceof Jwt jwt) {
                    token = jwt.getTokenValue();
                } else if (authentication.getCredentials() instanceof Jwt jwt) {
                    token = jwt.getTokenValue();
                } else if (authentication.getCredentials() != null) {
                    String credStr = authentication.getCredentials().toString();
                    if (!credStr.isBlank()) {
                        token = credStr.startsWith("Bearer ") ? credStr.substring(7).trim() : credStr.trim();
                    }
                }
            }

            // Fallback: si aún no se obtuvo del contexto pero la petición HTTP entrante incluye Authorization
            if ((token == null || token.isBlank()) && servletRequest != null) {
                String authHeader = servletRequest.getHeader(HttpHeaders.AUTHORIZATION);
                if (authHeader != null && !authHeader.isBlank()) {
                    token = authHeader.startsWith("Bearer ") ? authHeader.substring(7).trim() : authHeader.trim();
                }
            }

            if (token != null && !token.isBlank()) {
                request.getHeaders().setBearerAuth(token);
            }
        }

        if (servletRequest != null) {
            request.getHeaders().set(
                    CorrelationIdFilter.HEADER_NAME,
                    CorrelationIdFilter.from(servletRequest)
            );
        }
        request.getHeaders().set(HttpHeaders.ACCEPT, "application/json");
        return execution.execute(request, body);
    }
}
