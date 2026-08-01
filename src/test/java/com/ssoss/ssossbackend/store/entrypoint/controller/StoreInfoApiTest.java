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
        @DisplayName("한 줄 소개를 보내지 않으면 비운 것으로 본다")
        void treatsOmittedIntroductionAsAbsent() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-basic-no-introduction");

            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody("보니스커피", "CAFE", "서울 중구 을지로 100", null))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).basic())
                .isEqualTo(new StoreBasicInfoResponse("보니스커피", "CAFE", "서울 중구 을지로 100", null,
                    COMPLETED.name()));
        }

        @Test
        @DisplayName("한 줄 소개를 빈 문자열로 저장하면 비운 것으로 본다")
        void treatsEmptyIntroductionAsAbsent() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-basic-empty-introduction");

            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody("보니스커피", "CAFE", "서울 중구 을지로 100", ""))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).basic())
                .isEqualTo(new StoreBasicInfoResponse("보니스커피", "CAFE", "서울 중구 을지로 100", null,
                    COMPLETED.name()));
        }

        @Test
        @DisplayName("한 줄 소개를 공백만 담아 저장하면 비운 것으로 본다")
        void treatsBlankIntroductionAsAbsent() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-basic-blank-introduction");

            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody("보니스커피", "CAFE", "서울 중구 을지로 100", "   "))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).basic())
                .isEqualTo(new StoreBasicInfoResponse("보니스커피", "CAFE", "서울 중구 을지로 100", null,
                    COMPLETED.name()));
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
                    COMPLETED.name()));
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
                    COMPLETED.name()));
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

    @Nested
    @DisplayName("PUT /v1/stores/me/operation")
    class SaveOperationInfo {

        @Test
        @DisplayName("네 가지를 다 채워 저장하면 조회에 그대로 담기고 운영 정보가 작성 완료가 된다")
        void savesAllFields_whenEveryFieldGiven() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-operation-full");

            fixture.saveStoreOperationInfo(signup.accessToken(),
                    fixture.storeOperationInfoBody(List.of("MONDAY", "TUESDAY"), "09:00", "22:00",
                        List.of("크루아상", "바닐라 라떼"), true, false, true))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).operation())
                .isEqualTo(new StoreOperationInfoResponse(List.of("MONDAY", "TUESDAY"), "09:00", "22:00",
                    List.of("크루아상", "바닐라 라떼"), true, false, true, COMPLETED.name()));
        }

        @Test
        @DisplayName("온보딩처럼 대표 메뉴 없이 저장하면 대표 메뉴가 빈 채로 작성 완료가 된다")
        void savesWithoutSignatureMenus_whenOnboardingOmitsThem() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-operation-no-menus");

            fixture.saveStoreOperationInfo(signup.accessToken(),
                    fixture.storeOperationInfoBody(List.of("MONDAY"), "09:00", "22:00", null, true, false, false))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).operation())
                .isEqualTo(new StoreOperationInfoResponse(List.of("MONDAY"), "09:00", "22:00", List.of(),
                    true, false, false, COMPLETED.name()));
        }

        @Test
        @DisplayName("대표 메뉴를 빼고 다시 저장하면 이전에 저장한 대표 메뉴가 비워진다")
        void clearsSignatureMenus_whenSavedAgainWithoutThem() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-operation-replace");
            fixture.saveStoreOperationInfo(signup.accessToken(),
                    fixture.storeOperationInfoBody(List.of("MONDAY"), "09:00", "22:00", List.of("크루아상"),
                        true, true, true))
                .expectStatus().isNoContent();

            fixture.saveStoreOperationInfo(signup.accessToken(),
                    Map.of("businessDays", List.of("TUESDAY"), "openTime", "10:00", "closeTime", "20:00"))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).operation())
                .isEqualTo(new StoreOperationInfoResponse(List.of("TUESDAY"), "10:00", "20:00", List.of(),
                    false, false, false, COMPLETED.name()));
        }

        @Test
        @DisplayName("종료 시각이 시작 시각보다 일러도 자정을 넘긴 것으로 봐 그대로 저장된다")
        void savesOvernightHours_whenCloseTimeIsEarlierThanOpenTime() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-operation-overnight");

            fixture.saveStoreOperationInfo(signup.accessToken(),
                    fixture.storeOperationInfoBody(List.of("FRIDAY"), "18:00", "02:00", null, false, false, false))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).operation())
                .isEqualTo(new StoreOperationInfoResponse(List.of("FRIDAY"), "18:00", "02:00", List.of(),
                    false, false, false, COMPLETED.name()));
        }

        @Test
        @DisplayName("같은 요일을 여러 번 보내면 한 번만 저장되고 월요일부터의 순서로 정리된다")
        void normalizesBusinessDays_whenDuplicatedOrUnordered() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-operation-duplicated-days");

            fixture.saveStoreOperationInfo(signup.accessToken(),
                    fixture.storeOperationInfoBody(List.of("WEDNESDAY", "MONDAY", "WEDNESDAY"), null, null, null,
                        false, false, false))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).operation().businessDays())
                .containsExactly("MONDAY", "WEDNESDAY");
        }

        @Test
        @DisplayName("편의 시설을 안 보내면 셋 다 불가로 저장된다")
        void savesAmenitiesAsUnavailable_whenOmitted() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-operation-no-amenities");

            fixture.saveStoreOperationInfo(signup.accessToken(),
                    Map.of("businessDays", List.of("MONDAY")))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).operation())
                .isEqualTo(new StoreOperationInfoResponse(List.of("MONDAY"), null, null, List.of(),
                    false, false, false, COMPLETED.name()));
        }

        @Test
        @DisplayName("편의 시설을 null 로 보내도 불가로 저장된다")
        void savesAmenitiesAsUnavailable_whenSentAsNull() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-operation-null-amenities");

            fixture.saveStoreOperationInfo(signup.accessToken(),
                    fixture.storeOperationInfoBody(List.of("MONDAY"), null, null, null, null, null, null))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).operation())
                .isEqualTo(new StoreOperationInfoResponse(List.of("MONDAY"), null, null, List.of(),
                    false, false, false, COMPLETED.name()));
        }

        @Test
        @DisplayName("네 가지를 다 비워 저장하면 운영 정보가 미작성이 된다")
        void marksNotWritten_whenNothingGiven() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-operation-empty");

            fixture.saveStoreOperationInfo(signup.accessToken(),
                    fixture.storeOperationInfoBody(null, null, null, null, false, false, false))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).operation())
                .isEqualTo(new StoreOperationInfoResponse(List.of(), null, null, List.of(),
                    false, false, false, NOT_WRITTEN.name()));
        }

        @Test
        @DisplayName("영업 시각이 HH:mm 형식이 아니면 400 과 C0001 을 반환한다")
        void returns400_whenTimeFormatInvalid() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-operation-bad-time");

            fixture.saveStoreOperationInfo(signup.accessToken(),
                    fixture.storeOperationInfoBody(List.of("MONDAY"), "9시", "22:00", null, false, false, false))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("영업 시각이 24시간 범위를 넘으면 400 과 C0001 을 반환한다")
        void returns400_whenTimeOutOfRange() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-operation-out-of-range-time");

            fixture.saveStoreOperationInfo(signup.accessToken(),
                    fixture.storeOperationInfoBody(List.of("MONDAY"), "09:00", "24:00", null, false, false, false))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("영업 시작 시각만 보내면 400 과 C0001 을 반환한다")
        void returns400_whenOnlyOpenTimeGiven() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-operation-only-open-time");

            fixture.saveStoreOperationInfo(signup.accessToken(),
                    fixture.storeOperationInfoBody(List.of("MONDAY"), "09:00", null, null, false, false, false))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("영업 종료 시각만 보내면 400 과 C0001 을 반환한다")
        void returns400_whenOnlyCloseTimeGiven() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-operation-only-close-time");

            fixture.saveStoreOperationInfo(signup.accessToken(),
                    fixture.storeOperationInfoBody(List.of("MONDAY"), null, "22:00", null, false, false, false))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("목록에 없는 영업 요일이면 400 과 C0001 을 반환한다")
        void returns400_whenBusinessDayNotInList() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-operation-unknown-day");

            fixture.saveStoreOperationInfo(signup.accessToken(),
                    fixture.storeOperationInfoBody(List.of("월요일"), null, null, null, false, false, false))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("대표 메뉴가 10개를 넘으면 400 과 C0001 을 반환한다")
        void returns400_whenSignatureMenusTooMany() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-operation-many-menus");

            fixture.saveStoreOperationInfo(signup.accessToken(),
                    fixture.storeOperationInfoBody(null, null, null,
                        List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"), false, false, false))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("대표 메뉴 하나가 30자를 넘으면 400 과 C0001 을 반환한다")
        void returns400_whenSignatureMenuTooLong() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-operation-long-menu");

            fixture.saveStoreOperationInfo(signup.accessToken(),
                    fixture.storeOperationInfoBody(null, null, null, List.of("가".repeat(31)), false, false, false))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("대표 메뉴에 공백만 담긴 값이 있으면 400 과 C0001 을 반환한다")
        void returns400_whenSignatureMenuBlank() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-operation-blank-menu");

            fixture.saveStoreOperationInfo(signup.accessToken(),
                    fixture.storeOperationInfoBody(null, null, null, List.of("크루아상", "   "), false, false, false))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("400 이 나면 이전에 저장한 값이 그대로 남는다")
        void keepsSavedValues_whenValidationFails() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-operation-rejected");
            fixture.saveStoreOperationInfo(signup.accessToken(),
                    fixture.storeOperationInfoBody(List.of("MONDAY"), "09:00", "22:00", List.of("크루아상"),
                        true, false, true))
                .expectStatus().isNoContent();

            fixture.saveStoreOperationInfo(signup.accessToken(),
                    fixture.storeOperationInfoBody(List.of("TUESDAY"), "9시", null, null, false, false, false))
                .expectStatus().isBadRequest();

            assertThat(fixture.storeInfo(signup.accessToken()).operation())
                .isEqualTo(new StoreOperationInfoResponse(List.of("MONDAY"), "09:00", "22:00", List.of("크루아상"),
                    true, false, true, COMPLETED.name()));
        }

        @Test
        @DisplayName("기본 정보를 저장한 뒤 운영 정보를 저장해도 기본 정보는 그대로 남는다")
        void keepsBasicInfo_whenOperationInfoSaved() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-operation-keeps-basic");
            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody("보니스커피", "CAFE", "서울 중구 을지로 100", "을지로 크루아상 카페"))
                .expectStatus().isNoContent();

            fixture.saveStoreOperationInfo(signup.accessToken(),
                    fixture.storeOperationInfoBody(List.of("MONDAY"), "09:00", "22:00", List.of("크루아상"),
                        true, false, true))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).basic())
                .isEqualTo(new StoreBasicInfoResponse("보니스커피", "CAFE", "서울 중구 을지로 100", "을지로 크루아상 카페",
                    COMPLETED.name()));
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 저장하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenSaves() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-store-operation-pending");

            fixture.saveStoreOperationInfo(login.accessToken(),
                    fixture.storeOperationInfoBody(List.of("MONDAY"), "09:00", "22:00", null, false, false, false))
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("탈퇴 대기(WITHDRAWN) 토큰으로 저장하면 403 과 A0007 을 반환한다")
        void returns403_whenWithdrawnTokenSaves() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-operation-withdrawn");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            SocialLoginResponse withdrawnLogin = fixture.naverLoginMember("naver-store-operation-withdrawn");

            fixture.saveStoreOperationInfo(withdrawnLogin.accessToken(),
                    fixture.storeOperationInfoBody(List.of("MONDAY"), "09:00", "22:00", null, false, false, false))
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 저장하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            client().put().uri("/v1/stores/me/operation")
                .contentType(MediaType.APPLICATION_JSON)
                .body(fixture.storeOperationInfoBody(List.of("MONDAY"), "09:00", "22:00", null, false, false, false))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }

    @Nested
    @DisplayName("PUT /v1/stores/me/content")
    class SaveContentInfo {

        @Test
        @DisplayName("네 필드를 다 채워 저장하면 조회에 그대로 담기고 콘텐츠 정보가 작성 완료가 된다")
        void savesAllFields_whenEveryFieldGiven() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-content-full");

            fixture.saveStoreContentInfo(signup.accessToken(),
                    fixture.storeContentInfoBody("매일 아침 직접 굽는 크루아상과 직접 로스팅한 원두",
                        List.of("디저트", "크루아상", "을지로베이커리"), "최저가, 1위 같은 과장 표현", "CASUAL"))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).content())
                .isEqualTo(new StoreContentInfoResponse("매일 아침 직접 굽는 크루아상과 직접 로스팅한 원두",
                    List.of("디저트", "크루아상", "을지로베이커리"), "최저가, 1위 같은 과장 표현", "CASUAL", COMPLETED.name()));
        }

        @Test
        @DisplayName("서버는 키워드의 # 를 붙이지도 떼지도 않고 받은 그대로 저장한다")
        void keepsKeywordsAsSent() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-content-keywords");

            fixture.saveStoreContentInfo(signup.accessToken(),
                    fixture.storeContentInfoBody(null, List.of("디저트", "#크루아상"), null, null))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).content().keywords())
                .containsExactly("디저트", "#크루아상");
        }

        @Test
        @DisplayName("키워드를 빼고 다시 저장하면 이전에 저장한 키워드가 비워진다")
        void clearsKeywords_whenSavedAgainWithoutThem() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-content-replace");
            fixture.saveStoreContentInfo(signup.accessToken(),
                    fixture.storeContentInfoBody("직접 로스팅한 원두", List.of("디저트", "크루아상"), "과장 표현", "CASUAL"))
                .expectStatus().isNoContent();

            fixture.saveStoreContentInfo(signup.accessToken(),
                    Map.of("strength", "매일 굽는 빵", "tone", "INFORMATIVE"))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).content())
                .isEqualTo(new StoreContentInfoResponse("매일 굽는 빵", List.of(), null, "INFORMATIVE",
                    COMPLETED.name()));
        }

        @Test
        @DisplayName("네 필드를 다 비워 저장하면 콘텐츠 정보가 미작성이 된다")
        void marksNotWritten_whenNothingGiven() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-content-empty");

            fixture.saveStoreContentInfo(signup.accessToken(),
                    fixture.storeContentInfoBody(null, null, null, null))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).content())
                .isEqualTo(new StoreContentInfoResponse(null, List.of(), null, null, NOT_WRITTEN.name()));
        }

        @Test
        @DisplayName("매장 강점·금지 내용·톤을 빈 문자열로 저장하면 비운 것으로 본다")
        void treatsEmptyTextAsAbsent() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-content-empty-text");

            fixture.saveStoreContentInfo(signup.accessToken(),
                    fixture.storeContentInfoBody("", null, "", ""))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).content())
                .isEqualTo(new StoreContentInfoResponse(null, List.of(), null, null, NOT_WRITTEN.name()));
        }

        @Test
        @DisplayName("매장 강점·금지 내용·톤을 공백만 담아 저장하면 비운 것으로 본다")
        void treatsBlankTextAsAbsent() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-content-blank-text");

            fixture.saveStoreContentInfo(signup.accessToken(),
                    fixture.storeContentInfoBody("   ", null, "   ", "   "))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).content())
                .isEqualTo(new StoreContentInfoResponse(null, List.of(), null, null, NOT_WRITTEN.name()));
        }

        @Test
        @DisplayName("톤을 빈 문자열로 다시 저장하면 이전에 고른 톤이 비워진다")
        void clearsTone_whenSavedAgainWithEmptyValue() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-content-clear-tone");
            fixture.saveStoreContentInfo(signup.accessToken(),
                    fixture.storeContentInfoBody(null, null, null, "CASUAL"))
                .expectStatus().isNoContent();

            fixture.saveStoreContentInfo(signup.accessToken(),
                    fixture.storeContentInfoBody(null, null, null, ""))
                .expectStatus().isNoContent();

            assertThat(fixture.storeInfo(signup.accessToken()).content())
                .isEqualTo(new StoreContentInfoResponse(null, List.of(), null, null, NOT_WRITTEN.name()));
        }

        @Test
        @DisplayName("매장 강점이 500자를 넘으면 400 과 C0001 을 반환한다")
        void returns400_whenStrengthTooLong() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-content-long-strength");

            fixture.saveStoreContentInfo(signup.accessToken(),
                    fixture.storeContentInfoBody("가".repeat(501), null, null, null))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("금지 내용이 500자를 넘으면 400 과 C0001 을 반환한다")
        void returns400_whenForbiddenTooLong() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-content-long-forbidden");

            fixture.saveStoreContentInfo(signup.accessToken(),
                    fixture.storeContentInfoBody(null, null, "가".repeat(501), null))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("키워드가 10개를 넘으면 400 과 C0001 을 반환한다")
        void returns400_whenKeywordsTooMany() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-content-many-keywords");

            fixture.saveStoreContentInfo(signup.accessToken(),
                    fixture.storeContentInfoBody(null,
                        List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"), null, null))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("키워드 하나가 30자를 넘으면 400 과 C0001 을 반환한다")
        void returns400_whenKeywordTooLong() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-content-long-keyword");

            fixture.saveStoreContentInfo(signup.accessToken(),
                    fixture.storeContentInfoBody(null, List.of("가".repeat(31)), null, null))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("키워드에 공백만 담긴 값이 있으면 400 과 C0001 을 반환한다")
        void returns400_whenKeywordBlank() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-content-blank-keyword");

            fixture.saveStoreContentInfo(signup.accessToken(),
                    fixture.storeContentInfoBody(null, List.of("디저트", "   "), null, null))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("목록에 없는 톤이면 400 과 C0001 을 반환한다")
        void returns400_whenToneNotInList() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-content-unknown-tone");

            fixture.saveStoreContentInfo(signup.accessToken(),
                    fixture.storeContentInfoBody(null, null, null, "FRIENDLY"))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("400 이 나면 이전에 저장한 값이 그대로 남는다")
        void keepsSavedValues_whenValidationFails() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-content-rejected");
            fixture.saveStoreContentInfo(signup.accessToken(),
                    fixture.storeContentInfoBody("직접 로스팅한 원두", List.of("디저트"), "과장 표현", "CASUAL"))
                .expectStatus().isNoContent();

            fixture.saveStoreContentInfo(signup.accessToken(),
                    fixture.storeContentInfoBody("가".repeat(501), null, null, null))
                .expectStatus().isBadRequest();

            assertThat(fixture.storeInfo(signup.accessToken()).content())
                .isEqualTo(new StoreContentInfoResponse("직접 로스팅한 원두", List.of("디저트"), "과장 표현", "CASUAL",
                    COMPLETED.name()));
        }

        @Test
        @DisplayName("콘텐츠 정보를 저장해도 기본 정보·운영 정보는 그대로 남는다")
        void keepsOtherGroups_whenContentInfoSaved() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-content-keeps-others");
            fixture.saveStoreBasicInfo(signup.accessToken(),
                    fixture.storeBasicInfoBody("보니스커피", "CAFE", "서울 중구 을지로 100", "을지로 크루아상 카페"))
                .expectStatus().isNoContent();
            fixture.saveStoreOperationInfo(signup.accessToken(),
                    fixture.storeOperationInfoBody(List.of("MONDAY"), "09:00", "22:00", List.of("크루아상"),
                        true, false, true))
                .expectStatus().isNoContent();

            fixture.saveStoreContentInfo(signup.accessToken(),
                    fixture.storeContentInfoBody("직접 로스팅한 원두", List.of("디저트"), "과장 표현", "CASUAL"))
                .expectStatus().isNoContent();

            StoreInfoResponse info = fixture.storeInfo(signup.accessToken());
            assertThat(info.basic())
                .isEqualTo(new StoreBasicInfoResponse("보니스커피", "CAFE", "서울 중구 을지로 100", "을지로 크루아상 카페",
                    COMPLETED.name()));
            assertThat(info.operation())
                .isEqualTo(new StoreOperationInfoResponse(List.of("MONDAY"), "09:00", "22:00", List.of("크루아상"),
                    true, false, true, COMPLETED.name()));
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 저장하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenSaves() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-store-content-pending");

            fixture.saveStoreContentInfo(login.accessToken(),
                    fixture.storeContentInfoBody("직접 로스팅한 원두", null, null, "CASUAL"))
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("탈퇴 대기(WITHDRAWN) 토큰으로 저장하면 403 과 A0007 을 반환한다")
        void returns403_whenWithdrawnTokenSaves() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-content-withdrawn");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            SocialLoginResponse withdrawnLogin = fixture.naverLoginMember("naver-store-content-withdrawn");

            fixture.saveStoreContentInfo(withdrawnLogin.accessToken(),
                    fixture.storeContentInfoBody("직접 로스팅한 원두", null, null, "CASUAL"))
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 저장하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            client().put().uri("/v1/stores/me/content")
                .contentType(MediaType.APPLICATION_JSON)
                .body(fixture.storeContentInfoBody("직접 로스팅한 원두", null, null, "CASUAL"))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }
}
