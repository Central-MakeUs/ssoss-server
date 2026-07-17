package com.ssoss.ssossbackend.auth.entrypoint.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;
import com.ssoss.ssossbackend.auth.domain.contract.TokenHasher;
import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.domain.model.RefreshToken;
import com.ssoss.ssossbackend.auth.domain.model.RefreshTokenStatus;
import com.ssoss.ssossbackend.auth.domain.model.SocialProvider;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.member.domain.contract.MemberRepository;
import com.ssoss.ssossbackend.member.domain.model.Member;
import com.ssoss.ssossbackend.member.domain.model.MemberStatus;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static com.ssoss.ssossbackend.member.domain.model.SocialProvider.APPLE;
import static com.ssoss.ssossbackend.member.domain.model.SocialProvider.NAVER;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("소셜 로그인 API")
class SocialLoginApiTest extends IntegrationTest {

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
    @DisplayName("POST /v1/social-logins/{provider}")
    class Login {

        @Test
        @DisplayName("네이버 첫 로그인 시 가입 대기 회원이 생성되고 PENDING 상태와 토큰 쌍이 발급된다")
        void createsPendingMemberAndIssuesTokenPair_whenFirstLogin() {
            naverApi.stubProfile("naver-access-token", "naver-id-new");

            fixture.socialLogin(SocialProvider.NAVER, "naver-access-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("PENDING");
                    assertThat(body.accessToken()).isNotBlank();
                    assertThat(body.refreshToken()).isNotBlank();
                    assertThat(jwtTestSupport.roleOf(body.accessToken())).isEqualTo("PENDING");
                });

