package com.ssoss.ssossbackend.auth.infrastructure.token;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import com.ssoss.ssossbackend.auth.domain.model.AccessTokenPayload;
import com.ssoss.ssossbackend.auth.domain.model.MemberStatus;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtSigner")
class JwtSignerTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-256-bits-long!!";
    private static final Duration ACCESS_TTL = Duration.ofMinutes(30);

    private final JwtSigner jwtSigner = new JwtSigner(SECRET);

    @Nested
    @DisplayName("access 토큰 해석")
    class Parse {

        @Test
        @DisplayName("자체 서명한 유효 토큰이면 memberId 와 회원 상태를 담은 payload 를 반환한다")
        void returnsPayload_whenTokenIsValid() {
            String accessToken = jwtSigner.sign(1L, MemberStatus.PENDING, Instant.now(), ACCESS_TTL);

            assertThat(jwtSigner.parse(accessToken)).contains(new AccessTokenPayload(1L, MemberStatus.PENDING));
        }

        @Test
        @DisplayName("만료된 토큰이면 빈 결과를 반환한다")
        void returnsEmpty_whenTokenIsExpired() {
            String expired = jwtSigner.sign(
                1L, MemberStatus.ACTIVE, Instant.now().minus(Duration.ofMinutes(31)), ACCESS_TTL);

            assertThat(jwtSigner.parse(expired)).isEmpty();
        }

        @Test
        @DisplayName("다른 키로 서명된 토큰이면 빈 결과를 반환한다")
        void returnsEmpty_whenSignedByUnknownKey() {
            JwtSigner unknownSigner = new JwtSigner("another-secret-key-that-is-long-enough-256bit!!");
            String forged = unknownSigner.sign(1L, MemberStatus.ACTIVE, Instant.now(), ACCESS_TTL);

            assertThat(jwtSigner.parse(forged)).isEmpty();
        }

        @Test
        @DisplayName("role 클레임이 없는 토큰이면 빈 결과를 반환한다")
        void returnsEmpty_whenRoleClaimIsMissing() {
            SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
            String roleless = Jwts.builder()
                .subject("1")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(ACCESS_TTL)))
                .signWith(key)
                .compact();

            assertThat(jwtSigner.parse(roleless)).isEmpty();
        }

        @Test
        @DisplayName("JWT 형식이 아닌 문자열이면 빈 결과를 반환한다")
        void returnsEmpty_whenTokenIsMalformed() {
            assertThat(jwtSigner.parse("not-a-jwt")).isEmpty();
        }
    }
}
