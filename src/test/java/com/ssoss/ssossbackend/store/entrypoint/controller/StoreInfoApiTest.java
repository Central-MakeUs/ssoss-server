package com.ssoss.ssossbackend.store.entrypoint.controller;

import java.util.List;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.store.entrypoint.response.StoreBasicInfoResponse;
import com.ssoss.ssossbackend.store.entrypoint.response.StoreContentInfoResponse;
import com.ssoss.ssossbackend.store.entrypoint.response.StoreOperationInfoResponse;
import com.ssoss.ssossbackend.store.entrypoint.response.StoreInfoResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ssoss.ssossbackend.store.domain.model.StoreInfoStatus.NOT_WRITTEN;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("매장 정보 조회 API")
class StoreInfoApiTest extends IntegrationTest {

    @Nested
    @DisplayName("GET /v1/stores/me")
    class Info {

        @Test
        @DisplayName("가입 직후 조회하면 세 그룹 값이 모두 비고 작성 상태가 셋 다 미작성으로 반환된다")
        void returnsEmptyGroups_whenActiveMemberQueriesRightAfterSignup() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-profile-empty");

            fixture.storeInfo(signup.accessToken())
                .expectStatus().isOk()
                .expectBody(StoreInfoResponse.class)
                .value(body -> {
                    assertThat(body.basic())
                        .isEqualTo(new StoreBasicInfoResponse(null, null, null, null, NOT_WRITTEN.name()));
                    assertThat(body.operation())
                        .isEqualTo(new StoreOperationInfoResponse(List.of(), null, null, List.of(),
                            false, false, false, NOT_WRITTEN.name()));
                    assertThat(body.content())
                        .isEqualTo(new StoreContentInfoResponse(null, List.of(), null, null, NOT_WRITTEN.name()));
                });
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 조회하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenQueriesInfo() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-store-profile-pending");

            fixture.storeInfo(login.accessToken())
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("탈퇴 대기(WITHDRAWN) 토큰으로 조회하면 403 과 A0007 을 반환한다")
        void returns403_whenWithdrawnTokenQueriesInfo() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-profile-withdrawn");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            SocialLoginResponse withdrawnLogin = fixture.naverLoginMember("naver-store-profile-withdrawn");

            fixture.storeInfo(withdrawnLogin.accessToken())
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 조회하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            client().get().uri("/v1/stores/me")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }

        @Test
        @DisplayName("유효하지 않은 액세스 토큰으로 조회하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenInvalid() {
            fixture.storeInfo("not-a-jwt")
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }
}
