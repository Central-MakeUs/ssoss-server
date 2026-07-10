package com.ssoss.ssossbackend.documentation;

import com.ssoss.ssossbackend.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiDocs")
class ApiDocsTest extends IntegrationTest {

    @Nested
    @DisplayName("GET /v3/api-docs")
    class GetApiDocs {

        @Test
        @DisplayName("openapi 필드를 포함한 문서가 반환된다")
        void returnsOpenApiDocument() {
            client.get().uri("/v3/api-docs")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(String.class)
                    .value(body -> assertThat(body).contains("\"openapi\""));
        }
    }

    @Nested
    @DisplayName("GET /scalar")
    class GetScalarUi {

        @Test
        @DisplayName("api-docs 를 가리키는 Scalar UI HTML 이 반환된다")
        void returnsScalarHtml() {
            client.get().uri("/scalar")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                    .expectBody(String.class)
                    .value(body -> assertThat(body)
                            .containsIgnoringCase("scalar")
                            .contains("/v3/api-docs"));
        }
    }
}
