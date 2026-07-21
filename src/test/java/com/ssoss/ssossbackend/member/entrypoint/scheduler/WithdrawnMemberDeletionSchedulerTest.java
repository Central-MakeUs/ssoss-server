package com.ssoss.ssossbackend.member.entrypoint.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.StreamSupport;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.member.domain.contract.MemberRepository;
import com.ssoss.ssossbackend.member.domain.contract.MemberTermRepository;
import com.ssoss.ssossbackend.member.domain.contract.MemberWithdrawalHistoryRepository;
import com.ssoss.ssossbackend.member.domain.model.Member;
import com.ssoss.ssossbackend.member.domain.model.MemberStatus;
import com.ssoss.ssossbackend.member.domain.model.MemberTerm;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.ssoss.ssossbackend.member.domain.model.SocialProvider.NAVER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("탈퇴 회원 삭제 스케줄러")
class WithdrawnMemberDeletionSchedulerTest extends IntegrationTest {

    private static final Duration PAST_GRACE_PERIOD = Member.RECOVERY_GRACE_PERIOD.plusSeconds(1);
    private static final Duration WITHIN_GRACE_PERIOD = Member.RECOVERY_GRACE_PERIOD.minusDays(1);

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
    @DisplayName("deleteWithdrawnMembers")
    class DeleteWithdrawnMembers {

        @Test
        @DisplayName("복구 유예 기간이 지난 탈퇴 회원은 회원·약관 동의·refresh token 이 모두 삭제된다")
        void deletesMemberWithRelatedRows_whenGracePeriodHasPassed() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-due");
            Long memberId = memberIdOf("naver-delete-due");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(memberRepository.findByProviderAndSocialId(NAVER, "naver-delete-due")).isEmpty();
            assertThat(termsOf(memberId)).isEmpty();
            assertThat(refreshTokenRepository.findAllByMemberId(memberId)).isEmpty();
        }

        @Test
        @DisplayName("삭제 대상이 여럿이면 모두 삭제되고 유예 기간 내 회원은 남는다")
        void deletesEveryDueMember_whenMultipleMembersArePastGracePeriod() {
            SignupResponse first = fixture.signupActiveMember("naver-delete-multi-first");
            SignupResponse second = fixture.signupActiveMember("naver-delete-multi-second");
            Long firstId = memberIdOf("naver-delete-multi-first");
            Long secondId = memberIdOf("naver-delete-multi-second");
            fixture.withdraw(first.accessToken()).expectStatus().isNoContent();
            fixture.withdraw(second.accessToken()).expectStatus().isNoContent();
            clock.advanceBy(PAST_GRACE_PERIOD);
            SignupResponse recent = fixture.signupActiveMember("naver-delete-multi-recent");
            Long recentId = memberIdOf("naver-delete-multi-recent");
            fixture.withdraw(recent.accessToken()).expectStatus().isNoContent();

            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(memberRepository.findByProviderAndSocialId(NAVER, "naver-delete-multi-first")).isEmpty();
            assertThat(memberRepository.findByProviderAndSocialId(NAVER, "naver-delete-multi-second")).isEmpty();
            assertThat(termsOf(firstId)).isEmpty();
            assertThat(termsOf(secondId)).isEmpty();
            assertThat(refreshTokenRepository.findAllByMemberId(firstId)).isEmpty();
            assertThat(refreshTokenRepository.findAllByMemberId(secondId)).isEmpty();
            assertThat(memberRepository.findByProviderAndSocialId(NAVER, "naver-delete-multi-recent")).isPresent();
            assertThat(termsOf(recentId)).isNotEmpty();
            assertThat(refreshTokenRepository.findAllByMemberId(recentId)).isNotEmpty();
        }

