package com.launchwindow.service.auth;

import com.launchwindow.config.JwtConfiguration;
import com.launchwindow.config.JwtProperties;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {
    private static final Instant NOW = Instant.now().truncatedTo(ChronoUnit.SECONDS);

    @Test
    void shouldCreateSignedTokenWithUserClaims() {
        JwtProperties properties = new JwtProperties(
                "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
                "launch-window",
                Duration.ofHours(2)
        );

        JwtConfiguration configuration = new JwtConfiguration();
        SecretKey secretKey = configuration.jwtSecretKey(properties);
        JwtEncoder encoder = configuration.jwtEncoder(secretKey);
        JwtDecoder decoder = configuration.jwtDecoder(secretKey, properties);
        JwtService service = new JwtService(encoder, properties, Clock.fixed(NOW, ZoneOffset.UTC));

        AppUser user = mock(AppUser.class);
        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("launch_test");
        when(user.getRole()).thenReturn(Role.USER);

        String token = service.createToken(user);
        Jwt decodedToken = decoder.decode(token);

        assertAll(
                () -> assertEquals("launch_test", decodedToken.getSubject()),
                () -> assertEquals("launch-window", decodedToken.getClaimAsString("iss")),
                () -> assertEquals(1L, decodedToken.<Number>getClaim("userId").longValue()),
                () -> assertEquals("USER", decodedToken.getClaimAsString("role")),
                () -> assertEquals(NOW, decodedToken.getIssuedAt()),
                () -> assertEquals(NOW.plus(Duration.ofHours(2)), decodedToken.getExpiresAt())
        );
    }
}
