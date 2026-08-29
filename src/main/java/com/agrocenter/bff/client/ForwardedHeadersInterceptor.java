package com.agrocenter.bff.client;

import com.agrocenter.bff.observability.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication
                && authentication.isAuthenticated()) {
            request.getHeaders().setBearerAuth(jwtAuthentication.getToken().getTokenValue());
        }

        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest servletRequest = attributes.getRequest();
            request.getHeaders().set(
                    CorrelationIdFilter.HEADER_NAME,
                    CorrelationIdFilter.from(servletRequest)
            );
        }
        request.getHeaders().set(HttpHeaders.ACCEPT, "application/json");
        return execution.execute(request, body);
    }
}
