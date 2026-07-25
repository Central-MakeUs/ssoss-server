package com.ssoss.ssossbackend.auth.entrypoint.controller;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;
import com.ssoss.ssossbackend.auth.domain.contract.SocialLoginRepository;
import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
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

@DisplayName("탈퇴 API")
class WithdrawalApiTest extends IntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private MemberTermRepository memberTermRepository;

    @Autowired
    private MemberWithdrawalHistoryRepository memberWithdrawalHistoryRepository;

    @Autowired
    private SocialLoginRepository socialLoginRepository;

    @BeforeEach
    void resetDatabase() {
        memberWithdrawalHistoryRepository.deleteAll();
        memberTermRepository.deleteAll();
        socialLoginRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("DELETE /v1/members/me")
    class Withdraw {

        @Test
        @DisplayName("네이버 회원이 탈퇴하면 저장된 리프레시 토큰으로 네이버 연결 해제가 1회 호출된다")
        void revokesNaverConnectionOnce_whenNaverMemberWithdraws() {
            SignupResponse signup = fixture.signupActiveMember("naver-unlink");

            fixture.withdraw(signup.accessToken())
                .expectStatus().isNoContent();

            assertThat(naverApi.revokeRequestBodies())
                .singleElement()
                .satisfies(body -> assertThat(body)
                    .contains("token=naver-refresh-token")
                    .contains("token_type_hint=refresh_token")
                    .contains("client_id=test-naver-client-id")
                    .contains("client_secret=test-naver-client-secret"));
        }

        @Test
        @DisplayName("저장된 소셜 로그인이 없는 회원이 탈퇴하면 연결 해제를 호출하지 않고 204 로 끝난다")
        void skipsUnlink_whenNoStoredSocialLogin() {
            appleApi.stubJwks();
            appleApi.stubTokenExchangeServerError();
            SocialLoginResponse login = fixture.socialLogin(com.ssoss.ssossbackend.auth.domain.model.SocialProvider.APPLE,
                    appleApi.issueIdentityToken("apple-sub-no-token"), "apple-auth-code")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();
            SignupResponse signup = fixture.signup(login.accessToken())
                .expectStatus().isOk()
                .expectBody(SignupResponse.class)
                .returnResult()
                .getResponseBody();

            fixture.withdraw(signup.accessToken())
                .expectStatus().isNoContent();

            assertThat(appleApi.revokeRequestBodies()).isEmpty();
        }

        @Test
        @DisplayName("애플 회원이 탈퇴하면 저장된 리프레시 토큰으로 애플 연결 해제가 1회 호출된다")
        void revokesAppleConnectionOnce_whenAppleMemberWithdraws() {
            appleApi.stubJwks();
            appleApi.stubTokenExchange("apple-stored-refresh-token");
            SocialLoginResponse login = fixture.socialLogin(com.ssoss.ssossbackend.auth.domain.model.SocialProvider.APPLE,
                    appleApi.issueIdentityToken("apple-sub-unlink"), "apple-auth-code")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();
            SignupResponse signup = fixture.signup(login.accessToken())
                .expectStatus().isOk()
                .expectBody(SignupResponse.class)
                .returnResult()
                .getResponseBody();

            fixture.withdraw(signup.accessToken())
                .expectStatus().isNoContent();

            assertThat(appleApi.revokeRequestBodies())
                .singleElement()
                .satisfies(body -> assertThat(body)
                    .contains("token=apple-stored-refresh-token")
                    .contains("token_type_hint=refresh_token")
                    .contains("client_id=test-apple-client-id")
                    .contains("client_secret="));
        }

        @Test
        @DisplayName("네이버 연결 해제가 실패해도 탈퇴는 204 로 끝나고 탈퇴 대기로 남는다")
        void completesWithdrawal_whenUnlinkFails() {
            SignupResponse signup = fixture.signupActiveMember("naver-unlink-fail");
            naverApi.stubRevokeServerError();

            fixture.withdraw(signup.accessToken())
                .expectStatus().isNoContent();

            Member member = memberRepository.findByProviderAndSocialId(NAVER, "naver-unlink-fail").orElseThrow();
            assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        }

        @Test
        @DisplayName("탈퇴해도 소셜 로그인 행은 유예 기간 동안 남는다")
        void keepsSocialLogin_whenMemberWithdraws() {
            SignupResponse signup = fixture.signupActiveMember("naver-unlink-keep");

            fixture.withdraw(signup.accessToken())
                .expectStatus().isNoContent();

            Member member = memberRepository.findByProviderAndSocialId(NAVER, "naver-unlink-keep").orElseThrow();
            assertThat(socialLoginRepository.findByMemberId(member.getId()))
                .get()
                .satisfies(socialLogin -> {
                    assertThat(socialLogin.getSocialId()).isEqualTo("naver-unlink-keep");
                    assertThat(socialLogin.getProvider().name()).isEqualTo("NAVER");
                });
        }

        @Test
        @DisplayName("탈퇴하면 204 를 반환하고 탈퇴 대기로 전환되며 탈퇴 시각이 기록된다")
        void marksWithdrawnAndRecordsWithdrawnAt_whenActiveMemberWithdraws() {
            SignupResponse signup = fixture.signupActiveMember("naver-withdraw");

            fixture.withdraw(signup.accessToken())
                .expectStatus().isNoContent();

            Member member = memberRepository.findByProviderAndSocialId(NAVER, "naver-withdraw").orElseThrow();
            assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
            assertThat(member.getLastWithdrawnAt()).isCloseTo(clock.instant(), within(1, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("탈퇴하면 재가입 제한 판정을 위한 탈퇴 이력이 적재된다")
        void recordsWithdrawalHistory_whenMemberWithdraws() {
            SignupResponse signup = fixture.signupActiveMember("naver-withdraw-history");

            fixture.withdraw(signup.accessToken())
                .expectStatus().isNoContent();

            assertThat(memberWithdrawalHistoryRepository.findAll())
                .singleElement()
                .satisfies(history -> {
                    assertThat(history.getProvider()).isEqualTo(NAVER);
                    assertThat(history.getSocialId()).isEqualTo("naver-withdraw-history");
                    assertThat(history.getWithdrawnAt()).isCloseTo(clock.instant(), within(1, ChronoUnit.SECONDS));
                });
        }

        @Test
        @DisplayName("탈퇴 후 모든 기기의 refresh token 으로 재발급하면 401 과 A0004 를 반환한다")
        void blocksTokenRefreshOnAllSessions_whenMemberWithdraws() {
            SignupResponse deviceA = fixture.signupActiveMember("naver-withdraw-all");
            SocialLoginResponse deviceB = fixture.naverLoginMember("naver-withdraw-all");

            fixture.withdraw(deviceA.accessToken())
                .expectStatus().isNoContent();

            fixture.refreshTokens(deviceA.refreshToken())
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN.getCode()));
            fixture.refreshTokens(deviceB.refreshToken())
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN.getCode()));
        }

        @Test
        @DisplayName("이미 탈퇴한 회원이 만료 전 ACTIVE 토큰으로 다시 탈퇴하면 409 와 M0003 을 반환한다")
        void returns409_whenAlreadyWithdrawnMemberWithdrawsAgain() {
            SignupResponse signup = fixture.signupActiveMember("naver-withdraw-again");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();

            fixture.withdraw(signup.accessToken())
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(MemberErrorCode.ALREADY_WITHDRAWN.getCode()));

            assertThat(memberWithdrawalHistoryRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 탈퇴하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenWithdraws() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-withdraw-pending");

            fixture.withdraw(login.accessToken())
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));

            assertThat(memberRepository.findByProviderAndSocialId(NAVER, "naver-withdraw-pending")
                .orElseThrow()
                .getStatus())
                .isEqualTo(MemberStatus.PENDING);
        }

        @Test
        @DisplayName("액세스 토큰 없이 호출하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            fixture.client().delete().uri("/v1/members/me")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }

    @Nested
    @DisplayName("탈퇴 대기 회원의 로그인")
    class WithdrawnMemberLogin {

        @Test
        @DisplayName("탈퇴 대기 회원이 로그인하면 WITHDRAWN 상태와 복구 전용 role 토큰 쌍이 응답된다")
        void respondsWithdrawnStatusAndRecoveryRoleTokenPair_whenWithdrawnMemberLogsIn() {
            SignupResponse signup = fixture.signupActiveMember("naver-withdrawn-login");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();

            fixture.naverLogin("naver-withdrawn-login")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("WITHDRAWN");
                    assertThat(body.accessToken()).isNotBlank();
                    assertThat(jwtTestSupport.roleOf(body.accessToken())).isEqualTo("WITHDRAWN");
                    assertThat(body.refreshToken()).isNotBlank();
                });
        }

        @Test
        @DisplayName("탈퇴 후 새로 로그인한 세션의 refresh token 으로 재발급하면 WITHDRAWN 토큰이 발급된다")
        void refreshesWithdrawnTokens_withRefreshTokenIssuedAfterWithdrawal() {
            SignupResponse signup = fixture.signupActiveMember("naver-withdrawn-refresh");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            clock.advanceBy(Duration.ofHours(1));
            SocialLoginResponse withdrawnLogin = fixture.naverLoginMember("naver-withdrawn-refresh");

            fixture.refreshTokens(withdrawnLogin.refreshToken())
                .expectStatus().isOk()
                .expectBody(TokenRefreshResponse.class)
                .value(body -> {
                    assertThat(body.accessToken()).isNotBlank();
                    assertThat(jwtTestSupport.roleOf(body.accessToken())).isEqualTo("WITHDRAWN");
                });
        }

        @Test
        @DisplayName("탈퇴 대기 로그인의 WITHDRAWN 토큰으로 보호 API 를 호출하면 403 과 A0007 을 반환한다")
        void returns403_whenWithdrawnTokenCallsProtectedApi() {
            SignupResponse signup = fixture.signupActiveMember("naver-withdrawn-protected");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            SocialLoginResponse withdrawnLogin = fixture.naverLoginMember("naver-withdrawn-protected");

            fixture.withdraw(withdrawnLogin.accessToken())
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }
    }
}