            Member member = memberRepository.findByProviderAndSocialId(NAVER, "naver-id-new").orElseThrow();
            assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING);
        }

        @Test
        @DisplayName("네이버 첫 로그인 시 프로필의 이메일이 회원에 저장된다")
        void storesProfileEmail_whenFirstNaverLogin() {
            naverApi.stubProfile("naver-access-token", "naver-id-email", "hong@naver.com");

            fixture.socialLogin(SocialProvider.NAVER, "naver-access-token").expectStatus().isOk();

            Member member = memberRepository.findByProviderAndSocialId(NAVER, "naver-id-email").orElseThrow();
            assertThat(member.getEmail()).isEqualTo("hong@naver.com");
        }

        @Test
        @DisplayName("가입 대기 회원이 재로그인하면 회원이 중복 생성되지 않고 같은 회원으로 다시 PENDING 응답을 받는다")
        void reusesPendingMember_whenPendingMemberLogsInAgain() {
            naverApi.stubProfile("returning-token", "naver-id-returning");

            fixture.socialLogin(SocialProvider.NAVER, "returning-token").expectStatus().isOk();

            fixture.socialLogin(SocialProvider.NAVER, "returning-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("PENDING");
                    assertThat(body.accessToken()).isNotBlank();
                    assertThat(body.refreshToken()).isNotBlank();
                });

            assertThat(memberRepository.findAll())
                .filteredOn(member -> member.getSocialId().equals("naver-id-returning"))
                .hasSize(1);
        }

        @Test
        @DisplayName("로그인할 때마다 refresh token 이 별도 ACTIVE 세션으로 저장되어 멀티 디바이스 세션이 공존한다")
        void storesActiveSessionPerLogin() {
            naverApi.stubProfile("rt-token", "naver-id-rt");

            SocialLoginResponse first = fixture.socialLogin(SocialProvider.NAVER, "rt-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();

            SocialLoginResponse second = fixture.socialLogin(SocialProvider.NAVER, "rt-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .returnResult()
                .getResponseBody();

            Long memberId = memberRepository.findByProviderAndSocialId(NAVER, "naver-id-rt")
                .orElseThrow()
                .getId();
            List<RefreshToken> sessions = refreshTokenRepository.findAllByMemberId(memberId);
            assertThat(sessions).hasSize(2);
            assertThat(sessions).allSatisfy(session -> {
                assertThat(session.getStatus()).isEqualTo(RefreshTokenStatus.ACTIVE);
                assertThat(session.getSessionId()).isNotBlank();
                assertThat(session.getExpiresAt()).isAfter(Instant.now());
            });
            assertThat(sessions).extracting(RefreshToken::getSessionId).doesNotHaveDuplicates();
            assertThat(sessions).extracting(RefreshToken::getTokenHash)
                .containsExactlyInAnyOrder(
                    tokenHasher.hash(first.refreshToken()),
                    tokenHasher.hash(second.refreshToken())
                );
        }

        @Test
        @DisplayName("네이버가 토큰을 거부하면 401 과 A0001 을 반환한다")
        void returns401AndAuthErrorCode_whenNaverRejectsToken() {
            naverApi.stubProfile("valid-token", "naver-id-valid");

            fixture.socialLogin(SocialProvider.NAVER, "invalid-token")
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_SOCIAL_TOKEN.getCode()));
        }

        @Test
        @DisplayName("네이버 응답에 프로필이 없으면 401 과 A0001 을 반환한다")
        void returns401_whenNaverResponseIsMalformed() {
            naverApi.stubMalformedProfile("weird-token");

            fixture.socialLogin(SocialProvider.NAVER, "weird-token")
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_SOCIAL_TOKEN.getCode()));
        }

        @Test
        @DisplayName("애플 첫 로그인 시 가입 대기 회원이 생성되고 PENDING 상태와 토큰 쌍이 발급된다")
        void createsPendingMemberAndIssuesTokenPair_whenFirstAppleLogin() {
            appleApi.stubJwks();

            fixture.socialLogin(SocialProvider.APPLE, appleApi.issueIdentityToken("apple-sub-new"))
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("PENDING");
                    assertThat(body.accessToken()).isNotBlank();
                    assertThat(body.refreshToken()).isNotBlank();
                });

            Member member = memberRepository.findByProviderAndSocialId(APPLE, "apple-sub-new").orElseThrow();
            assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING);
        }

        @Test
        @DisplayName("애플 첫 로그인 시 identity token 의 email 클레임이 회원에 저장된다")
        void storesEmailClaim_whenFirstAppleLogin() {
            appleApi.stubJwks();

            fixture.socialLogin(SocialProvider.APPLE, appleApi.issueIdentityToken("apple-sub-email", "kim@icloud.com"))
                .expectStatus().isOk();

            Member member = memberRepository.findByProviderAndSocialId(APPLE, "apple-sub-email").orElseThrow();
            assertThat(member.getEmail()).isEqualTo("kim@icloud.com");
        }

        @Test
        @DisplayName("애플 비공개 릴레이 이메일도 그대로 회원에 저장된다")
        void storesPrivateRelayEmail_whenAppleEmailIsRelayAddress() {
            appleApi.stubJwks();

            fixture.socialLogin(SocialProvider.APPLE,
                    appleApi.issueIdentityToken("apple-sub-relay", "abc123def@privaterelay.appleid.com"))
                .expectStatus().isOk();

            Member member = memberRepository.findByProviderAndSocialId(APPLE, "apple-sub-relay").orElseThrow();
            assertThat(member.getEmail()).isEqualTo("abc123def@privaterelay.appleid.com");
        }

        @Test
        @DisplayName("동일 애플 계정으로 재로그인하면 회원이 중복 생성되지 않고 다시 PENDING 응답을 받는다")
        void reusesExistingMember_whenSameAppleAccountLogsInAgain() {
            appleApi.stubJwks();

            fixture.socialLogin(SocialProvider.APPLE, appleApi.issueIdentityToken("apple-sub-returning"))
                .expectStatus().isOk();

            fixture.socialLogin(SocialProvider.APPLE, appleApi.issueIdentityToken("apple-sub-returning"))
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("PENDING");
                    assertThat(body.accessToken()).isNotBlank();
                    assertThat(body.refreshToken()).isNotBlank();
                });

            assertThat(memberRepository.findAll())
                .filteredOn(member -> member.getProvider() == APPLE)
                .hasSize(1);
        }

        @Test
        @DisplayName("애플 공개키로 서명을 검증할 수 없는 identity token 이면 401 과 A0001 을 반환한다")
        void returns401AndAuthErrorCode_whenAppleIdentityTokenSignatureIsInvalid() {
            appleApi.stubJwks();

            fixture.socialLogin(SocialProvider.APPLE, appleApi.issueIdentityTokenSignedByUnknownKey("apple-sub-forged"))
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_SOCIAL_TOKEN.getCode()));
        }

        @Test
        @DisplayName("다른 앱(client-id)용으로 발급된 애플 identity token 이면 401 과 A0001 을 반환한다")
        void returns401AndAuthErrorCode_whenAppleIdentityTokenAudienceMismatches() {
            appleApi.stubJwks();

            fixture.socialLogin(SocialProvider.APPLE, appleApi.issueIdentityTokenForOtherClient("apple-sub-other-app"))
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_SOCIAL_TOKEN.getCode()));
        }

        @Test
        @DisplayName("만료된 애플 identity token 이면 401 과 A0001 을 반환한다")
        void returns401AndAuthErrorCode_whenAppleIdentityTokenIsExpired() {
            appleApi.stubJwks();

            fixture.socialLogin(SocialProvider.APPLE, appleApi.issueExpiredIdentityToken("apple-sub-expired"))
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_SOCIAL_TOKEN.getCode()));
        }

        @Test
        @DisplayName("네이버 프로필에 이메일이 없으면 400 과 A0008 가입 실패를 반환한다")
        void returns400AndSignupError_whenNaverProfileHasNoEmail() {
            naverApi.stubProfileWithoutEmail("no-email-token", "naver-id-no-email");

            fixture.socialLogin(SocialProvider.NAVER, "no-email-token")
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.SIGNUP_EMAIL_NOT_PROVIDED.getCode()));
        }

        @Test
        @DisplayName("애플 identity token 에 email 클레임이 없으면 400 과 A0008 가입 실패를 반환한다")
        void returns400AndSignupError_whenAppleIdentityTokenHasNoEmailClaim() {
            appleApi.stubJwks();

            fixture.socialLogin(SocialProvider.APPLE, appleApi.issueIdentityTokenWithoutEmail("apple-sub-no-email"))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.SIGNUP_EMAIL_NOT_PROVIDED.getCode()));
        }

        @Test
        @DisplayName("이미 회원이 된 뒤에는 소셜이 이메일을 제공하지 않아도 재로그인된다")
        void logsInExistingMember_whenSocialNoLongerProvidesEmail() {
            naverApi.stubProfile("keep-token", "naver-id-keep", "keep@naver.com");
            fixture.socialLogin(SocialProvider.NAVER, "keep-token").expectStatus().isOk();

            naverApi.stubProfileWithoutEmail("keep-token", "naver-id-keep");

            fixture.socialLogin(SocialProvider.NAVER, "keep-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .value(body -> assertThat(body.status()).isEqualTo("PENDING"));

            Member member = memberRepository.findByProviderAndSocialId(NAVER, "naver-id-keep").orElseThrow();
            assertThat(member.getEmail()).isEqualTo("keep@naver.com");
        }

        @Test
        @DisplayName("지원하지 않는 프로바이더 경로로 요청하면 404 와 A0002 를 반환한다")
        void returns404AndAuthErrorCode_whenProviderIsUnsupported() {
            fixture.client().post().uri("/v1/social-logins/kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("accessToken", "any-token"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.UNSUPPORTED_SOCIAL_PROVIDER.getCode()));
        }

        @Test
        @DisplayName("소셜 액세스 토큰이 비어 있으면 400 과 C0001 을 반환한다")
        void returns400_whenAccessTokenIsBlank() {
            fixture.socialLogin(SocialProvider.NAVER, "")
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("네이버가 5xx 를 반환하면 503 과 A0003 을 반환한다")
        void returns503_whenNaverReturnsServerError() {
            naverApi.stubServerError();

            fixture.socialLogin(SocialProvider.NAVER, "any-token")
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.SOCIAL_PROVIDER_UNAVAILABLE.getCode()));
        }

        @Test
        @DisplayName("애플 공개키 응답이 200 이지만 본문이 비정상이면 503 과 A0003 을 반환한다")
        void returns503_whenAppleJwksResponseIsMalformed() {
            appleApi.stubMalformedJwks();

            fixture.socialLogin(SocialProvider.APPLE, appleApi.issueIdentityToken("apple-sub-any"))
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.SOCIAL_PROVIDER_UNAVAILABLE.getCode()));
        }

        @Test
        @DisplayName("애플 공개키 조회가 5xx 로 실패하면 503 과 A0003 을 반환한다")
        void returns503_whenAppleJwksLookupFails() {
            appleApi.stubServerError();

            fixture.socialLogin(SocialProvider.APPLE, appleApi.issueIdentityToken("apple-sub-any"))
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.SOCIAL_PROVIDER_UNAVAILABLE.getCode()));
        }
    }
}
