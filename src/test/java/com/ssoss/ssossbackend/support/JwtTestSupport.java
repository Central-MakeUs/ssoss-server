package com.ssoss.ssossbackend.support;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtTestSupport {

    private final SecretKey key;

    public JwtTestSupport(String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Duration lifetimeOf(String token) {
        Claims claims = parse(token);
        return Duration.ofMillis(claims.getExpiration().getTime() - claims.getIssuedAt().getTime());
    }
}
