package com.agrocenter.bff.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CognitoSecurityConvertersTest {

    private static final Instant NOW = Instant.parse("2026-08-28T12:00:00Z");

    @Test
    void mapsOnlyKnownCognitoGroupsAndScopes() {
        Jwt jwt = jwtBuilder()
                .claim("cognito:groups", List.of("cliente", "SUPERUSER"))
                .claim("custom:role", "ADMIN")
                .claim("scope", "catalogo.read")
                .build();

        assertThat(new CognitoAuthoritiesConverter().convert(jwt))
                .extracting(Object::toString)
                .containsExactlyInAnyOrder(
                        "SCOPE_catalogo.read",
                        "ROLE_CLIENTE",
                        "ROLE_ADMIN"
                );
    }

    @Test
    void validatesAudienceOrCognitoClientId() {
        CognitoAudienceValidator validator = new CognitoAudienceValidator("web-client-id");

        Jwt audienceToken = jwtBuilder().audience(List.of("web-client-id")).build();
        Jwt accessToken = jwtBuilder().claim("client_id", "web-client-id").build();
        Jwt wrongToken = jwtBuilder().claim("client_id", "another-client").build();

        assertThat(validator.validate(audienceToken).hasErrors()).isFalse();
        assertThat(validator.validate(accessToken).hasErrors()).isFalse();
        assertThat(validator.validate(wrongToken).hasErrors()).isTrue();
    }

    @Test
    void requiresCognitoAccessTokenInProductionValidator() {
        CognitoTokenUseValidator validator = new CognitoTokenUseValidator();

        assertThat(validator.validate(jwtBuilder().claim("token_use", "access").build()).hasErrors())
                .isFalse();
        assertThat(validator.validate(jwtBuilder().claim("token_use", "id").build()).hasErrors())
                .isTrue();
    }

    private static Jwt.Builder jwtBuilder() {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user-123")
                .issuedAt(NOW)
                .expiresAt(NOW.plusSeconds(3600));
    }
}
