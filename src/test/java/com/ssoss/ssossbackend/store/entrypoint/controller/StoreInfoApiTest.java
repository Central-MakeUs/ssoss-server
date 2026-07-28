package com.ssoss.ssossbackend.store.entrypoint.controller;

import java.util.List;
import java.util.Map;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.store.entrypoint.response.StoreBasicInfoResponse;
import com.ssoss.ssossbackend.store.entrypoint.response.StoreContentInfoResponse;
import com.ssoss.ssossbackend.store.entrypoint.response.StoreOperationInfoResponse;
import com.ssoss.ssossbackend.store.entrypoint.response.StoreInfoResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static com.ssoss.ssossbackend.store.domain.model.StoreInfoStatus.COMPLETED;
import static com.ssoss.ssossbackend.store.domain.model.StoreInfoStatus.IN_PROGRESS;
import static com.ssoss.ssossbackend.store.domain.model.StoreInfoStatus.NOT_WRITTEN;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("매장 정보 API")
class StoreInfoApiTest extends IntegrationTest {

    @Nested
    @DisplayName("GET /v1/stores/me")
    class Info {

        @Test
        @DisplayName("가입 직후 조회하면 세 그룹 값이 모두 비고 작성 상태가 셋 다 미작성으로 반환된다")
        void returnsEmptyGroups_whenActiveMemberQueriesRightAfterSignup() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-profile-empty");

            fixture.getStoreInfo(signup.accessToken())
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

            fixture.getStoreInfo(login.accessToken())
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

            fixture.getStoreInfo(withdrawnLogin.accessToken())
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
            fixture.getStoreInfo("not-a-jwt")
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }

    @Nested
    @DisplayName("PUT /v1/stores/me/basic")
    class SaveBasicInfo {

