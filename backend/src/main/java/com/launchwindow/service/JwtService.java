package com.launchwindow.service;

import com.launchwindow.config.JwtProperties;
import com.launchwindow.model.AppUser;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class JwtService {
    private final JwtEncoder encoder;
    private final JwtProperties properties;
    private final Clock clock;

    public JwtService(JwtEncoder encoder, JwtProperties properties, Clock clock) {
        this.encoder = encoder;
        this.properties = properties;
        this.clock = clock;
    }

    public String createToken(AppUser user) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(properties.expiration());

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .build();

        JwtEncoderParameters parameters = JwtEncoderParameters.from(header, claims);

        return encoder.encode(parameters).getTokenValue();
    }
}
