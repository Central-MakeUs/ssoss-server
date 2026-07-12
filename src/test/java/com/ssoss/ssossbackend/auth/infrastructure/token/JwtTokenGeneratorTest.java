package com.ssoss.ssossbackend.auth.infrastructure.token;

import java.time.Duration;

import com.ssoss.ssossbackend.auth.domain.model.LoginToken;
import com.ssoss.ssossbackend.support.JwtTestSupport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenGenerator")
class JwtTokenGeneratorTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-256-bits-long!!";
    private static final Duration ACCESS_TTL = Duration.ofMinutes(30);
    private static final Duration REFRESH_TTL = Duration.ofDays(14);

    private final JwtTokenGenerator jwtTokenGenerator =
        new JwtTokenGenerator(new JwtSigner(SECRET), ACCESS_TTL, REFRESH_TTL);
    private final JwtTestSupport jwtTestSupport = new JwtTestSupport(SECRET);

    @Nested
    @DisplayName("토큰 생성")
    class Generate {

        @Test
        @DisplayName("회원 id 로 생성하면 두 토큰 모두 subject 에 회원 id 가 담긴다")
        void generatesLoginTokenWithMemberIdSubject() {
            LoginToken loginToken = jwtTokenGenerator.generate(1L);

            assertThat(jwtTestSupport.parse(loginToken.accessToken()).getSubject()).isEqualTo("1");
            assertThat(jwtTestSupport.parse(loginToken.refreshToken()).getSubject()).isEqualTo("1");
        }

        @Test
        @DisplayName("access 토큰은 access 만료 시간, refresh 토큰은 refresh 만료 시간으로 생성된다")
        void appliesConfiguredExpirations() {
            LoginToken loginToken = jwtTokenGenerator.generate(1L);

            assertThat(jwtTestSupport.lifetimeOf(loginToken.accessToken())).isEqualTo(ACCESS_TTL);
            assertThat(jwtTestSupport.lifetimeOf(loginToken.refreshToken())).isEqualTo(REFRESH_TTL);
        }
    }
}