        @Test
        @DisplayName("네 필드를 다 채워 저장하면 조회에 그대로 담기고 기본 정보가 작성 완료가 된다")
        void savesAllFields_whenEveryFieldGiven() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-basic-full");

            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody("보니스커피", "CAFE", "서울 중구 을지로 100", "매일 아침 굽는 크루아상이 있는 을지로 카페"))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).basic())
                .isEqualTo(new StoreBasicInfoResponse("보니스커피", "CAFE", "서울 중구 을지로 100",
                    "매일 아침 굽는 크루아상이 있는 을지로 카페", COMPLETED.name()));
        }

        @Test
        @DisplayName("한 줄 소개 없이 저장하면 기본 정보가 작성 중이 된다")
        void marksInProgress_whenIntroductionOmitted() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-basic-no-introduction");

            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody("보니스커피", "CAFE", "서울 중구 을지로 100", null))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).basic())
                .isEqualTo(new StoreBasicInfoResponse("보니스커피", "CAFE", "서울 중구 을지로 100", null,
                    IN_PROGRESS.name()));
        }

        @Test
        @DisplayName("한 줄 소개를 빈 문자열로 저장하면 비운 것으로 봐 작성 중이 된다")
        void treatsEmptyIntroductionAsAbsent() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-basic-empty-introduction");

            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody("보니스커피", "CAFE", "서울 중구 을지로 100", ""))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).basic())
                .isEqualTo(new StoreBasicInfoResponse("보니스커피", "CAFE", "서울 중구 을지로 100", null,
                    IN_PROGRESS.name()));
        }

        @Test
        @DisplayName("한 줄 소개를 공백만 담아 저장하면 비운 것으로 봐 작성 중이 된다")
        void treatsBlankIntroductionAsAbsent() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-basic-blank-introduction");

            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody("보니스커피", "CAFE", "서울 중구 을지로 100", "   "))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).basic())
                .isEqualTo(new StoreBasicInfoResponse("보니스커피", "CAFE", "서울 중구 을지로 100", null,
                    IN_PROGRESS.name()));
        }

        @Test
        @DisplayName("한 줄 소개를 빈 문자열로 다시 저장하면 이전에 저장한 소개가 비워진다")
        void clearsIntroduction_whenSavedAgainWithEmptyValue() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-basic-clear-introduction");
            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody("보니스커피", "CAFE", "서울 중구 을지로 100", "을지로 크루아상 카페"))
                .expectStatus().isNoContent();

            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody("보니스커피", "CAFE", "서울 중구 을지로 100", ""))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).basic())
                .isEqualTo(new StoreBasicInfoResponse("보니스커피", "CAFE", "서울 중구 을지로 100", null,
                    IN_PROGRESS.name()));
        }

        @Test
        @DisplayName("한 줄 소개를 빼고 다시 저장하면 이전에 저장한 소개가 비워진다")
        void clearsIntroduction_whenSavedAgainWithoutIt() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-basic-replace");
            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody("보니스커피", "CAFE", "서울 중구 을지로 100", "을지로 크루아상 카페"))
                .expectStatus().isNoContent();

            fixture.saveStoreBasicInfo(signup.accessToken(),
                    Map.of("name", "보니스베이커리", "type", "BAKERY", "address", "서울 중구 을지로 200"))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).basic())
                .isEqualTo(new StoreBasicInfoResponse("보니스베이커리", "BAKERY", "서울 중구 을지로 200", null,
                    IN_PROGRESS.name()));
        }

        @Test
        @DisplayName("매장명이 없으면 400 과 C0001 을 반환한다")
        void returns400_whenNameMissing() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-basic-no-name");

            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody(null, "CAFE", "서울 중구 을지로 100", null))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("매장 유형이 없으면 400 과 C0001 을 반환한다")
        void returns400_whenTypeMissing() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-basic-no-type");

            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody("보니스커피", null, "서울 중구 을지로 100", null))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("주소가 없으면 400 과 C0001 을 반환한다")
        void returns400_whenAddressMissing() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-basic-no-address");

            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody("보니스커피", "CAFE", null, null))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("목록에 없는 매장 유형이면 400 과 C0001 을 반환한다")
        void returns400_whenTypeNotInList() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-basic-unknown-type");

            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody("보니스커피", "ETC", "서울 중구 을지로 100", null))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("매장명이 50자를 넘으면 400 과 C0001 을 반환한다")
        void returns400_whenNameTooLong() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-basic-long-name");

            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody("가".repeat(51), "CAFE", "서울 중구 을지로 100", null))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("주소가 200자를 넘으면 400 과 C0001 을 반환한다")
        void returns400_whenAddressTooLong() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-basic-long-address");

            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody("보니스커피", "CAFE", "가".repeat(201), null))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("한 줄 소개가 100자를 넘으면 400 과 C0001 을 반환한다")
        void returns400_whenIntroductionTooLong() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-basic-long-introduction");

            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody("보니스커피", "CAFE", "서울 중구 을지로 100", "가".repeat(101)))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("상한을 넘겨 400 이 나면 이전에 저장한 값이 그대로 남는다")
        void keepsSavedValues_whenValidationFails() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-basic-rejected");
            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody("보니스커피", "CAFE", "서울 중구 을지로 100", "을지로 크루아상 카페"))
                .expectStatus().isNoContent();

            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody("가".repeat(51), "CAFE", "서울 중구 을지로 100", null))
                .expectStatus().isBadRequest();

            assertThat(fixture.storeInfo(signup.accessToken()).basic())
                .isEqualTo(new StoreBasicInfoResponse("보니스커피", "CAFE", "서울 중구 을지로 100", "을지로 크루아상 카페",
                    COMPLETED.name()));
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 저장하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenSaves() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-store-basic-pending");

            fixture.saveStoreBasicInfo(login.accessToken(),
                    fixture.storeBasicInfoBody("보니스커피", "CAFE", "서울 중구 을지로 100", null))
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("탈퇴 대기(WITHDRAWN) 토큰으로 저장하면 403 과 A0007 을 반환한다")
        void returns403_whenWithdrawnTokenSaves() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-basic-withdrawn");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            SocialLoginResponse withdrawnLogin = fixture.naverLoginMember("naver-store-basic-withdrawn");

            fixture.saveStoreBasicInfo(withdrawnLogin.accessToken(),
                    fixture.storeBasicInfoBody("보니스커피", "CAFE", "서울 중구 을지로 100", null))
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 저장하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            client().put().uri("/v1/stores/me/basic")
                .contentType(MediaType.APPLICATION_JSON)
                .body(fixture.storeBasicInfoBody("보니스커피", "CAFE", "서울 중구 을지로 100", null))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }
}
