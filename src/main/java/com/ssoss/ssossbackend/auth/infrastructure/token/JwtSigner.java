package com.ssoss.ssossbackend.auth.infrastructure.token;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import com.ssoss.ssossbackend.auth.domain.contract.TokenParser;
import com.ssoss.ssossbackend.auth.domain.model.AccessTokenPayload;
import com.ssoss.ssossbackend.auth.domain.model.MemberStatus;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class JwtSigner implements TokenParser {

    private static final String ROLE_CLAIM = "role";

    private final SecretKey key;

    JwtSigner(@Value("${auth.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    String sign(Long memberId, MemberStatus status, Instant issuedAt, Duration ttl) {
        return Jwts.builder()
            .subject(String.valueOf(memberId))
            .claim(ROLE_CLAIM, status.name())
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(issuedAt.plus(ttl)))
            .signWith(key)
            .compact();
    }

    @Override
    public Optional<AccessTokenPayload> parse(String accessToken) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();
            String role = claims.get(ROLE_CLAIM, String.class);
            if (role == null) {
                return Optional.empty();
            }
            return Optional.of(new AccessTokenPayload(Long.valueOf(claims.getSubject()), MemberStatus.valueOf(role)));
        } catch (JwtException | IllegalArgumentException invalid) {
            // 무효 토큰(서명·만료·클레임 오류)은 빈 결과 = 미인증 — 보호 경로의 401 은 EntryPoint 가 담당한다
            return Optional.empty();
        }
    }
}
