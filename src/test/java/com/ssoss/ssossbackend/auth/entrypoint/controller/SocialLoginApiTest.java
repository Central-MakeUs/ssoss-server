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
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;

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

    @Nested
    @DisplayName("POST /v1/social-logins/{provider}")
    class Login {

        @Test
        @DisplayName("신규 사용자가 네이버 로그인하면 회원이 자동 생성되고 access/refresh 토큰이 발급된다")
        void createsMemberAndIssuesTokens_whenFirstLogin() {
            naverApi.stubProfile("naver-access-token", "naver-id-new");

            fixture.socialLogin(SocialProvider.NAVER, "naver-access-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .value(body -> {
                    assertThat(body.accessToken()).isNotBlank();
                    assertThat(body.refreshToken()).isNotBlank();
                });

            assertThat(memberRepository.findByProviderAndSocialId(NAVER, "naver-id-new")).isPresent();
        }

        @Test
        @DisplayName("동일 네이버 계정으로 재로그인하면 회원이 중복 생성되지 않고 토큰이 발급된다")
        void reusesExistingMember_whenSameAccountLogsInAgain() {
            naverApi.stubProfile("returning-token", "naver-id-returning");

            fixture.socialLogin(SocialProvider.NAVER, "returning-token").expectStatus().isOk();

            fixture.socialLogin(SocialProvider.NAVER, "returning-token")
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .value(body -> {
                    assertThat(body.accessToken()).isNotBlank();
                    assertThat(body.refreshToken()).isNotBlank();
                });

            assertThat(memberRepository.findByProviderAndSocialId(NAVER, "naver-id-returning")).isPresent();
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
        @DisplayName("신규 사용자가 애플 로그인하면 회원이 자동 생성되고 access/refresh 토큰이 발급된다")
        void createsMemberAndIssuesTokens_whenFirstAppleLogin() {
            appleApi.stubJwks();

            fixture.socialLogin(SocialProvider.APPLE, appleApi.issueIdentityToken("apple-sub-new"))
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .value(body -> {
                    assertThat(body.accessToken()).isNotBlank();
                    assertThat(body.refreshToken()).isNotBlank();
                });

            assertThat(memberRepository.findByProviderAndSocialId(APPLE, "apple-sub-new")).isPresent();
        }

        @Test
        @DisplayName("동일 애플 계정으로 재로그인하면 회원이 중복 생성되지 않고 토큰이 발급된다")
        void reusesExistingMember_whenSameAppleAccountLogsInAgain() {
            appleApi.stubJwks();

            fixture.socialLogin(SocialProvider.APPLE, appleApi.issueIdentityToken("apple-sub-returning"))
                .expectStatus().isOk();

            fixture.socialLogin(SocialProvider.APPLE, appleApi.issueIdentityToken("apple-sub-returning"))
                .expectStatus().isOk()
                .expectBody(SocialLoginResponse.class)
                .value(body -> {
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
