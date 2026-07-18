package com.ssoss.ssossbackend.auth.entrypoint.scheduler;

import java.time.Duration;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;
import com.ssoss.ssossbackend.auth.domain.contract.TokenHasher;
import com.ssoss.ssossbackend.auth.domain.model.RefreshToken;
import com.ssoss.ssossbackend.auth.domain.model.RefreshTokenStatus;
import com.ssoss.ssossbackend.auth.domain.model.SocialProvider;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.member.domain.contract.MemberRepository;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.ssoss.ssossbackend.member.domain.model.SocialProvider.NAVER;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("만료 refresh token 정리 스케줄러")
class RefreshTokenCleanupSchedulerTest extends IntegrationTest {

    @Autowired
    private RefreshTokenCleanupScheduler refreshTokenCleanupScheduler;

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
    @DisplayName("cleanUpRefreshTokens")
    class CleanUpRefreshTokens {

        @Test
        @DisplayName("실행하면 만료 후 보존 기간이 지난 행만 삭제되고 보존 기간 내의 만료 세션과 폐기 이력은 유지된다")
        void deletesOnlyRowsPastRetention() {
            naverApi.stubProfile("cleanup-old-token", "naver-cleanup-old");
            fixture.socialLogin(SocialProvider.NAVER, "cleanup-old-token").expectStatus().isOk();

            clock.advanceBy(Duration.ofDays(20));
            naverApi.stubProfile("cleanup-recent-token", "naver-cleanup-recent");
            SocialLoginResponse recentLogin = fixture.socialLogin(SocialProvider.NAVER, "cleanup-recent-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();
            fixture.refreshTokens(recentLogin.refreshToken()).expectStatus().isOk();

            clock.advanceBy(Duration.ofDays(25));
            refreshTokenCleanupScheduler.cleanUpRefreshTokens();

            Long oldMemberId = memberRepository.findByProviderAndSocialId(NAVER, "naver-cleanup-old")
                .orElseThrow()
                .getId();
            assertThat(refreshTokenRepository.findAllByMemberId(oldMemberId)).isEmpty();
            assertThat(findRow("naver-cleanup-recent", recentLogin.refreshToken()).getStatus())
                .isEqualTo(RefreshTokenStatus.DELETED);
            assertThat(refreshTokenRepository.findAllByMemberId(
                memberRepository.findByProviderAndSocialId(NAVER, "naver-cleanup-recent").orElseThrow().getId()))
                .hasSize(2);
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
