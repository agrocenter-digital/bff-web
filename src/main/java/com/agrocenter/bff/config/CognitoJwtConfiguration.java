package com.agrocenter.bff.config;

import com.agrocenter.bff.security.CognitoAudienceValidator;
import com.agrocenter.bff.security.CognitoTokenUseValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;

@Configuration
@Profile("!dev")
public class CognitoJwtConfiguration {

    @Bean
    JwtDecoder cognitoJwtDecoder(JwtProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(properties.jwkSetUri())
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        OAuth2TokenValidator<Jwt> issuerAndTime = JwtValidators.createDefaultWithIssuer(
                properties.issuerUri()
        );
        java.util.List<OAuth2TokenValidator<Jwt>> validators = new java.util.ArrayList<>();
        validators.add(issuerAndTime);
        if (properties.audience() != null && !properties.audience().isBlank() && !"agrocenter-api".equalsIgnoreCase(properties.audience())) {
            validators.add(new CognitoAudienceValidator(properties.audience()));
        }
        validators.add(new CognitoTokenUseValidator());
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        return decoder;
    }
}
