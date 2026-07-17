package com.ssoss.ssossbackend.auth.entrypoint.controller;

import java.time.Duration;
import java.time.Instant;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;
import com.ssoss.ssossbackend.auth.domain.contract.TokenHasher;
import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.domain.model.RefreshToken;
import com.ssoss.ssossbackend.auth.domain.model.RefreshTokenStatus;
import com.ssoss.ssossbackend.auth.domain.model.SocialProvider;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.TokenRefreshResponse;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("로그아웃 API")
class LogoutApiTest extends IntegrationTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TokenHasher tokenHasher;

    @BeforeEach
    void resetDatabase() {
        refreshTokenRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /v1/logout")
    class Logout {

        @Test
        @DisplayName("유효한 refresh token 으로 로그아웃하면 204 를 반환하고 폐기된 토큰으로는 재발급이 거부된다")
        void revokesRefreshToken_whenValid() {
            naverApi.stubProfile("logout-login-token", "naver-id-logout");
            SocialLoginResponse loggedIn = fixture.socialLogin(SocialProvider.NAVER, "logout-login-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();

            fixture.logout(loggedIn.refreshToken())
                .expectStatus().isNoContent();

            RefreshToken revoked = refreshTokenRepository.findByTokenHash(tokenHasher.hash(loggedIn.refreshToken()))
                .orElseThrow();
            assertThat(revoked.getStatus()).isEqualTo(RefreshTokenStatus.DELETED);
            assertThat(revoked.getDeletedAt()).isNotNull();

            fixture.refreshTokens(loggedIn.refreshToken())
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN.getCode()));
        }

        @Test
        @DisplayName("존재하지 않는 refresh token 으로 로그아웃해도 204 를 반환한다")
        void returns204_whenTokenUnknown() {
            fixture.logout("unknown-refresh-token")
                .expectStatus().isNoContent();
        }

        @Test
        @DisplayName("이미 회전되어 폐기된 토큰으로 로그아웃해도 204 를 반환하고 폐기 이력과 세션 최신 토큰은 그대로 유지된다")
        void keepsSessionAndHistory_whenRotatedTokenIsSubmitted() {
            naverApi.stubProfile("rotated-logout-token", "naver-id-rotated-logout");
            SocialLoginResponse loggedIn = fixture.socialLogin(SocialProvider.NAVER, "rotated-logout-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();
            TokenRefreshResponse refreshed = fixture.refreshTokens(loggedIn.refreshToken())
                .expectStatus().isOk()
                .expectBody(TokenRefreshResponse.class)
                .returnResult()
                .getResponseBody();
            Instant rotatedAt = refreshTokenRepository.findByTokenHash(tokenHasher.hash(loggedIn.refreshToken()))
                .orElseThrow()
                .getDeletedAt();

            clock.advanceBy(Duration.ofHours(1));
            fixture.logout(loggedIn.refreshToken())
                .expectStatus().isNoContent();

            RefreshToken rotated = refreshTokenRepository.findByTokenHash(tokenHasher.hash(loggedIn.refreshToken()))
                .orElseThrow();
            assertThat(rotated.getDeletedAt()).isEqualTo(rotatedAt);
            fixture.refreshTokens(refreshed.refreshToken()).expectStatus().isOk();
        }

        @Test
        @DisplayName("refresh TTL 이 지나 만료된 토큰으로 로그아웃해도 204 를 반환하고 폐기는 만료 배치 소관으로 남긴다")
        void returns204WithoutRevoking_whenTokenExpired() {
            naverApi.stubProfile("expired-logout-token", "naver-id-expired-logout");
            SocialLoginResponse loggedIn = fixture.socialLogin(SocialProvider.NAVER, "expired-logout-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();

            clock.advanceBy(Duration.ofDays(15));
            fixture.logout(loggedIn.refreshToken())
                .expectStatus().isNoContent();

            RefreshToken expired = refreshTokenRepository.findByTokenHash(tokenHasher.hash(loggedIn.refreshToken()))
                .orElseThrow();
            assertThat(expired.getStatus()).isEqualTo(RefreshTokenStatus.ACTIVE);
            assertThat(expired.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("여러 기기로 로그인한 상태에서 한 세션만 로그아웃하면 다른 세션은 계속 재발급할 수 있다")
        void keepsOtherSessions_whenOneSessionLogsOut() {
            naverApi.stubProfile("device-a-logout-token", "naver-id-multi-logout");
            SocialLoginResponse deviceA = fixture.socialLogin(SocialProvider.NAVER, "device-a-logout-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();
            naverApi.stubProfile("device-b-logout-token", "naver-id-multi-logout");
            SocialLoginResponse deviceB = fixture.socialLogin(SocialProvider.NAVER, "device-b-logout-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();

            fixture.logout(deviceA.refreshToken())
                .expectStatus().isNoContent();

            fixture.refreshTokens(deviceA.refreshToken())
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN.getCode()));
            fixture.refreshTokens(deviceB.refreshToken()).expectStatus().isOk();
        }

        @Test
        @DisplayName("refreshToken 이 공백이면 400 과 C0001 을 반환한다")
        void returns400_whenRefreshTokenBlank() {
            fixture.logout(" ")
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }
    }
}
