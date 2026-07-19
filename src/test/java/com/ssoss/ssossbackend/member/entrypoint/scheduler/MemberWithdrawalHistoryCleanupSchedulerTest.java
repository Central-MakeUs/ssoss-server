package com.ssoss.ssossbackend.member.entrypoint.scheduler;

import java.time.Duration;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.member.domain.contract.MemberRepository;
import com.ssoss.ssossbackend.member.domain.contract.MemberTermRepository;
import com.ssoss.ssossbackend.member.domain.contract.MemberWithdrawalHistoryRepository;
import com.ssoss.ssossbackend.member.domain.model.MemberWithdrawalHistory;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.ssoss.ssossbackend.member.domain.model.SocialProvider.NAVER;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("탈퇴 이력 정리 스케줄러")
class MemberWithdrawalHistoryCleanupSchedulerTest extends IntegrationTest {

    private static final Duration PAST_RESTRICTION_PERIOD =
        MemberWithdrawalHistory.SIGNUP_RESTRICTION_PERIOD.plusSeconds(1);
    private static final Duration WITHIN_RESTRICTION_PERIOD =
        MemberWithdrawalHistory.SIGNUP_RESTRICTION_PERIOD.minusDays(1);

    @Autowired
    private MemberWithdrawalHistoryCleanupScheduler memberWithdrawalHistoryCleanupScheduler;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberTermRepository memberTermRepository;

    @Autowired
    private MemberWithdrawalHistoryRepository memberWithdrawalHistoryRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void resetDatabase() {
        memberWithdrawalHistoryRepository.deleteAll();
        memberTermRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("cleanUpWithdrawalHistories")
    class CleanUpWithdrawalHistories {

        @Test
        @DisplayName("재가입 제한 기간이 지난 탈퇴 이력은 삭제된다")
        void deletesHistory_whenRestrictionPeriodHasPassed() {
            SignupResponse signup = fixture.signupActiveMember("naver-history-old");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(PAST_RESTRICTION_PERIOD);
            memberWithdrawalHistoryCleanupScheduler.cleanUpWithdrawalHistories();

            assertThat(memberWithdrawalHistoryRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("재가입 제한 기간이 지나지 않은 탈퇴 이력은 유지된다")
        void keepsHistory_whenStillWithinRestrictionPeriod() {
            SignupResponse signup = fixture.signupActiveMember("naver-history-recent");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(WITHIN_RESTRICTION_PERIOD);
            memberWithdrawalHistoryCleanupScheduler.cleanUpWithdrawalHistories();

            assertThat(memberWithdrawalHistoryRepository.findAll())
                .singleElement()
                .satisfies(history -> assertThat(history.getSocialId()).isEqualTo("naver-history-recent"));
        }

        @Test
        @DisplayName("제한 기간이 지난 이력만 삭제하고 기간 내 이력은 남긴다")
        void deletesOnlyHistoriesPastRestrictionPeriod() {
            SignupResponse oldSignup = fixture.signupActiveMember("naver-history-mixed-old");
            fixture.withdraw(oldSignup.accessToken()).expectStatus().isNoContent();
            clock.advanceBy(WITHIN_RESTRICTION_PERIOD);
            SignupResponse recentSignup = fixture.signupActiveMember("naver-history-mixed-recent");
            fixture.withdraw(recentSignup.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(Duration.ofDays(1).plusSeconds(1));
            memberWithdrawalHistoryCleanupScheduler.cleanUpWithdrawalHistories();

            assertThat(memberWithdrawalHistoryRepository.findAll())
                .singleElement()
                .satisfies(history -> assertThat(history.getSocialId()).isEqualTo("naver-history-mixed-recent"));
        }
    }
}
