package com.ssoss.ssossbackend.auth.infrastructure.token;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import com.ssoss.ssossbackend.auth.domain.model.LoginToken;
import com.ssoss.ssossbackend.support.JwtTestSupport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginTokenGenerator")
class LoginTokenGeneratorTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-256-bits-long!!";
    private static final Duration ACCESS_TTL = Duration.ofMinutes(30);
    private static final Duration REFRESH_TTL = Duration.ofDays(14);
    private static final Instant NOW = Instant.now();

    private final LoginTokenGenerator loginTokenGenerator = new LoginTokenGenerator(
        new JwtSigner(SECRET), new OpaqueTokenGenerator(), Clock.fixed(NOW, ZoneOffset.UTC), ACCESS_TTL, REFRESH_TTL);
    private final JwtTestSupport jwtTestSupport = new JwtTestSupport(SECRET);

    @Nested
    @DisplayName("토큰 생성")
    class Generate {

        @Test
        @DisplayName("access 토큰은 회원 id 를 subject 로 담고 access 만료 시간을 가진 JWT 로 생성된다")
        void generatesAccessTokenAsJwt() {
            LoginToken loginToken = loginTokenGenerator.generate(1L);

            assertThat(jwtTestSupport.parse(loginToken.accessToken()).getSubject()).isEqualTo("1");
            assertThat(jwtTestSupport.lifetimeOf(loginToken.accessToken())).isEqualTo(ACCESS_TTL);
        }

        @Test
        @DisplayName("refresh 토큰은 JWT 가 아닌 base64url 랜덤 문자열(opaque)로 생성된다")
        void generatesRefreshTokenAsOpaque() {
            LoginToken loginToken = loginTokenGenerator.generate(1L);

            assertThat(loginToken.refreshToken()).matches("[A-Za-z0-9_-]{43,}");
        }

        @Test
        @DisplayName("같은 회원이라도 refresh 토큰은 호출마다 다른 값으로 생성된다")
        void generatesDifferentRefreshTokenPerCall() {
            LoginToken first = loginTokenGenerator.generate(1L);
            LoginToken second = loginTokenGenerator.generate(1L);

            assertThat(first.refreshToken()).isNotEqualTo(second.refreshToken());
        }

        @Test
        @DisplayName("refresh 토큰 만료 시각은 생성 시각 + refresh 만료 시간으로 부여된다")
        void assignsRefreshTokenExpiry() {
            LoginToken loginToken = loginTokenGenerator.generate(1L);

            assertThat(loginToken.refreshTokenExpiresAt()).isEqualTo(NOW.plus(REFRESH_TTL));
        }
    }
}
