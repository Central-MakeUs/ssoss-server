package com.ssoss.ssossbackend.auth.entrypoint.scheduler;

import java.time.Duration;
import java.time.Instant;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;
import com.ssoss.ssossbackend.auth.domain.contract.TokenHasher;
import com.ssoss.ssossbackend.auth.domain.model.RefreshToken;
import com.ssoss.ssossbackend.auth.domain.model.RefreshTokenStatus;
import com.ssoss.ssossbackend.auth.domain.model.SocialProvider;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.TokenRefreshResponse;
import com.ssoss.ssossbackend.member.domain.contract.MemberRepository;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.ssoss.ssossbackend.member.domain.model.SocialProvider.NAVER;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("만료 refresh token 마킹 스케줄러")
class RefreshTokenExpirySchedulerTest extends IntegrationTest {

    @Autowired
    private RefreshTokenExpiryScheduler refreshTokenExpiryScheduler;

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
    @DisplayName("expireRefreshTokens")
    class ExpireRefreshTokens {

        @Test
        @DisplayName("실행하면 만료된 ACTIVE 토큰 전건이 DELETED 와 deletedAt 으로 마킹되고 미만료 ACTIVE 와 기존 DELETED 이력은 변하지 않는다")
        void marksOnlyExpiredActiveTokens() {
            naverApi.stubProfile("marker-expired-token", "naver-id-marker-expired");
            SocialLoginResponse expiredLogin = fixture.socialLogin(SocialProvider.NAVER, "marker-expired-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();
            naverApi.stubProfile("marker-rotated-token", "naver-id-marker-rotated");
            SocialLoginResponse rotatedLogin = fixture.socialLogin(SocialProvider.NAVER, "marker-rotated-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();
            TokenRefreshResponse rotated = fixture.refreshTokens(rotatedLogin.refreshToken())
                .expectStatus().isOk()
                .expectBody(TokenRefreshResponse.class)
                .returnResult()
                .getResponseBody();

            clock.advanceBy(Duration.ofDays(15));
            naverApi.stubProfile("marker-fresh-token", "naver-id-marker-fresh");
            SocialLoginResponse freshLogin = fixture.socialLogin(SocialProvider.NAVER, "marker-fresh-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();
            Instant rotatedHistoryDeletedAt = findRow("naver-id-marker-rotated", rotatedLogin.refreshToken())
                .getDeletedAt();

            refreshTokenExpiryScheduler.expireRefreshTokens();

            RefreshToken expiredRow = findRow("naver-id-marker-expired", expiredLogin.refreshToken());
            assertThat(expiredRow.getStatus()).isEqualTo(RefreshTokenStatus.DELETED);
            assertThat(expiredRow.getDeletedAt()).isNotNull();
            RefreshToken rotatedNextRow = findRow("naver-id-marker-rotated", rotated.refreshToken());
            assertThat(rotatedNextRow.getStatus()).isEqualTo(RefreshTokenStatus.DELETED);
            assertThat(rotatedNextRow.getDeletedAt()).isNotNull();
            RefreshToken rotatedHistoryRow = findRow("naver-id-marker-rotated", rotatedLogin.refreshToken());
            assertThat(rotatedHistoryRow.getStatus()).isEqualTo(RefreshTokenStatus.DELETED);
            assertThat(rotatedHistoryRow.getDeletedAt()).isEqualTo(rotatedHistoryDeletedAt);
            RefreshToken freshRow = findRow("naver-id-marker-fresh", freshLogin.refreshToken());
            assertThat(freshRow.getStatus()).isEqualTo(RefreshTokenStatus.ACTIVE);
            assertThat(freshRow.getDeletedAt()).isNull();
        }

        private RefreshToken findRow(String socialId, String rawRefreshToken) {
            Long memberId = memberRepository.findByProviderAndSocialId(NAVER, socialId)
                .orElseThrow()
                .getId();
            return refreshTokenRepository.findAllByMemberId(memberId).stream()
                .filter(row -> row.getTokenHash().equals(tokenHasher.hash(rawRefreshToken)))
                .findFirst()
                .orElseThrow();
        }
    }
}
