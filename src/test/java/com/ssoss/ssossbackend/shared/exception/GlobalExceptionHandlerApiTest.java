package com.ssoss.ssossbackend.shared.exception;

import java.util.Map;

import com.ssoss.ssossbackend.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerApiTest extends IntegrationTest {

    @Nested
    @DisplayName("BusinessException")
    class Business {

        @Test
        @DisplayName("BusinessException 이 발생하면 ErrorCode 의 상태와 code, message 를 반환한다")
        void returnsErrorCodeStatusAndBody_whenBusinessExceptionThrown() {
            client().get().uri("/test/exceptions/business")
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                    .expectBody(ErrorResponse.class)
                    .value(body -> {
                        assertThat(body.code()).isEqualTo("T0001");
                        assertThat(body.message()).isEqualTo("이미 처리된 요청입니다");
                    });
        }
    }

    @Nested
    @DisplayName("검증 실패")
    class Validation {

        @Test
        @DisplayName("@Valid 검증에 실패하면 400 과 C0001, 첫 검증 메시지를 반환한다")
        void returns400AndFirstMessage_whenValidationFails() {
            Map<String, String> invalidBody = Map.of("name", "");

            client().post().uri("/test/exceptions/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(invalidBody)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(ErrorResponse.class)
                    .value(body -> {
                        assertThat(body.code()).isEqualTo("C0001");
                        assertThat(body.message()).isEqualTo("이름을 입력해 주세요");
                    });
        }

        @Test
        @DisplayName("파라미터 @Valid 에 실패하면 400 과 C0001, 위반 메시지를 반환한다")
        void returns400AndParamMessage_whenParameterValidationFails() {
            client().get().uri("/test/exceptions/param?name=a")
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(ErrorResponse.class)
                    .value(body -> {
                        assertThat(body.code()).isEqualTo("C0001");
                        assertThat(body.message()).isEqualTo("이름은 2자 이상이어야 합니다");
                    });
        }
    }

    @Nested
    @DisplayName("표준 예외 매핑")
    class StandardExceptions {

        @Test
        @DisplayName("매핑되지 않은 경로를 요청하면 404 와 C0002 code 를 반환한다")
        void returns404_whenNoHandlerMatches() {
            client().get().uri("/test/exceptions/does-not-exist")
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody(ErrorResponse.class)
                    .value(body -> assertThat(body.code()).isEqualTo("C0002"));
        }

        @Test
        @DisplayName("지원하지 않는 메서드로 요청하면 405 와 C0003 code 를 반환한다")
        void returns405_whenMethodNotSupported() {
            client().method(HttpMethod.DELETE).uri("/test/exceptions/business")
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED)
                    .expectBody(ErrorResponse.class)
                    .value(body -> assertThat(body.code()).isEqualTo("C0003"));
        }

        @Test
        @DisplayName("처리되지 않은 예외가 발생하면 500 과 C9999 code 를 반환한다")
        void returns500_whenUnexpectedExceptionThrown() {
            client().get().uri("/test/exceptions/unexpected")
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                    .expectBody(ErrorResponse.class)
                    .value(body -> assertThat(body.code()).isEqualTo("C9999"));
        }
    }

    @Nested
    @DisplayName("잘못된 요청")
    class BadRequest {

        @Test
        @DisplayName("본문 JSON 이 깨지면 400 과 C0001 을 반환한다")
        void returns400_whenBodyIsMalformed() {
            client().post().uri("/test/exceptions/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{")
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(ErrorResponse.class)
                    .value(body -> assertThat(body.code()).isEqualTo("C0001"));
        }

        @Test
        @DisplayName("지원하지 않는 Content-Type 이면 415 와 C0005 를 반환한다")
        void returns415_whenMediaTypeUnsupported() {
            client().post().uri("/test/exceptions/validate")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("plain text")
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .expectBody(ErrorResponse.class)
                    .value(body -> assertThat(body.code()).isEqualTo("C0005"));
        }

        @Test
        @DisplayName("필수 파라미터가 없으면 400 과 C0001 을 반환한다")
        void returns400_whenRequiredParameterMissing() {
            client().get().uri("/test/exceptions/param")
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(ErrorResponse.class)
                    .value(body -> assertThat(body.code()).isEqualTo("C0001"));
        }

        @Test
        @DisplayName("파라미터 타입이 맞지 않으면 400 과 C0001 을 반환한다")
        void returns400_whenParameterTypeMismatch() {
            client().get().uri("/test/exceptions/typed?number=abc")
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(ErrorResponse.class)
                    .value(body -> assertThat(body.code()).isEqualTo("C0001"));
        }
    }
}
