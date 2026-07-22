package com.ssoss.ssossbackend.credit.entrypoint.controller;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.credit.entrypoint.response.CreditBalanceResponse;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("잔여 크레딧 조회 API")
class CreditBalanceApiTest extends IntegrationTest {

    @Nested
    @DisplayName("GET /v1/credits/me")
    class Balance {

        @Test
        @DisplayName("가입 회원이 조회하면 원장이 비어 있어 잔여 50 과 한도 50 을 반환한다")
        void returnsFullRemainingAndLimit_whenActiveMemberHasEmptyLedger() {
            SignupResponse signup = fixture.signupActiveMember("naver-credit-balance");

            fixture.creditBalance(signup.accessToken())
                .expectStatus().isOk()
                .expectBody(CreditBalanceResponse.class)
                .value(body -> {
                    assertThat(body.remaining()).isEqualTo(50);
                    assertThat(body.limit()).isEqualTo(50);
                });
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 조회하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenQueriesBalance() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-credit-pending");

            fixture.creditBalance(login.accessToken())
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("탈퇴 대기(WITHDRAWN) 토큰으로 조회하면 403 과 A0007 을 반환한다")
        void returns403_whenWithdrawnTokenQueriesBalance() {
            SignupResponse signup = fixture.signupActiveMember("naver-credit-withdrawn");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            SocialLoginResponse withdrawnLogin = fixture.naverLoginMember("naver-credit-withdrawn");

            fixture.creditBalance(withdrawnLogin.accessToken())
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 조회하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            fixture.client().get().uri("/v1/credits/me")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }
}
