package com.agrocenter.bff.config;

import com.agrocenter.bff.security.CognitoAudienceValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@Profile("dev & !prod")
public class DevJwtConfiguration {

    private static final int MINIMUM_SECRET_BYTES = 32;

    @Bean
    JwtDecoder devJwtDecoder(
            @Value("${agrocenter.security.dev.jwt-secret}") String secret,
            @Value("${agrocenter.security.dev.issuer}") String issuer,
            @Value("${agrocenter.security.dev.audience}") String audience
    ) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MINIMUM_SECRET_BYTES) {
            throw new IllegalStateException("DEV_JWT_SECRET debe tener al menos 32 caracteres");
        }
        SecretKey key = new SecretKeySpec(bytes, "HmacSHA256");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuer),
                new CognitoAudienceValidator(audience)
        ));
        return decoder;
    }
}
