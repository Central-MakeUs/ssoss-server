package com.ssoss.ssossbackend.content.entrypoint.controller;

import java.util.List;
import java.util.Map;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.content.domain.contract.GenerationResultRepository;
import com.ssoss.ssossbackend.content.domain.model.ContentErrorCode;
import com.ssoss.ssossbackend.content.domain.model.GenerationResult;
import com.ssoss.ssossbackend.content.domain.model.GenerationResultStatus;
import com.ssoss.ssossbackend.content.entrypoint.response.GenerationStartResponse;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("생성 작업 API")
class GenerationApiTest extends IntegrationTest {

    @Autowired
    private GenerationResultRepository generationResultRepository;

    @Nested
    @DisplayName("POST /v1/generations")
    class Start {

        @Test
        @DisplayName("생성을 요청하면 작업 id 와 Location 헤더를 즉시 반환한다")
        void returnsGenerationIdImmediately_whenGenerationRequested() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-start");

            fixture.startGeneration(signup.accessToken(), List.of("BLOG"))
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody(GenerationStartResponse.class)
                .value(body -> assertThat(body.generationId()).isNotNull());
        }

        @Test
        @DisplayName("진행 중 작업이 있으면 새 생성이 409 로 거부된다")
        void returns409_whenInProgressGenerationExists() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-conflict");
            taskExecutor.hold();
            fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.startGeneration(signup.accessToken(), List.of("INSTAGRAM"))
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code())
                    .isEqualTo(ContentErrorCode.GENERATION_IN_PROGRESS_EXISTS.getCode()));
        }

        @Test
        @DisplayName("완료된 작업만 있으면 새 생성이 허용된다")
        void allowsNewGeneration_whenPreviousGenerationCompleted() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-again");
            fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.startGeneration(signup.accessToken(), List.of("INSTAGRAM"))
                .expectStatus().isCreated();
        }

        @Test
        @DisplayName("강조 내용이 없으면 400 을 반환한다")
        void returns400_whenEmphasisMissing() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-no-emphasis");

            fixture.startGeneration(signup.accessToken(), Map.of(
                    "channels", List.of("BLOG"),
                    "purpose", "INFORMATION",
                    "tone", "CASUAL"))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("톤이 없으면 400 을 반환한다")
        void returns400_whenToneMissing() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-no-tone");

            fixture.startGeneration(signup.accessToken(), Map.of(
                    "channels", List.of("BLOG"),
                    "purpose", "INFORMATION",
                    "emphasis", "테스트 강조 내용"))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("채널을 하나도 고르지 않으면 400 을 반환한다")
        void returns400_whenChannelsEmpty() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-no-channel");

            fixture.startGeneration(signup.accessToken(), List.of())
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("같은 채널을 중복 선택하면 400 을 반환한다")
        void returns400_whenChannelsDuplicated() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-dup-channel");

            fixture.startGeneration(signup.accessToken(), List.of("BLOG", "BLOG"))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("대소문자만 다른 같은 채널을 선택해도 중복으로 400 을 반환한다")
        void returns400_whenChannelsDuplicatedWithDifferentCase() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-case-dup-channel");

            fixture.startGeneration(signup.accessToken(), List.of("BLOG", "blog"))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 요청하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenRequests() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-gen-pending");

            fixture.startGeneration(login.accessToken(), List.of("BLOG"))
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }
    }

    @Nested
    @DisplayName("LLM 요청")
    class LlmRequest {

        @Test
        @DisplayName("LLM 요청에 강조·금지·키워드·톤·목적 지시가 실려 있다")
        void carriesAllInstructions_inLlmRequest() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-prompt");

            fixture.startGeneration(signup.accessToken(), Map.of(
                    "channels", List.of("BLOG"),
                    "purpose", "EVENT_DISCOUNT",
                    "tone", "EMOTIONAL",
                    "emphasis", "주말 아메리카노 1+1 이벤트",
                    "forbidden", "가격 인상 언급",
                    "keywords", "디저트 맛집"))
                .expectStatus().isCreated();

            assertThat(llmApi.recordedRequestBodies()).singleElement().satisfies(request -> assertThat(request)
                .contains("주말 아메리카노 1+1 이벤트")
                .contains("가격 인상 언급")
                .contains("디저트 맛집")
                .contains("감성")
                .contains("이벤트")
                .contains("할인"));
        }

        @Test
        @DisplayName("채널 수만큼 LLM 요청이 발생한다")
        void sendsOneLlmRequestPerChannel() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-fanout");

            fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM", "THREADS"));

            assertThat(llmApi.recordedRequestBodies()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("결과 상태 기록")
    class ResultStatusRecord {

        @Test
        @DisplayName("성공한 채널은 SUCCEEDED 행으로 토큰 사용량·원문과 함께 기록된다")
        void recordsSucceededResultWithTokens_whenChannelSucceeds() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-obs-success");

            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            assertThat(resultsOf(generationId)).hasSize(2).allSatisfy(result -> {
                assertThat(result.getStatus()).isEqualTo(GenerationResultStatus.SUCCEEDED);
                assertThat(result.getInputTokens()).isEqualTo(10);
                assertThat(result.getOutputTokens()).isEqualTo(20);
                assertThat(result.getRawResponse()).contains("테스트 본문");
            });
        }

        @Test
        @DisplayName("LLM 이 429 를 반환하면 RATE_LIMITED 행으로 기록된다")
        void recordsRateLimitedResult_whenLlmReturns429() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-obs-429");
            llmApi.stubFailure(429);

            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            assertThat(resultsOf(generationId)).singleElement().satisfies(result -> {
                assertThat(result.getStatus()).isEqualTo(GenerationResultStatus.RATE_LIMITED);
                assertThat(result.getInputTokens()).isNull();
                assertThat(result.getBody()).isNull();
            });
        }

        @Test
        @DisplayName("LLM 이 5xx 를 반환하면 SERVER_ERROR 행으로 기록된다")
        void recordsServerErrorResult_whenLlmReturns5xx() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-obs-500");
            llmApi.stubFailure(500);

            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            assertThat(resultsOf(generationId)).singleElement().satisfies(result ->
                assertThat(result.getStatus()).isEqualTo(GenerationResultStatus.SERVER_ERROR));
        }

        @Test
        @DisplayName("응답이 왔지만 변환에 실패한 산출은 토큰·원문과 함께 EMPTY_OUTPUT 으로 기록된다")
        void recordsEmptyOutputWithTokens_whenContentUnparseable() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-obs-malformed");
            llmApi.stubMalformedContent();

            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            assertThat(resultsOf(generationId)).singleElement().satisfies(result -> {
                assertThat(result.getStatus()).isEqualTo(GenerationResultStatus.EMPTY_OUTPUT);
                assertThat(result.getInputTokens()).isEqualTo(10);
                assertThat(result.getOutputTokens()).isEqualTo(20);
                assertThat(result.getRawResponse()).isEqualTo("이건 JSON 이 아닙니다");
            });
        }

        @Test
        @DisplayName("본문이 빈 산출은 EMPTY_OUTPUT 행으로 기록된다")
        void recordsEmptyOutputResult_whenBodyBlank() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-obs-empty");
            llmApi.stubEmptyBody();

            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("INSTAGRAM"));

            assertThat(resultsOf(generationId)).singleElement().satisfies(result -> {
                assertThat(result.getStatus()).isEqualTo(GenerationResultStatus.EMPTY_OUTPUT);
                assertThat(result.getBody()).isNull();
            });
        }
    }

    private List<GenerationResult> resultsOf(Long generationId) {
        return generationResultRepository.findAllByGenerationIdOrderById(generationId);
    }
}
