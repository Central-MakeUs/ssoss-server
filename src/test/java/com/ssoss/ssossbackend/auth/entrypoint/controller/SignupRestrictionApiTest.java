package com.ssoss.ssossbackend.auth.entrypoint.controller;

import java.time.Duration;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.member.domain.contract.MemberRepository;
import com.ssoss.ssossbackend.member.domain.contract.MemberTermRepository;
import com.ssoss.ssossbackend.member.domain.contract.MemberWithdrawalHistoryRepository;
import com.ssoss.ssossbackend.member.domain.model.Member;
import com.ssoss.ssossbackend.member.domain.model.MemberErrorCode;
import com.ssoss.ssossbackend.member.domain.model.MemberWithdrawalHistory;
import com.ssoss.ssossbackend.member.entrypoint.scheduler.WithdrawnMemberDeletionScheduler;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.ssoss.ssossbackend.member.domain.model.SocialProvider.NAVER;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("탈퇴 후 재가입 제한")
class SignupRestrictionApiTest extends IntegrationTest {

    private static final Duration PAST_GRACE_PERIOD = Member.RECOVERY_GRACE_PERIOD.plusSeconds(1);
    private static final Duration PAST_RESTRICTION_PERIOD =
        MemberWithdrawalHistory.SIGNUP_RESTRICTION_PERIOD.plusSeconds(1);

    @Autowired
    private WithdrawnMemberDeletionScheduler withdrawnMemberDeletionScheduler;

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
    @DisplayName("재가입 제한 기간 내")
    class WithinRestrictionPeriod {

        @Test
        @DisplayName("삭제된 회원이 같은 소셜 계정으로 로그인하면 403 과 M0005 를 반환한다")
        void returns403_whenDeletedSocialAccountLogsInWithinRestrictionPeriod() {
            SignupResponse signup = fixture.signupActiveMember("naver-signup-blocked");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            fixture.naverLogin("naver-signup-blocked")
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> {
                    assertThat(body.code()).isEqualTo(MemberErrorCode.SIGNUP_RESTRICTED.getCode());
                    assertThat(body.message()).isEqualTo("탈퇴 후 2개월이 지나야 다시 가입할 수 있습니다");
                });
        }

        @Test
        @DisplayName("로그인 시도는 가입 대기 회원을 다시 만들지 않는다")
        void doesNotRecreateMember_whenSignupIsRestricted() {
            SignupResponse signup = fixture.signupActiveMember("naver-signup-no-row");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            fixture.naverLogin("naver-signup-no-row").expectStatus().isForbidden();

            assertThat(memberRepository.findByProviderAndSocialId(NAVER, "naver-signup-no-row")).isEmpty();
        }
    }

    @Nested
    @DisplayName("재가입 제한 기간 경과 후")
    class AfterRestrictionPeriod {

        @Test
        @DisplayName("이력 정리 배치가 돌지 않아도 제한 기간이 지나면 신규 가입 흐름을 탄다")
        void allowsSignup_whenRestrictionPeriodHasPassed() {
            SignupResponse signup = fixture.signupActiveMember("naver-signup-allowed");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            clock.advanceBy(PAST_RESTRICTION_PERIOD);

            fixture.naverLogin("naver-signup-allowed")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .value(body -> assertThat(body.status()).isEqualTo("PENDING"));
        }
    }
}
