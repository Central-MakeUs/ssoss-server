package com.ssoss.ssossbackend.app.entrypoint.controller;

import com.ssoss.ssossbackend.app.domain.model.AppErrorCode;
import com.ssoss.ssossbackend.app.entrypoint.response.AppVersionResponse;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("앱 버전 조회 API")
class AppVersionApiTest extends IntegrationTest {

    @Nested
    @DisplayName("GET /v1/app-versions/{os}")
    class Check {

        @Test
        @DisplayName("최소 지원 버전보다 낮은 버전으로 조회하면 업데이트가 필요하다고 응답한다")
        void returnsUpdateRequired_whenVersionIsLowerThanMinimum() {
            fixture.appVersion("IOS", "0.9.0")
                .expectStatus().isOk()
                .expectBody(AppVersionResponse.class)
                .value(body -> {
                    assertThat(body.updateRequired()).isTrue();
                    assertThat(body.minimumVersion()).isEqualTo("1.0.0");
                });
        }

        @Test
        @DisplayName("최소 지원 버전과 같은 버전으로 조회하면 업데이트가 필요하지 않다고 응답한다")
        void returnsUpdateNotRequired_whenVersionEqualsMinimum() {
            fixture.appVersion("IOS", "1.0.0")
                .expectStatus().isOk()
                .expectBody(AppVersionResponse.class)
                .value(body -> assertThat(body.updateRequired()).isFalse());
        }

        @Test
        @DisplayName("최소 지원 버전보다 높은 버전으로 조회하면 업데이트가 필요하지 않다고 응답한다")
        void returnsUpdateNotRequired_whenVersionIsHigherThanMinimum() {
            fixture.appVersion("IOS", "1.2.3")
                .expectStatus().isOk()
                .expectBody(AppVersionResponse.class)
                .value(body -> assertThat(body.updateRequired()).isFalse());
        }

        @Test
        @DisplayName("Android 로 조회하면 Android 의 최소 지원 버전을 반환한다")
        void returnsAndroidMinimumVersion_whenAndroidQueries() {
            fixture.appVersion("ANDROID", "0.9.0")
                .expectStatus().isOk()
                .expectBody(AppVersionResponse.class)
                .value(body -> {
                    assertThat(body.updateRequired()).isTrue();
                    assertThat(body.minimumVersion()).isEqualTo("1.0.0");
                });
        }

        @Test
        @DisplayName("OS 를 소문자로 보내도 조회된다")
        void returnsResult_whenOsIsLowerCase() {
            fixture.appVersion("ios", "0.9.0")
                .expectStatus().isOk()
                .expectBody(AppVersionResponse.class)
                .value(body -> assertThat(body.updateRequired()).isTrue());
        }

        @Test
        @DisplayName("지원하지 않는 OS 로 조회하면 400 과 AP0001 을 반환한다")
        void returns400_whenOsIsUnsupported() {
            fixture.appVersion("WINDOWS", "1.0.0")
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AppErrorCode.UNSUPPORTED_APP_OS.getCode()));
        }

        @Test
        @DisplayName("버전 형식이 semver 가 아니면 400 과 AP0003 을 반환한다")
        void returns400_whenVersionIsNotSemver() {
            fixture.appVersion("IOS", "1.0")
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AppErrorCode.INVALID_APP_VERSION.getCode()));
        }

        @Test
        @DisplayName("버전을 보내지 않으면 400 과 C0001 을 반환한다")
        void returns400_whenVersionMissing() {
            fixture.client().get().uri("/v1/app-versions/IOS")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }
    }
}
