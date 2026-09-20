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
        // En rutas privadas, se propaga el Bearer token únicamente si el usuario está autenticado.
        if (!isPublicCatalogRequest) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null
                    && authentication.isAuthenticated()
                    && !(authentication instanceof AnonymousAuthenticationToken)) {

                if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
                    request.getHeaders().setBearerAuth(jwtAuthentication.getToken().getTokenValue());
                } else if (authentication.getPrincipal() instanceof Jwt jwt) {
                    request.getHeaders().setBearerAuth(jwt.getTokenValue());
                } else if (authentication.getCredentials() instanceof String cred && !cred.isBlank()) {
                    request.getHeaders().setBearerAuth(cred);
                }
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
