package com.ssoss.ssossbackend.auth.infrastructure.token;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class JwtSigner {

    private final SecretKey key;

    JwtSigner(@Value("${auth.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    String sign(Long memberId, Instant issuedAt, Duration ttl) {
        return Jwts.builder()
            .subject(String.valueOf(memberId))
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(issuedAt.plus(ttl)))
            .signWith(key)
            .compact();
    }
}
