package com.ssoss.ssossbackend.member.entrypoint.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;
import com.ssoss.ssossbackend.auth.domain.contract.SocialLoginRepository;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.content.domain.contract.ContentChannelHistoryRepository;
import com.ssoss.ssossbackend.content.domain.contract.ContentChannelRepository;
import com.ssoss.ssossbackend.content.domain.contract.ContentRepository;
import com.ssoss.ssossbackend.content.domain.contract.GenerationRepository;
import com.ssoss.ssossbackend.content.domain.contract.GenerationResultRepository;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentSaveResponse;
import com.ssoss.ssossbackend.credit.domain.contract.CreditLedgerRepository;
import com.ssoss.ssossbackend.credit.domain.contract.CreditRepository;
import com.ssoss.ssossbackend.credit.entrypoint.response.CreditBalanceResponse;
import com.ssoss.ssossbackend.hashtag.domain.contract.HashtagBundleBookmarkRepository;
import com.ssoss.ssossbackend.member.domain.contract.MemberRepository;
import com.ssoss.ssossbackend.member.domain.contract.MemberTermRepository;
import com.ssoss.ssossbackend.member.domain.contract.MemberWithdrawalHistoryRepository;
import com.ssoss.ssossbackend.member.domain.contract.MemberWithdrawalReasonRepository;
import com.ssoss.ssossbackend.member.domain.model.Member;
import com.ssoss.ssossbackend.member.domain.model.MemberStatus;
import com.ssoss.ssossbackend.member.domain.model.WithdrawalReason;
import com.ssoss.ssossbackend.store.domain.contract.StoreRepository;
import com.ssoss.ssossbackend.support.IntegrationTest;
import com.ssoss.ssossbackend.template.domain.contract.SavedTemplateHistoryRepository;
import com.ssoss.ssossbackend.template.domain.contract.SavedTemplateRepository;
import com.ssoss.ssossbackend.template.domain.model.SavedTemplate;
import com.ssoss.ssossbackend.template.domain.model.SavedTemplateHistory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

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
    private MemberWithdrawalReasonRepository memberWithdrawalReasonRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private SocialLoginRepository socialLoginRepository;

    @Autowired
    private CreditRepository creditRepository;

    @Autowired
    private CreditLedgerRepository creditLedgerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private HashtagBundleBookmarkRepository hashtagBundleBookmarkRepository;

    @Autowired
    private GenerationRepository generationRepository;

    @Autowired
    private GenerationResultRepository generationResultRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private ContentChannelRepository contentChannelRepository;

    @Autowired
    private ContentChannelHistoryRepository contentChannelHistoryRepository;

    @Autowired
    private SavedTemplateRepository savedTemplateRepository;

    @Autowired
    private SavedTemplateHistoryRepository savedTemplateHistoryRepository;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void resetDatabase() {
        memberWithdrawalHistoryRepository.deleteAll();
        memberWithdrawalReasonRepository.deleteAll();
        memberTermRepository.deleteAll();
        socialLoginRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        creditLedgerRepository.deleteAll();
        creditRepository.deleteAll();
        storeRepository.deleteAll();
        hashtagBundleBookmarkRepository.deleteAll();
        savedTemplateHistoryRepository.deleteAll();
        savedTemplateRepository.deleteAll();
        contentChannelHistoryRepository.deleteAll();
        contentChannelRepository.deleteAll();
        contentRepository.deleteAll();
        generationResultRepository.deleteAll();
        generationRepository.deleteAll();
        jdbcClient.sql("DELETE FROM generation_lock").update();
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("deleteWithdrawnMembers")
    class DeleteWithdrawnMembers {

        @Test
        @DisplayName("복구 유예 기간이 지난 탈퇴 회원은 회원·약관 동의·refresh token·매장·북마크가 모두 삭제된다")
        void deletesMemberWithRelatedRows_whenGracePeriodHasPassed() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-due");
            Long memberId = database.memberIdOf("naver-delete-due");
            Long bundleId = fixture.firstBundleId(signup.accessToken());
            fixture.bookmarkedHashtagBundle(signup.accessToken(), bundleId);
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(memberRepository.findByProviderAndSocialId(NAVER, "naver-delete-due")).isEmpty();
            assertThat(database.termsOf(memberId)).isEmpty();
            assertThat(refreshTokenRepository.findAllByMemberId(memberId)).isEmpty();
            assertThat(storeRepository.findByMemberId(memberId)).isEmpty();
            assertThat(database.hashtagBundleBookmarksOf(memberId)).isEmpty();
        }

        @Test
        @DisplayName("탈퇴 대기 중 재로그인한 회원이 삭제되면 소셜 로그인 행도 사라지고 연결 해제가 호출된다")
        void deletesSocialLoginAndUnlinks_whenReloggedMemberIsDeleted() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-relogin");
            Long memberId = database.memberIdOf("naver-delete-relogin");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            fixture.naverLogin("naver-delete-relogin").expectStatus().isOk();
            naverApi.reset();

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(socialLoginRepository.findByMemberId(memberId)).isEmpty();
            assertThat(naverApi.revokeRequestBodies()).hasSize(1);
        }

        @Test
        @DisplayName("탈퇴 회원이 삭제되면 소셜 로그인 행도 사라진다")
        void deletesSocialLogin_whenMemberIsDeleted() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-social");
            Long memberId = database.memberIdOf("naver-delete-social");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(socialLoginRepository.findByMemberId(memberId)).isEmpty();
        }

        @Test
        @DisplayName("삭제 대상이 여럿이면 모두 삭제되고 유예 기간 내 회원은 남는다")
        void deletesEveryDueMember_whenMultipleMembersArePastGracePeriod() {
            SignupResponse first = fixture.signupActiveMember("naver-delete-multi-first");
            SignupResponse second = fixture.signupActiveMember("naver-delete-multi-second");
            Long firstId = database.memberIdOf("naver-delete-multi-first");
            Long secondId = database.memberIdOf("naver-delete-multi-second");
            fixture.withdraw(first.accessToken()).expectStatus().isNoContent();
            fixture.withdraw(second.accessToken()).expectStatus().isNoContent();
            clock.advanceBy(PAST_GRACE_PERIOD);
            SignupResponse recent = fixture.signupActiveMember("naver-delete-multi-recent");
            Long recentId = database.memberIdOf("naver-delete-multi-recent");
            fixture.withdraw(recent.accessToken()).expectStatus().isNoContent();

            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(memberRepository.findByProviderAndSocialId(NAVER, "naver-delete-multi-first")).isEmpty();
            assertThat(memberRepository.findByProviderAndSocialId(NAVER, "naver-delete-multi-second")).isEmpty();
            assertThat(database.termsOf(firstId)).isEmpty();
            assertThat(database.termsOf(secondId)).isEmpty();
            assertThat(refreshTokenRepository.findAllByMemberId(firstId)).isEmpty();
            assertThat(refreshTokenRepository.findAllByMemberId(secondId)).isEmpty();
            assertThat(memberRepository.findByProviderAndSocialId(NAVER, "naver-delete-multi-recent")).isPresent();
            assertThat(database.termsOf(recentId)).isNotEmpty();
            assertThat(refreshTokenRepository.findAllByMemberId(recentId)).isNotEmpty();
        }

        @Test
        @DisplayName("복구 유예 기간이 지난 탈퇴 회원은 크레딧 잔액과 원장도 삭제된다")
        void deletesCreditRows_whenGracePeriodHasPassed() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-credit");
            Long memberId = database.memberIdOf("naver-delete-credit");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(creditRepository.findByMemberId(memberId)).isEmpty();
            assertThat(database.ledgerOf(memberId)).isEmpty();
        }

        @Test
        @DisplayName("북마크를 해제해 남은 행도 회원이 삭제되면 함께 사라진다")
        void deletesUnbookmarkedRow_whenMemberIsDeleted() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-bookmark-released");
            Long memberId = database.memberIdOf("naver-delete-bookmark-released");
            Long bundleId = fixture.firstBundleId(signup.accessToken());
            fixture.bookmarkedHashtagBundle(signup.accessToken(), bundleId);
            fixture.unbookmarkedHashtagBundle(signup.accessToken(), bundleId);
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(database.hashtagBundleBookmarksOf(memberId)).isEmpty();
        }

        @Test
        @DisplayName("북마크가 하나도 없는 회원도 문제 없이 삭제된다")
        void deletesMember_whenMemberHasNoBookmark() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-bookmark-none");
            Long memberId = database.memberIdOf("naver-delete-bookmark-none");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(memberRepository.findById(memberId)).isEmpty();
            assertThat(database.hashtagBundleBookmarksOf(memberId)).isEmpty();
        }

        @Test
        @DisplayName("복구 유예 기간이 지난 탈퇴 회원은 저장한 템플릿도 삭제된다")
        void deletesSavedTemplates_whenGracePeriodHasPassed() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-saved-template");
            Long memberId = database.memberIdOf("naver-delete-saved-template");
            Long templateId = fixture.firstTemplate(signup.accessToken()).id();
            fixture.savedTemplate(signup.accessToken(), templateId, "탈퇴하면 사라질 본문");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(memberRepository.findById(memberId)).isEmpty();
            assertThat(database.savedTemplatesOf(memberId)).isEmpty();
        }

        @Test
        @DisplayName("탈퇴 회원의 저장한 템플릿이 삭제돼도 다른 회원이 저장한 글과 원본 템플릿은 남는다")
        void keepsOtherMembersSavedTemplateAndOrigin_whenMemberIsDeleted() {
            SignupResponse due = fixture.signupActiveMember("naver-delete-saved-template-due");
            SignupResponse kept = fixture.signupActiveMember("naver-delete-saved-template-kept");
            Long dueId = database.memberIdOf("naver-delete-saved-template-due");
            Long keptId = database.memberIdOf("naver-delete-saved-template-kept");
            Long templateId = fixture.firstTemplate(due.accessToken()).id();
            fixture.savedTemplate(due.accessToken(), templateId, "탈퇴 회원의 본문");
            fixture.savedTemplate(kept.accessToken(), templateId, "남는 회원의 본문");
            fixture.withdraw(due.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(database.savedTemplatesOf(dueId)).isEmpty();
            assertThat(database.savedTemplatesOf(keptId)).singleElement()
                .extracting(SavedTemplate::getBody)
                .isEqualTo("남는 회원의 본문");
            assertThat(fixture.templateDetail(kept.accessToken(), templateId).id()).isEqualTo(templateId);
        }

        @Test
        @DisplayName("탈퇴 회원의 저장한 템플릿 편집 히스토리도 함께 삭제된다")
        void deletesSavedTemplateHistories_whenGracePeriodHasPassed() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-saved-template-history");
            Long memberId = database.memberIdOf("naver-delete-saved-template-history");
            Long templateId = fixture.firstTemplate(signup.accessToken()).id();
            Long savedTemplateId =
                fixture.savedTemplate(signup.accessToken(), templateId, "탈퇴하면 사라질 본문").savedTemplateId();
            fixture.editedSavedTemplate(signup.accessToken(), savedTemplateId, "고친 제목", "고친 본문");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(memberRepository.findById(memberId)).isEmpty();
            assertThat(database.savedTemplatesOf(memberId)).isEmpty();
            assertThat(database.savedTemplateHistoriesOf(savedTemplateId)).isEmpty();
        }

        @Test
        @DisplayName("탈퇴 회원의 편집 히스토리가 삭제돼도 다른 회원의 히스토리는 남는다")
        void keepsOtherMembersSavedTemplateHistory_whenMemberIsDeleted() {
            SignupResponse due = fixture.signupActiveMember("naver-delete-history-due");
            SignupResponse kept = fixture.signupActiveMember("naver-delete-history-kept");
            Long templateId = fixture.firstTemplate(due.accessToken()).id();
            Long dueSavedTemplateId =
                fixture.savedTemplate(due.accessToken(), templateId, "탈퇴 회원의 본문").savedTemplateId();
            Long keptSavedTemplateId =
                fixture.savedTemplate(kept.accessToken(), templateId, "남는 회원의 본문").savedTemplateId();
            fixture.editedSavedTemplate(due.accessToken(), dueSavedTemplateId, "고친 제목", "탈퇴 회원이 고친 본문");
            fixture.editedSavedTemplate(kept.accessToken(), keptSavedTemplateId, "고친 제목", "남는 회원이 고친 본문");
            fixture.withdraw(due.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(database.savedTemplateHistoriesOf(dueSavedTemplateId)).isEmpty();
            assertThat(database.savedTemplateHistoriesOf(keptSavedTemplateId))
                .singleElement()
                .extracting(SavedTemplateHistory::getBody)
                .isEqualTo("남는 회원의 본문");
        }

        @Test
        @DisplayName("저장한 템플릿이 하나도 없는 회원도 문제 없이 삭제된다")
        void deletesMember_whenMemberHasNoSavedTemplate() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-saved-template-none");
            Long memberId = database.memberIdOf("naver-delete-saved-template-none");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(memberRepository.findById(memberId)).isEmpty();
            assertThat(database.savedTemplatesOf(memberId)).isEmpty();
        }

        @Test
        @DisplayName("탈퇴 회원의 북마크가 삭제돼도 다른 회원의 같은 묶음 북마크는 남는다")
        void keepsOtherMembersBookmark_whenMemberIsDeleted() {
            SignupResponse due = fixture.signupActiveMember("naver-delete-bookmark-due");
            SignupResponse kept = fixture.signupActiveMember("naver-delete-bookmark-kept");
            Long dueId = database.memberIdOf("naver-delete-bookmark-due");
            Long keptId = database.memberIdOf("naver-delete-bookmark-kept");
            Long bundleId = fixture.firstBundleId(due.accessToken());
            fixture.bookmarkedHashtagBundle(due.accessToken(), bundleId);
            fixture.bookmarkedHashtagBundle(kept.accessToken(), bundleId);
            fixture.withdraw(due.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(database.hashtagBundleBookmarksOf(dueId)).isEmpty();
            assertThat(database.hashtagBundleBookmarksOf(keptId)).singleElement()
                .satisfies(bookmark -> assertThat(bookmark.getBundleId()).isEqualTo(bundleId));
        }

        @Test
        @DisplayName("복구 유예 기간이 지난 탈퇴 회원은 생성 작업·생성 결과·콘텐츠·채널별 콘텐츠·편집 히스토리가 모두 삭제된다")
        void deletesContentRows_whenGracePeriodHasPassed() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-content");
            Long memberId = database.memberIdOf("naver-delete-content");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);
            Long contentChannelId = saved.contents().getFirst().contentChannelId();
            fixture.editContentChannel(signup.accessToken(), saved.contentId(), contentChannelId, Map.of(
                    "title", "직접 고친 제목",
                    "body", "직접 고친 본문",
                    "hashtags", List.of("#직접고친태그")))
                .expectStatus().isOk();
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(database.generationsOf(memberId)).isEmpty();
            assertThat(database.resultsOf(generationId)).isEmpty();
            assertThat(database.contentsOf(memberId)).isEmpty();
            assertThat(database.channelsOf(memberId)).isEmpty();
            assertThat(database.historiesOf(contentChannelId)).isEmpty();
            assertThat(database.generationLocksOf(memberId)).isZero();
        }

        @Test
        @DisplayName("콘텐츠를 만든 적 없는 회원도 문제 없이 삭제된다")
        void deletesMember_whenMemberHasNoContent() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-content-none");
            Long memberId = database.memberIdOf("naver-delete-content-none");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(memberRepository.findById(memberId)).isEmpty();
        }

        @Test
        @DisplayName("탈퇴 회원의 콘텐츠가 삭제돼도 다른 회원의 생성 기록과 콘텐츠는 그대로 조회된다")
        void keepsOtherMembersContent_whenMemberIsDeleted() {
            SignupResponse due = fixture.signupActiveMember("naver-delete-content-due");
            SignupResponse kept = fixture.signupActiveMember("naver-delete-content-kept");
            Long dueId = database.memberIdOf("naver-delete-content-due");
            Long keptId = database.memberIdOf("naver-delete-content-kept");
            Long dueGenerationId = fixture.startedGenerationId(due.accessToken(), List.of("BLOG"));
            Long keptGenerationId = fixture.startedGenerationId(kept.accessToken(), List.of("BLOG"));
            fixture.contentsOfGeneration(due.accessToken(), dueGenerationId);
            Long keptContentId = fixture.savedContentId(kept.accessToken(), keptGenerationId);
            fixture.withdraw(due.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            fixture.getGeneration(kept.accessToken(), keptGenerationId).expectStatus().isOk();
            assertThat(fixture.contentList(kept.accessToken(), "").contents())
                .singleElement()
                .satisfies(card -> assertThat(card.contentId()).isEqualTo(keptContentId));
            assertThat(fixture.contentDetail(kept.accessToken(), keptContentId).contents()).hasSize(1);
            assertThat(database.generationsOf(dueId)).isEmpty();
            assertThat(database.resultsOf(dueGenerationId)).isEmpty();
            assertThat(database.contentsOf(dueId)).isEmpty();
            assertThat(database.channelsOf(dueId)).isEmpty();
            assertThat(database.generationLocksOf(dueId)).isZero();
            assertThat(database.generationLocksOf(keptId)).isOne();
        }

        @Test
        @DisplayName("유예 기간 안에 복구한 회원은 생성 기록·콘텐츠·잔여 크레딧이 탈퇴 전 그대로다")
        void keepsContent_whenMemberRecoveredWithinGracePeriod() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-content-recovered");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            Long contentId = fixture.savedContentId(signup.accessToken(), generationId);
            int balanceBefore = fixture.creditBalance(signup.accessToken()).balance();
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            String withdrawnToken = fixture.naverLoginMember("naver-delete-content-recovered").accessToken();
            String recoveredToken = fixture.recovered(withdrawnToken).accessToken();

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            fixture.getGeneration(recoveredToken, generationId).expectStatus().isOk();
            assertThat(fixture.contentList(recoveredToken, "").contents())
                .singleElement()
                .satisfies(card -> assertThat(card.contentId()).isEqualTo(contentId));
            assertThat(fixture.contentDetail(recoveredToken, contentId).contents()).hasSize(1);
            assertThat(fixture.creditBalance(recoveredToken).balance()).isEqualTo(balanceBefore);
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
        @DisplayName("회원이 삭제되어도 회원을 식별하지 않는 탈퇴 사유는 남는다")
        void keepsWithdrawalReason_whenMemberIsDeleted() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-reason");
            fixture.withdraw(signup.accessToken(), "HARD_TO_USE", null).expectStatus().isNoContent();

            clock.advanceBy(PAST_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(memberWithdrawalReasonRepository.findAll())
                .singleElement()
                .satisfies(collected -> assertThat(collected.getReason()).isEqualTo(WithdrawalReason.HARD_TO_USE));
        }

        @Test
        @DisplayName("복구 유예 기간이 지나지 않은 탈퇴 회원은 복구할 수 있도록 삭제하지 않는다")
        void keepsMember_whenStillWithinGracePeriod() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-recent");
            Long memberId = database.memberIdOf("naver-delete-recent");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();

            clock.advanceBy(WITHIN_GRACE_PERIOD);
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(memberRepository.findByProviderAndSocialId(NAVER, "naver-delete-recent"))
                .get()
                .satisfies(member -> assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN));
            assertThat(database.termsOf(memberId)).isNotEmpty();
            assertThat(refreshTokenRepository.findAllByMemberId(memberId)).isNotEmpty();
            assertThat(storeRepository.findByMemberId(memberId)).isPresent();
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
            Long dueId = database.memberIdOf("naver-guard-due");
            clock.advanceBy(PAST_GRACE_PERIOD);
            SignupResponse rewithdrawn = fixture.signupActiveMember("naver-guard-rewithdrawn");
            fixture.withdraw(rewithdrawn.accessToken()).expectStatus().isNoContent();
            Long rewithdrawnId = database.memberIdOf("naver-guard-rewithdrawn");

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
            Long memberId = database.memberIdOf("naver-failure-abort");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            Long contentId = fixture.savedContentId(signup.accessToken(), generationId);
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            clock.advanceBy(PAST_GRACE_PERIOD);
            failingMemberDeletedListener.failFor(memberId);

            assertThatThrownBy(withdrawnMemberDeletionScheduler::deleteWithdrawnMembers)
                .isInstanceOf(IllegalStateException.class);

            assertThat(memberRepository.findById(memberId)).isPresent();
            assertThat(database.termsOf(memberId)).isNotEmpty();
            assertThat(refreshTokenRepository.findAllByMemberId(memberId)).isNotEmpty();
            fixture.getGeneration(signup.accessToken(), generationId).expectStatus().isOk();
            assertThat(fixture.contentDetail(signup.accessToken(), contentId).contents()).hasSize(1);
        }

        @Test
        @DisplayName("실패로 남은 회원은 다음 배치가 마저 지운다")
        void deletesRemainingMember_whenNextBatchRuns() {
            SignupResponse signup = fixture.signupActiveMember("naver-failure-retry");
            Long memberId = database.memberIdOf("naver-failure-retry");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            clock.advanceBy(PAST_GRACE_PERIOD);
            failingMemberDeletedListener.failFor(memberId);
            assertThatThrownBy(withdrawnMemberDeletionScheduler::deleteWithdrawnMembers)
                .isInstanceOf(IllegalStateException.class);

            failingMemberDeletedListener.reset();
            withdrawnMemberDeletionScheduler.deleteWithdrawnMembers();

            assertThat(memberRepository.findById(memberId)).isEmpty();
            assertThat(database.termsOf(memberId)).isEmpty();
            assertThat(refreshTokenRepository.findAllByMemberId(memberId)).isEmpty();
        }
    }
}
