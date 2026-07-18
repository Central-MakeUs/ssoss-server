package com.ssoss.ssossbackend.auth.entrypoint.controller;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;
import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.entrypoint.response.RecoveryResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.TokenRefreshResponse;
import com.ssoss.ssossbackend.member.domain.contract.MemberRepository;
import com.ssoss.ssossbackend.member.domain.contract.MemberTermRepository;
import com.ssoss.ssossbackend.member.domain.contract.MemberWithdrawalHistoryRepository;
import com.ssoss.ssossbackend.member.domain.model.Member;
import com.ssoss.ssossbackend.member.domain.model.MemberErrorCode;
import com.ssoss.ssossbackend.member.domain.model.MemberStatus;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static com.ssoss.ssossbackend.member.domain.model.SocialProvider.NAVER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("복구 API")
class RecoveryApiTest extends IntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private MemberTermRepository memberTermRepository;

    @Autowired
    private MemberWithdrawalHistoryRepository memberWithdrawalHistoryRepository;

    @BeforeEach
    void resetDatabase() {
        memberWithdrawalHistoryRepository.deleteAll();
        memberTermRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /v1/members/me/recovery")
    class Recover {

        @Test
        @DisplayName("탈퇴 대기 회원이 복구하면 200 과 가입 회원 상태·정식 토큰 쌍이 응답되고 가입 회원으로 복원된다")
        void restoresActiveMemberAndIssuesTokenPair_whenWithdrawnMemberRecovers() {
            SignupResponse signup = fixture.signupActiveMember("naver-recovery");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            clock.advanceBy(Duration.ofHours(1));
            SocialLoginResponse withdrawnLogin = fixture.naverLoginMember("naver-recovery");

            fixture.recover(withdrawnLogin.accessToken())
                .expectStatus().isOk()
                .expectBody(RecoveryResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("ACTIVE");
                    assertThat(jwtTestSupport.roleOf(body.accessToken())).isEqualTo("ACTIVE");
                    assertThat(body.refreshToken()).isNotBlank();
                });

            Member member = memberRepository.findByProviderAndSocialId(NAVER, "naver-recovery").orElseThrow();
            assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        }

        @Test
        @DisplayName("복구해도 마지막 탈퇴 시각은 지워지지 않는다")
        void keepsLastWithdrawnAt_whenWithdrawnMemberRecovers() {
            SignupResponse signup = fixture.signupActiveMember("naver-recovery-withdrawn-at");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            Instant withdrawnAt = clock.instant();
            clock.advanceBy(Duration.ofHours(1));
            SocialLoginResponse withdrawnLogin = fixture.naverLoginMember("naver-recovery-withdrawn-at");

            fixture.recover(withdrawnLogin.accessToken()).expectStatus().isOk();

            Member member = memberRepository.findByProviderAndSocialId(NAVER, "naver-recovery-withdrawn-at")
                .orElseThrow();
            assertThat(member.getWithdrawnAt()).isCloseTo(withdrawnAt, within(1, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("복구하면 기존 회원 데이터가 그대로 유지된다")
        void keepsExistingMemberData_whenWithdrawnMemberRecovers() {
            SignupResponse signup = fixture.signupActiveMember("naver-recovery-data");
            Long memberId = memberRepository.findByProviderAndSocialId(NAVER, "naver-recovery-data")
                .orElseThrow()
                .getId();
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            clock.advanceBy(Duration.ofHours(1));
            SocialLoginResponse withdrawnLogin = fixture.naverLoginMember("naver-recovery-data");

            fixture.recover(withdrawnLogin.accessToken()).expectStatus().isOk();

            Member member = memberRepository.findByProviderAndSocialId(NAVER, "naver-recovery-data").orElseThrow();
            assertThat(member.getId()).isEqualTo(memberId);
            assertThat(memberTermRepository.findAll())
                .singleElement()
                .satisfies(term -> assertThat(term.getMemberId()).isEqualTo(memberId));
        }

        @Test
        @DisplayName("복구 후 탈퇴 전에 발급된 refresh token 으로 재발급하면 401 과 A0004 를 반환한다")
        void blocksTokenRefresh_withRefreshTokenIssuedBeforeWithdrawal() {
            SignupResponse signup = fixture.signupActiveMember("naver-recovery-old-refresh");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            clock.advanceBy(Duration.ofHours(1));
            SocialLoginResponse withdrawnLogin = fixture.naverLoginMember("naver-recovery-old-refresh");
            fixture.recover(withdrawnLogin.accessToken()).expectStatus().isOk();

            fixture.refreshTokens(signup.refreshToken())
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN.getCode()));
        }

        @Test
        @DisplayName("복구 시 발급된 refresh token 으로 재발급하면 ACTIVE 토큰이 발급된다")
        void refreshesActiveTokens_withRefreshTokenIssuedByRecovery() {
            SignupResponse signup = fixture.signupActiveMember("naver-recovery-new-refresh");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            clock.advanceBy(Duration.ofHours(1));
            SocialLoginResponse withdrawnLogin = fixture.naverLoginMember("naver-recovery-new-refresh");
            RecoveryResponse recovery = fixture.recover(withdrawnLogin.accessToken())
                .expectStatus().isOk()
                .expectBody(RecoveryResponse.class)
                .returnResult()
                .getResponseBody();

            fixture.refreshTokens(recovery.refreshToken())
                .expectStatus().isOk()
                .expectBody(TokenRefreshResponse.class)
                .value(body -> assertThat(jwtTestSupport.roleOf(body.accessToken())).isEqualTo("ACTIVE"));
        }

        @Test
        @DisplayName("이미 복구된 회원이 만료 전 WITHDRAWN 토큰으로 다시 복구하면 409 와 M0004 를 반환한다")
        void returns409_whenAlreadyRecoveredMemberRecoversAgain() {
            SignupResponse signup = fixture.signupActiveMember("naver-recovery-again");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            clock.advanceBy(Duration.ofHours(1));
            SocialLoginResponse withdrawnLogin = fixture.naverLoginMember("naver-recovery-again");
            fixture.recover(withdrawnLogin.accessToken()).expectStatus().isOk();

            fixture.recover(withdrawnLogin.accessToken())
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(MemberErrorCode.ALREADY_RECOVERED.getCode()));
        }

        @Test
        @DisplayName("가입 회원(ACTIVE) 토큰으로 복구하면 403 과 A0007 을 반환한다")
        void returns403_whenActiveTokenRecovers() {
            SignupResponse signup = fixture.signupActiveMember("naver-recovery-active");

            fixture.recover(signup.accessToken())
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 복구하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenRecovers() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-recovery-pending");

            fixture.recover(login.accessToken())
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 호출하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            fixture.client().post().uri("/v1/members/me/recovery")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }
}