        @Test
        @DisplayName("회원이 삭제되어도 재가입 제한 판정에 쓰이는 탈퇴 이력은 남는다")
        void keepsWithdrawalHistory_whenMemberIsDeleted() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-history");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(memberWithdrawalHistoryRepository.findAll())
                .singleElement()
                .satisfies(history -> {
                    assertThat(history.getProvider()).isEqualTo(NAVER);
                    assertThat(history.getSocialId()).isEqualTo("naver-delete-history");
                });
        }

        @Test
        @DisplayName("복구 유예 기간이 지나지 않은 탈퇴 회원은 복구할 수 있도록 삭제하지 않는다")
        void keepsMember_whenStillWithinGracePeriod() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-recent");
            Long memberId = memberIdOf("naver-delete-recent");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(WITHIN_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(memberRepository.findByProviderAndSocialId(NAVER, "naver-delete-recent"))
                .get()
                .satisfies(member -> assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN));
            assertThat(termsOf(memberId)).isNotEmpty();
            assertThat(refreshTokenRepository.findAllByMemberId(memberId)).isNotEmpty();
        }

        @Test
        @DisplayName("탈퇴하지 않은 가입 대기 회원과 가입 회원은 삭제하지 않는다")
        void keepsPendingAndActiveMembers_whenDeletionRuns() {
            fixture.naverLoginMember("naver-delete-pending");
            fixture.signupActiveMember("naver-delete-active");

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(memberRepository.findByProviderAndSocialId(NAVER, "naver-delete-pending"))
                .get()
                .satisfies(member -> assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING));
            assertThat(memberRepository.findByProviderAndSocialId(NAVER, "naver-delete-active"))
                .get()
                .satisfies(member -> assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE));
        }

        @Test
        @DisplayName("복구한 회원은 탈퇴 시각이 남아 있어도 삭제하지 않는다")
        void keepsRecoveredMember_whenGracePeriodHasPassedSinceWithdrawal() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-recovered");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            String withdrawnAccessToken = fixture.naverLoginMember("naver-delete-recovered").accessToken();
            fixture.recover(withdrawnAccessToken).expectStatus().isOk();

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(memberRepository.findByProviderAndSocialId(NAVER, "naver-delete-recovered"))
                .get()
                .satisfies(member -> assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE));
        }
    }

    @Nested
    @DisplayName("삭제 대상 판정")
    class DeletionGuard {

        @Test
        @DisplayName("후보로 잡힌 뒤 다시 탈퇴해 유예 기간이 새로 시작된 회원은 삭제하지 않는다")
        void keepsMember_whenGracePeriodRestartedAfterBeingSelected() {
            SignupResponse due = fixture.signupActiveMember("naver-guard-due");
            fixture.withdraw(due.accessToken()).expectStatus().isNoContent();
            Long dueId = memberIdOf("naver-guard-due");
            clock.advanceBy(PAST_GRACE_PERIOD);
            SignupResponse rewithdrawn = fixture.signupActiveMember("naver-guard-rewithdrawn");
            fixture.withdraw(rewithdrawn.accessToken()).expectStatus().isNoContent();
            Long rewithdrawnId = memberIdOf("naver-guard-rewithdrawn");

            Instant threshold = clock.instant().minus(Member.RECOVERY_GRACE_PERIOD);
            memberRepository.deleteByIdAndStatusAndLastWithdrawnAtBefore(dueId, MemberStatus.WITHDRAWN, threshold);
            memberRepository.deleteByIdAndStatusAndLastWithdrawnAtBefore(
                rewithdrawnId, MemberStatus.WITHDRAWN, threshold);

            assertThat(memberRepository.findById(dueId)).isEmpty();
            assertThat(memberRepository.findById(rewithdrawnId)).isPresent();
        }
    }

    @Nested
    @DisplayName("삭제 실패 시")
    class WhenDeletionFails {

        @Test
        @DisplayName("리스너가 실패하면 예외가 전파되고 실패한 회원은 롤백되어 남는다")
        void propagatesFailureAndRollsBackMember_whenListenerFails() {
            SignupResponse signup = fixture.signupActiveMember("naver-failure-abort");
            Long memberId = memberIdOf("naver-failure-abort");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            clock.advanceBy(PAST_GRACE_PERIOD);
            failingMemberDeletedListener.failFor(memberId);

            assertThatThrownBy(withdrawnMemberDeletionScheduler::deleteWithdrawnMembers)
                .isInstanceOf(IllegalStateException.class);

            assertThat(memberRepository.findById(memberId)).isPresent();
            assertThat(termsOf(memberId)).isNotEmpty();
            assertThat(refreshTokenRepository.findAllByMemberId(memberId)).isNotEmpty();
        }

        @Test
        @DisplayName("실패로 남은 회원은 다음 배치가 마저 지운다")
        void deletesRemainingMember_whenNextBatchRuns() {
            SignupResponse signup = fixture.signupActiveMember("naver-failure-retry");
            Long memberId = memberIdOf("naver-failure-retry");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            clock.advanceBy(PAST_GRACE_PERIOD);
            failingMemberDeletedListener.failFor(memberId);
            assertThatThrownBy(withdrawnMemberDeletionScheduler::deleteWithdrawnMembers)
                .isInstanceOf(IllegalStateException.class);

            failingMemberDeletedListener.reset();
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(memberRepository.findById(memberId)).isEmpty();
            assertThat(termsOf(memberId)).isEmpty();
            assertThat(refreshTokenRepository.findAllByMemberId(memberId)).isEmpty();
        }
    }

    private Long memberIdOf(String socialId) {
        return memberRepository.findByProviderAndSocialId(NAVER, socialId).orElseThrow().getId();
    }

    private List<MemberTerm> termsOf(Long memberId) {
        return StreamSupport.stream(memberTermRepository.findAll().spliterator(), false)
            .filter(term -> term.getMemberId().equals(memberId))
            .toList();
    }
}
