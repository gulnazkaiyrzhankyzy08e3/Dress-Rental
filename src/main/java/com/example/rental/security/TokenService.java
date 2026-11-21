package com.example.rental.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class TokenService {
    public static final String ISSUER = "clothing-rental";
    public static final long EXPIRES_IN = 900;
    private final JwtEncoder encoder;
    public TokenService(JwtEncoder encoder) { this.encoder = encoder; }
    public String issue(Authentication user) {
        Instant now = Instant.now();
        List<String> roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_")).map(a -> a.substring(5)).toList();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(ISSUER).subject(user.getName())
                .issuedAt(now).expiresAt(now.plus(Duration.ofSeconds(EXPIRES_IN)))
                .claim("roles", roles).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}