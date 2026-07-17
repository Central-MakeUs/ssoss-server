package com.ssoss.ssossbackend.auth.entrypoint.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;
import com.ssoss.ssossbackend.auth.domain.contract.TokenHasher;
import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.domain.model.RefreshToken;
import com.ssoss.ssossbackend.auth.domain.model.RefreshTokenStatus;
import com.ssoss.ssossbackend.auth.domain.model.SocialProvider;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.TokenRefreshResponse;
import com.ssoss.ssossbackend.member.domain.contract.MemberRepository;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.ssoss.ssossbackend.member.domain.model.SocialProvider.NAVER;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("토큰 재발급 API")
class TokenRefreshApiTest extends IntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TokenHasher tokenHasher;

    @BeforeEach
    void resetDatabase() {
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /v1/tokens")
    class Refresh {

        @Test
        @DisplayName("유효한 refresh token 으로 요청하면 새 토큰 쌍이 발급되고 기존 토큰은 같은 세션의 DELETED 이력으로 남는다")
        void rotatesRefreshToken_whenValid() {
            naverApi.stubProfile("refresh-login-token", "naver-id-refresh");
            SocialLoginResponse loggedIn = fixture.socialLogin(SocialProvider.NAVER, "refresh-login-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();

            clock.advanceBy(Duration.ofHours(1));
            TokenRefreshResponse refreshed = fixture.refreshTokens(loggedIn.refreshToken())
                .expectStatus().isOk()
                .expectBody(TokenRefreshResponse.class)
                .returnResult()
                .getResponseBody();

            assertThat(refreshed.accessToken()).isNotBlank();
            assertThat(jwtTestSupport.roleOf(refreshed.accessToken())).isEqualTo("PENDING");
            assertThat(refreshed.refreshToken()).isNotBlank().isNotEqualTo(loggedIn.refreshToken());

            Long memberId = memberRepository.findByProviderAndSocialId(NAVER, "naver-id-refresh")
                .orElseThrow()
                .getId();
            List<RefreshToken> rows = refreshTokenRepository.findAllByMemberId(memberId);
            assertThat(rows).hasSize(2);

            RefreshToken consumed = rows.stream()
                .filter(row -> row.getTokenHash().equals(tokenHasher.hash(loggedIn.refreshToken())))
                .findFirst()
                .orElseThrow();
            RefreshToken active = rows.stream()
                .filter(row -> row.getTokenHash().equals(tokenHasher.hash(refreshed.refreshToken())))
                .findFirst()
                .orElseThrow();
            assertThat(consumed.getStatus()).isEqualTo(RefreshTokenStatus.DELETED);
            assertThat(consumed.getDeletedAt()).isNotNull();
            assertThat(active.getStatus()).isEqualTo(RefreshTokenStatus.ACTIVE);
            assertThat(active.getSessionId()).isEqualTo(consumed.getSessionId());
            assertThat(active.getExpiresAt()).isEqualTo(consumed.getExpiresAt().plus(Duration.ofHours(1)));
        }

        @Test
        @DisplayName("이미 회전된 토큰을 다시 제출하면 401 과 A0004 를 반환하지만 같은 세션의 최신 토큰은 계속 유효하다")
        void returns401ButKeepsSession_whenRotatedTokenIsReused() {
            naverApi.stubProfile("reuse-login-token", "naver-id-reuse");
            SocialLoginResponse loggedIn = fixture.socialLogin(SocialProvider.NAVER, "reuse-login-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();
            TokenRefreshResponse refreshed = fixture.refreshTokens(loggedIn.refreshToken())
                .expectStatus().isOk()
                .expectBody(TokenRefreshResponse.class)
                .returnResult()
                .getResponseBody();

            fixture.refreshTokens(loggedIn.refreshToken())
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN.getCode()));

            fixture.refreshTokens(refreshed.refreshToken()).expectStatus().isOk();
        }

        @Test
        @DisplayName("만료 시각이 지난 폐기(DELETED) 토큰을 재제출해도 만료가 아닌 A0004 로 거부되고 세션은 유지된다")
        void keepsSession_whenExpiredRotatedTokenIsResubmitted() {
            naverApi.stubProfile("stale-login-token", "naver-id-stale");
            SocialLoginResponse loggedIn = fixture.socialLogin(SocialProvider.NAVER, "stale-login-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();

            clock.advanceBy(Duration.ofDays(10));
            TokenRefreshResponse refreshed = fixture.refreshTokens(loggedIn.refreshToken())
                .expectStatus().isOk()
                .expectBody(TokenRefreshResponse.class)
                .returnResult()
                .getResponseBody();

            clock.advanceBy(Duration.ofDays(5));
            fixture.refreshTokens(loggedIn.refreshToken())
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN.getCode()));

            fixture.refreshTokens(refreshed.refreshToken()).expectStatus().isOk();
        }

        @Test
        @DisplayName("같은 토큰으로 동시에 재발급을 요청하면 한 건만 성공하고 세션에 ACTIVE 토큰은 하나만 남는다")
        void allowsSingleRotation_whenConcurrentRequestsRace() throws Exception {
            naverApi.stubProfile("race-login-token", "naver-id-race");
            SocialLoginResponse loggedIn = fixture.socialLogin(SocialProvider.NAVER, "race-login-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();
            CyclicBarrier barrier = new CyclicBarrier(2);
            Callable<Integer> attempt = () -> {
                barrier.await();
                return fixture.refreshTokens(loggedIn.refreshToken())
                    .expectBody(String.class)
                    .returnResult()
                    .getStatus()
                    .value();
            };

            List<Integer> statuses = new ArrayList<>();
            try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
                for (Future<Integer> future : executor.invokeAll(List.of(attempt, attempt))) {
                    statuses.add(future.get());
                }
            }

            assertThat(statuses).containsExactlyInAnyOrder(200, 401);
            Long memberId = memberRepository.findByProviderAndSocialId(NAVER, "naver-id-race")
                .orElseThrow()
                .getId();
            List<RefreshToken> rows = refreshTokenRepository.findAllByMemberId(memberId);
            assertThat(rows).filteredOn(row -> row.getStatus() == RefreshTokenStatus.ACTIVE).hasSize(1);
        }

        @Test
        @DisplayName("refresh TTL 이 지나 만료된 토큰으로 요청하면 매번 401 과 A0005 를 반환한다")
        void returns401WithExpiredCode_wheneverExpiredTokenIsPresented() {
            naverApi.stubProfile("expired-login-token", "naver-id-expired");
            SocialLoginResponse loggedIn = fixture.socialLogin(SocialProvider.NAVER, "expired-login-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();

            clock.advanceBy(Duration.ofDays(15));

            fixture.refreshTokens(loggedIn.refreshToken())
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.EXPIRED_REFRESH_TOKEN.getCode()));

            fixture.refreshTokens(loggedIn.refreshToken())
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.EXPIRED_REFRESH_TOKEN.getCode()));
        }

        @Test
        @DisplayName("존재하지 않는 refresh token 으로 요청하면 재사용 감지와 구별되지 않는 401 과 A0004 를 반환한다")
        void returns401_whenTokenUnknown() {
            fixture.refreshTokens("unknown-refresh-token")
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN.getCode()));
        }

        @Test
        @DisplayName("여러 기기로 로그인하면 세션이 공존하고 각자 독립적으로 회전한다")
        void rotatesIndependently_perDeviceSession() {
            naverApi.stubProfile("device-a-token", "naver-id-multi");
            SocialLoginResponse deviceA = fixture.socialLogin(SocialProvider.NAVER, "device-a-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();
            naverApi.stubProfile("device-b-token", "naver-id-multi");
            SocialLoginResponse deviceB = fixture.socialLogin(SocialProvider.NAVER, "device-b-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();

            fixture.refreshTokens(deviceA.refreshToken()).expectStatus().isOk();
            fixture.refreshTokens(deviceB.refreshToken()).expectStatus().isOk();

            Long memberId = memberRepository.findByProviderAndSocialId(NAVER, "naver-id-multi")
                .orElseThrow()
                .getId();
            List<RefreshToken> rows = refreshTokenRepository.findAllByMemberId(memberId);
            assertThat(rows).hasSize(4);
            assertThat(rows).filteredOn(row -> row.getStatus() == RefreshTokenStatus.ACTIVE).hasSize(2);
            assertThat(rows.stream().map(RefreshToken::getSessionId).distinct()).hasSize(2);
        }
    }
}
