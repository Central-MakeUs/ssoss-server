package com.ssoss.ssossbackend.auth.entrypoint.controller;

import java.util.Map;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;
import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.TokenRefreshResponse;
import com.ssoss.ssossbackend.member.domain.contract.MemberRepository;
import com.ssoss.ssossbackend.member.domain.contract.MemberTermRepository;
import com.ssoss.ssossbackend.member.domain.model.MemberErrorCode;
import com.ssoss.ssossbackend.member.domain.model.MemberStatus;
import com.ssoss.ssossbackend.member.domain.model.TermErrorCode;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static com.ssoss.ssossbackend.member.domain.model.SocialProvider.NAVER;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("회원가입 API")
class SignupApiTest extends IntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private MemberTermRepository memberTermRepository;

    @BeforeEach
    void resetDatabase() {
        memberTermRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /v1/signup")
    class Signup {

        @Test
        @DisplayName("필수 약관과 마케팅 수신에 모두 동의하면 가입 회원으로 전환되고 ACTIVE 토큰 쌍이 발급된다")
        void activatesMemberAndIssuesActiveTokenPair_whenAllTermsAgreed() {
            SocialLoginResponse login = fixture.naverLogin("naver-signup-all")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();

            fixture.signup(login.accessToken())
                .expectStatus().isOk()
                .expectBody(SignupResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("ACTIVE");
                    assertThat(body.accessToken()).isNotBlank();
                    assertThat(body.refreshToken()).isNotBlank();
                    assertThat(jwtTestSupport.roleOf(body.accessToken())).isEqualTo("ACTIVE");
                });

            Long memberId = memberRepository.findByProviderAndSocialId(NAVER, "naver-signup-all")
                .orElseThrow()
                .getId();
            assertThat(memberRepository.findById(memberId).orElseThrow().getStatus()).isEqualTo(MemberStatus.ACTIVE);
            assertThat(memberTermRepository.findAll())
                .filteredOn(term -> term.getMemberId().equals(memberId))
                .singleElement()
                .satisfies(term -> {
                    assertThat(term.isServiceTermsAgreed()).isTrue();
                    assertThat(term.isPrivacyPolicyAgreed()).isTrue();
                    assertThat(term.isMarketingAgreed()).isTrue();
                    assertThat(term.getCreatedAt()).isNotNull();
                });
        }

        @Test
        @DisplayName("마케팅 수신에 동의하지 않아도 회원가입이 되고 미동의 사실과 시각이 기록된다")
        void signsUpAndRecordsDisagreement_whenMarketingNotAgreed() {
            SocialLoginResponse login = fixture.naverLogin("naver-signup-no-marketing")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();

            fixture.signup(login.accessToken(), true, true, false)
                .expectStatus().isOk()
                .expectBody(SignupResponse.class)
                .value(body -> assertThat(body.status()).isEqualTo("ACTIVE"));

            Long memberId = memberRepository.findByProviderAndSocialId(NAVER, "naver-signup-no-marketing")
                .orElseThrow()
                .getId();
            assertThat(memberTermRepository.findAll())
                .filteredOn(term -> term.getMemberId().equals(memberId))
                .singleElement()
                .satisfies(term -> {
                    assertThat(term.isServiceTermsAgreed()).isTrue();
                    assertThat(term.isPrivacyPolicyAgreed()).isTrue();
                    assertThat(term.isMarketingAgreed()).isFalse();
                    assertThat(term.getCreatedAt()).isNotNull();
                    assertThat(term.getUpdatedAt()).isNotNull();
                });
        }

        @Test
        @DisplayName("필수 약관 중 하나라도 동의하지 않으면 400 과 T0001 을 반환하고 가입 대기 상태가 유지된다")
        void returns400AndKeepsPending_whenRequiredTermsNotAgreed() {
            SocialLoginResponse login = fixture.naverLogin("naver-signup-required-false")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();

            fixture.signup(login.accessToken(), true, false, true)
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(TermErrorCode.REQUIRED_TERMS_NOT_AGREED.getCode()));

            assertThat(memberRepository.findByProviderAndSocialId(NAVER, "naver-signup-required-false")
                .orElseThrow()
                .getStatus())
                .isEqualTo(MemberStatus.PENDING);
            assertThat(memberTermRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("필수 약관 항목을 제출하지 않으면 400 과 C0001 을 반환하고 가입 대기 상태가 유지된다")
        void returns400AndKeepsPending_whenRequiredTermMissing() {
            SocialLoginResponse login = fixture.naverLogin("naver-signup-required-missing")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();

            fixture.client().post().uri("/v1/signup")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + login.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("privacyPolicyAgreed", true, "marketingAgreed", true))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));

            assertThat(memberRepository.findByProviderAndSocialId(NAVER, "naver-signup-required-missing")
                .orElseThrow()
                .getStatus())
                .isEqualTo(MemberStatus.PENDING);
        }

        @Test
        @DisplayName("마케팅 수신 항목을 제출하지 않으면 400 과 C0001 을 반환한다")
        void returns400_whenMarketingEntryMissing() {
            SocialLoginResponse login = fixture.naverLogin("naver-signup-marketing-missing")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();

            fixture.client().post().uri("/v1/signup")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + login.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("serviceTermsAgreed", true, "privacyPolicyAgreed", true))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 호출하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            fixture.client().post().uri("/v1/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("serviceTermsAgreed", true, "privacyPolicyAgreed", true, "marketingAgreed", true))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }

        @Test
        @DisplayName("가입 회원의 ACTIVE 토큰으로 호출하면 403 과 A0007 을 반환한다")
        void returns403_whenCalledWithActiveToken() {
            SocialLoginResponse login = fixture.naverLogin("naver-signup-active")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();
            SignupResponse signup = fixture.signup(login.accessToken())
                .expectStatus().isOk()
                .expectBody(SignupResponse.class)
                .returnResult()
                .getResponseBody();

            fixture.signup(signup.accessToken())
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("회원가입 후 이전 가입 대기 토큰으로 다시 호출하면 409 와 M0001 을 반환한다")
        void returns409_whenStalePendingTokenReplaysSignup() {
            SocialLoginResponse login = fixture.naverLogin("naver-signup-replay")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();
            fixture.signup(login.accessToken()).expectStatus().isOk();

            fixture.signup(login.accessToken())
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(MemberErrorCode.ALREADY_SIGNED_UP.getCode()));
        }

        @Test
        @DisplayName("회원가입으로 받은 refresh token 으로 재발급하면 ACTIVE 액세스 토큰이 발급된다")
        void refreshesActiveTokens_withRefreshTokenIssuedAtSignup() {
            SocialLoginResponse login = fixture.naverLogin("naver-signup-refresh")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();
            SignupResponse signup = fixture.signup(login.accessToken())
                .expectStatus().isOk()
                .expectBody(SignupResponse.class)
                .returnResult()
                .getResponseBody();

            fixture.refreshTokens(signup.refreshToken())
                .expectStatus().isOk()
                .expectBody(TokenRefreshResponse.class)
                .value(body -> {
                    assertThat(body.accessToken()).isNotBlank();
                    assertThat(body.refreshToken()).isNotBlank();
                    assertThat(jwtTestSupport.roleOf(body.accessToken())).isEqualTo("ACTIVE");
                });
        }

        @Test
        @DisplayName("회원가입 후 재로그인하면 ACTIVE 상태로 응답된다")
        void respondsActiveStatus_whenLoggingInAgainAfterSignup() {
            SocialLoginResponse login = fixture.naverLogin("naver-signup-relogin")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();
            fixture.signup(login.accessToken()).expectStatus().isOk();

            fixture.naverLogin("naver-signup-relogin")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .value(body -> assertThat(body.status()).isEqualTo("ACTIVE"));
        }
    }
}
