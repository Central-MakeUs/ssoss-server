package com.ssoss.ssossbackend.content.entrypoint.controller;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.content.domain.contract.GenerationRepository;
import com.ssoss.ssossbackend.content.domain.contract.GenerationResultRepository;
import com.ssoss.ssossbackend.content.domain.model.ChannelOutcome;
import com.ssoss.ssossbackend.content.domain.model.ContentErrorCode;
import com.ssoss.ssossbackend.content.domain.model.Generation;
import com.ssoss.ssossbackend.content.domain.model.GenerationResult;
import com.ssoss.ssossbackend.content.domain.model.GenerationResultStatus;
import com.ssoss.ssossbackend.content.entrypoint.response.GenerationChannelResultResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.GenerationDetailResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.GenerationStartResponse;
import com.ssoss.ssossbackend.credit.domain.contract.CreditLedgerRepository;
import com.ssoss.ssossbackend.credit.domain.model.CreditErrorCode;
import com.ssoss.ssossbackend.credit.domain.model.CreditLedger;
import com.ssoss.ssossbackend.credit.domain.model.CreditLedgerType;
import com.ssoss.ssossbackend.credit.entrypoint.response.CreditBalanceResponse;
import com.ssoss.ssossbackend.credit.entrypoint.scheduler.CreditCycleScheduler;
import com.ssoss.ssossbackend.member.domain.contract.MemberRepository;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import static com.ssoss.ssossbackend.member.domain.model.SocialProvider.NAVER;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("생성 작업 API")
class GenerationApiTest extends IntegrationTest {

    @Autowired
    private GenerationResultRepository generationResultRepository;

    @Autowired
    private GenerationRepository generationRepository;

    @Autowired
    private CreditLedgerRepository creditLedgerRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CreditCycleScheduler creditCycleScheduler;

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
        @DisplayName("키워드를 10개 넘게 보내면 400 을 반환한다")
        void returns400_whenKeywordsExceedLimit() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-many-keywords");

            fixture.startGeneration(signup.accessToken(), Map.of(
                    "channels", List.of("BLOG"),
                    "purpose", "INFORMATION",
                    "tone", "CASUAL",
                    "emphasis", "테스트 강조 내용",
                    "keywords", List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11")))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("빈 키워드를 보내면 400 을 반환한다")
        void returns400_whenKeywordBlank() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-blank-keyword");

            fixture.startGeneration(signup.accessToken(), Map.of(
                    "channels", List.of("BLOG"),
                    "purpose", "INFORMATION",
                    "tone", "CASUAL",
                    "emphasis", "테스트 강조 내용",
                    "keywords", List.of("디저트", "   ")))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("키워드 하나가 30자를 넘으면 400 을 반환한다")
        void returns400_whenKeywordTooLong() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-long-keyword");

            fixture.startGeneration(signup.accessToken(), Map.of(
                    "channels", List.of("BLOG"),
                    "purpose", "INFORMATION",
                    "tone", "CASUAL",
                    "emphasis", "테스트 강조 내용",
                    "keywords", List.of("가".repeat(31))))
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
        @DisplayName("같은 회원이 동시에 요청하면 하나만 생성되고 나머지는 409 로 거부된다")
        void createsOnlyOne_whenSameMemberRequestsConcurrently() throws Exception {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-concurrent");
            taskExecutor.hold();
            CyclicBarrier barrier = new CyclicBarrier(2);
            Callable<Integer> attempt = () -> {
                barrier.await();
                return fixture.startGeneration(signup.accessToken(), List.of("BLOG"))
                    .expectBody(String.class)
                    .returnResult()
                    .getStatus()
                    .value();
            };

            List<Integer> statuses;
            try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
                statuses = executor.invokeAll(List.of(attempt, attempt)).stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .toList();
            }

            assertThat(statuses).containsExactlyInAnyOrder(
                HttpStatus.CREATED.value(), HttpStatus.CONFLICT.value());
            assertThat(generationsOf(memberIdOf("naver-gen-concurrent"))).hasSize(1);
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
    @DisplayName("GET /v1/generations/{generationId}")
    class GetById {

        @Test
        @DisplayName("블로그 생성이 끝나면 성공 상태와 문구·제목·본문·해시태그가 반환된다")
        void returnsSucceededBlogResultWithMessage_whenBlogGenerationFinished() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-blog");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.getGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationDetailResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("COMPLETED");
                    assertThat(body.results()).singleElement().satisfies(result -> {
                        assertThat(result.channel()).isEqualTo("BLOG");
                        assertThat(result.status()).isEqualTo("SUCCEEDED");
                        assertThat(result.message()).isEqualTo(ChannelOutcome.SUCCEEDED.getMessage());
                        assertThat(result.title()).isNotBlank();
                        assertThat(result.body()).isNotBlank();
                        assertThat(result.hashtags()).isNotEmpty().allSatisfy(tag -> assertThat(tag).startsWith("#"));
                    });
                });
        }

        @Test
        @DisplayName("제목 없는 채널의 결과는 제목이 null 이다")
        void returnsNullTitle_whenChannelHasNoTitle() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-insta");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("INSTAGRAM"));

            fixture.getGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationDetailResponse.class)
                .value(body -> assertThat(body.results()).singleElement().satisfies(result -> {
                    assertThat(result.title()).isNull();
                    assertThat(result.body()).isNotBlank();
                }));
        }

        @Test
        @DisplayName("선택한 채널이 요청한 순서 그대로 결과에 담긴다")
        void returnsResultPerSelectedChannelInRequestedOrder() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-multi");
            Long generationId = fixture.startedGenerationId(signup.accessToken(),
                List.of("INSTAGRAM", "BLOG", "DAANGN_BIZ", "THREADS"));

            fixture.getGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationDetailResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("COMPLETED");
                    assertThat(body.results())
                        .extracting(GenerationChannelResultResponse::channel)
                        .containsExactly("INSTAGRAM", "BLOG", "DAANGN_BIZ", "THREADS");
                    assertThat(body.results())
                        .filteredOn(result -> result.channel().equals("BLOG"))
                        .singleElement()
                        .satisfies(result -> assertThat(result.title()).isNotBlank());
                    assertThat(body.results())
                        .filteredOn(result -> !result.channel().equals("BLOG"))
                        .allSatisfy(result -> assertThat(result.title()).isNull());
                });
        }

        @Test
        @DisplayName("생성 조건인 목적·톤·키워드가 함께 반환된다")
        void returnsGenerationConditions_whenQueried() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-condition");
            Long generationId = fixture.startGeneration(signup.accessToken(), Map.of(
                    "channels", List.of("BLOG"),
                    "purpose", "EVENT_DISCOUNT",
                    "tone", "EMOTIONAL",
                    "emphasis", "테스트 강조 내용",
                    "keywords", List.of("디저트, 크루아상", "을지로 \"베이커리\"")))
                .expectStatus().isCreated()
                .expectBody(GenerationStartResponse.class)
                .returnResult()
                .getResponseBody()
                .generationId();

            fixture.getGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationDetailResponse.class)
                .value(body -> {
                    assertThat(body.purpose()).isEqualTo("EVENT_DISCOUNT");
                    assertThat(body.tone()).isEqualTo("EMOTIONAL");
                    assertThat(body.keywords()).containsExactly("디저트, 크루아상", "을지로 \"베이커리\"");
                });
        }

        @Test
        @DisplayName("키워드를 보내지 않으면 빈 배열로 반환된다")
        void returnsEmptyKeywords_whenNotGiven() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-no-keyword");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.getGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationDetailResponse.class)
                .value(body -> assertThat(body.keywords()).isEmpty());
        }

        @Test
        @DisplayName("작업이 끝나기 전에는 선택 채널이 모두 진행 중으로 반환된다")
        void returnsPendingResults_whenGenerationNotFinished() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-progress");
            taskExecutor.hold();
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            fixture.getGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationDetailResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("IN_PROGRESS");
                    assertThat(body.results())
                        .extracting(GenerationChannelResultResponse::channel)
                        .containsExactly("BLOG", "INSTAGRAM");
                    assertThat(body.results()).allSatisfy(result -> {
                        assertThat(result.status()).isEqualTo("PENDING");
                        assertThat(result.message()).isEqualTo(ChannelOutcome.PENDING.getMessage());
                        assertThat(result.body()).isNull();
                        assertThat(result.hashtags()).isEmpty();
                    });
                });

            taskExecutor.release();

            fixture.getGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationDetailResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("COMPLETED");
                    assertThat(body.results()).allSatisfy(result ->
                        assertThat(result.status()).isEqualTo("SUCCEEDED"));
                });
        }

        @Test
        @DisplayName("모든 채널이 실패해도 선택 채널이 모두 실패 문구와 함께 반환된다")
        void returnsFailedResultsWithMessage_whenAllChannelsFail() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-all-fail");
            llmApi.stubFailure(429);
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            fixture.getGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationDetailResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("COMPLETED");
                    assertThat(body.results())
                        .extracting(GenerationChannelResultResponse::channel)
                        .containsExactly("BLOG", "INSTAGRAM");
                    assertThat(body.results()).allSatisfy(result -> {
                        assertThat(result.status()).isEqualTo("FAILED");
                        assertThat(result.message()).isEqualTo(ChannelOutcome.OVERLOADED.getMessage());
                        assertThat(result.title()).isNull();
                        assertThat(result.body()).isNull();
                        assertThat(result.hashtags()).isEmpty();
                    });
                });
        }

        @Test
        @DisplayName("deadline 이 지나도록 끝나지 않으면 남은 채널이 시간 초과 문구와 함께 실패로 반환된다")
        void returnsTimedOutResults_whenDeadlinePassedWithoutFinish() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-timeout");
            taskExecutor.hold();
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            clock.advanceBy(Generation.DEADLINE.plusSeconds(1));

            fixture.getGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationDetailResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("COMPLETED");
                    assertThat(body.results()).singleElement().satisfies(result -> {
                        assertThat(result.status()).isEqualTo("FAILED");
                        assertThat(result.message()).isEqualTo(ChannelOutcome.TIMED_OUT.getMessage());
                    });
                });
        }

        @Test
        @DisplayName("일부 채널만 성공하면 실패한 채널도 실패 문구와 함께 결과에 남는다")
        void keepsFailedChannelInResults_whenPartiallySucceeded() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-partial");
            llmApi.stubEmptyBodyForUntitled();
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            fixture.getGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationDetailResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("COMPLETED");
                    assertThat(body.results()).hasSize(2);
                    assertThat(body.results().getFirst()).satisfies(result -> {
                        assertThat(result.channel()).isEqualTo("BLOG");
                        assertThat(result.status()).isEqualTo("SUCCEEDED");
                        assertThat(result.body()).isNotBlank();
                    });
                    assertThat(body.results().getLast()).satisfies(result -> {
                        assertThat(result.channel()).isEqualTo("INSTAGRAM");
                        assertThat(result.status()).isEqualTo("FAILED");
                        assertThat(result.message()).isEqualTo(ChannelOutcome.EMPTY_OUTPUT.getMessage());
                        assertThat(result.body()).isNull();
                    });
                });
        }

        @Test
        @DisplayName("없는 작업을 조회하면 404 와 CT0002 를 반환한다")
        void returns404_whenGenerationDoesNotExist() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-not-found");

            fixture.getGeneration(signup.accessToken(), 999_999L)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.GENERATION_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("다른 회원의 작업을 조회하면 404 와 CT0002 를 반환한다")
        void returns404_whenQueryingOtherMembersGeneration() {
            SignupResponse owner = fixture.signupActiveMember("naver-gen-owner");
            Long generationId = fixture.startedGenerationId(owner.accessToken(), List.of("BLOG"));
            SignupResponse other = fixture.signupActiveMember("naver-gen-other");

            fixture.getGeneration(other.accessToken(), generationId)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.GENERATION_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 조회하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            fixture.client().get().uri("/v1/generations/1")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
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
                    "keywords", List.of("디저트 맛집", "데이트 코스")))
                .expectStatus().isCreated();

            assertThat(llmApi.recordedRequestBodies()).singleElement().satisfies(request -> assertThat(request)
                .contains("주말 아메리카노 1+1 이벤트")
                .contains("가격 인상 언급")
                .contains("디저트 맛집")
                .contains("데이트 코스")
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
    @DisplayName("사진 가이드")
    class PhotoGuide {

        @Test
        @DisplayName("사진 가이드를 체크하면 본문에 사진 가이드 태그가 조립되어 담긴다")
        void assemblesTagsIntoBody_whenPhotoGuideChecked() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-guide-on");
            Long generationId = fixture.photoGuidedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.getGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationDetailResponse.class)
                .value(body -> assertThat(body.results()).singleElement().satisfies(result -> {
                    assertThat(result.status()).isEqualTo("SUCCEEDED");
                    assertThat(result.body())
                        .contains("<photo-guide type=\"MENU\" title=\"사진 제목 1\" description=\"사진 설명 1\"/>")
                        .contains("<photo-guide type=\"MENU\" title=\"사진 제목 2\" description=\"사진 설명 2\"/>")
                        .doesNotContain("<photo-guide/>");
                }));
        }

        @Test
        @DisplayName("제목 없는 채널도 사진 가이드 태그가 조립되어 담긴다")
        void assemblesTagsIntoBody_whenChannelHasNoTitle() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-guide-untitled");
            Long generationId = fixture.photoGuidedGenerationId(signup.accessToken(), List.of("INSTAGRAM"));

            fixture.getGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationDetailResponse.class)
                .value(body -> assertThat(body.results()).singleElement().satisfies(result -> {
                    assertThat(result.status()).isEqualTo("SUCCEEDED");
                    assertThat(result.title()).isNull();
                    assertThat(result.body())
                        .contains("<photo-guide type=\"MENU\" title=\"사진 제목 1\" description=\"사진 설명 1\"/>")
                        .doesNotContain("<photo-guide/>");
                }));
        }

        @Test
        @DisplayName("사진 가이드를 체크하지 않으면 본문에 사진 가이드 태그가 없다")
        void keepsBodyPlain_whenPhotoGuideUnchecked() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-guide-off");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.getGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationDetailResponse.class)
                .value(body -> assertThat(body.results()).singleElement().satisfies(result -> {
                    assertThat(result.status()).isEqualTo("SUCCEEDED");
                    assertThat(result.body()).doesNotContain("<photo-guide");
                }));
        }

        @Test
        @DisplayName("마커가 카드보다 많아도 채널이 성공하고 짝지어진 만큼만 조립된다")
        void succeedsWithPairedTagsOnly_whenMarkersOutnumberGuides() {
            llmApi.stubPhotoGuides(3, 1);
            SignupResponse signup = fixture.signupActiveMember("naver-gen-guide-many-markers");
            Long generationId = fixture.photoGuidedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.getGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationDetailResponse.class)
                .value(body -> assertThat(body.results()).singleElement().satisfies(result -> {
                    assertThat(result.status()).isEqualTo("SUCCEEDED");
                    assertThat(result.body())
                        .containsOnlyOnce("<photo-guide type=")
                        .doesNotContain("<photo-guide/>");
                }));
        }

        @Test
        @DisplayName("카드가 마커보다 많아도 채널이 성공하고 자리 없는 카드는 버려진다")
        void succeedsDroppingExtraGuides_whenGuidesOutnumberMarkers() {
            llmApi.stubPhotoGuides(1, 3);
            SignupResponse signup = fixture.signupActiveMember("naver-gen-guide-many-guides");
            Long generationId = fixture.photoGuidedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.getGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationDetailResponse.class)
                .value(body -> assertThat(body.results()).singleElement().satisfies(result -> {
                    assertThat(result.status()).isEqualTo("SUCCEEDED");
                    assertThat(result.body())
                        .containsOnlyOnce("<photo-guide type=")
                        .contains("사진 제목 1")
                        .doesNotContain("사진 제목 2");
                }));
        }

        @Test
        @DisplayName("사진 가이드를 체크했을 때만 LLM 출력 스키마에 배열 필드가 실린다")
        void carriesPhotoGuidesField_onlyWhenChecked() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-guide-schema");

            fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            assertThat(llmApi.recordedOutputSchemas()).singleElement()
                .satisfies(schema -> assertThat(schema).contains("title").doesNotContain("photoGuides"));

            llmApi.reset();
            fixture.photoGuidedGenerationId(signup.accessToken(), List.of("BLOG"));
            assertThat(llmApi.recordedOutputSchemas()).singleElement()
                .satisfies(schema -> assertThat(schema).contains("photoGuides").doesNotContain("마커"));
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
        @DisplayName("LLM 이 429 를 반환하면 RATE_LIMITED 행으로 기록되고 조회 결과에 과부하 실패로 남는다")
        void recordsRateLimitedResult_whenLlmReturns429() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-obs-429");
            llmApi.stubFailure(429);

            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            assertThat(resultsOf(generationId)).singleElement().satisfies(result -> {
                assertThat(result.getStatus()).isEqualTo(GenerationResultStatus.RATE_LIMITED);
                assertThat(result.getInputTokens()).isNull();
                assertThat(result.getBody()).isNull();
            });
            fixture.getGeneration(signup.accessToken(), generationId)
                .expectBody(GenerationDetailResponse.class)
                .value(body -> assertThat(body.results()).singleElement().satisfies(result -> {
                    assertThat(result.channel()).isEqualTo("BLOG");
                    assertThat(result.status()).isEqualTo("FAILED");
                    assertThat(result.message()).isEqualTo(ChannelOutcome.OVERLOADED.getMessage());
                }));
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
        @DisplayName("본문이 빈 산출은 EMPTY_OUTPUT 행으로 기록되고 조회 결과에 빈 결과 실패로 남는다")
        void recordsEmptyOutputResult_whenBodyBlank() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-obs-empty");
            llmApi.stubEmptyBody();

            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("INSTAGRAM"));

            assertThat(resultsOf(generationId)).singleElement().satisfies(result -> {
                assertThat(result.getStatus()).isEqualTo(GenerationResultStatus.EMPTY_OUTPUT);
                assertThat(result.getBody()).isNull();
            });
            fixture.getGeneration(signup.accessToken(), generationId)
                .expectBody(GenerationDetailResponse.class)
                .value(body -> assertThat(body.results()).singleElement().satisfies(result -> {
                    assertThat(result.channel()).isEqualTo("INSTAGRAM");
                    assertThat(result.status()).isEqualTo("FAILED");
                    assertThat(result.message()).isEqualTo(ChannelOutcome.EMPTY_OUTPUT.getMessage());
                }));
        }
    }

    @Nested
    @DisplayName("크레딧 차감")
    class Deduction {

        @Test
        @DisplayName("성공 결과 수만큼 차감되어 잔액이 50 − 5N 으로 조회된다")
        void deductsPerSucceededResult_whenChannelsSucceed() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-credit-deduct");

            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            assertThat(balanceOf(signup.accessToken())).isEqualTo(40);
            List<Long> succeededResultIds = resultsOf(generationId).stream()
                .filter(result -> result.getStatus() == GenerationResultStatus.SUCCEEDED)
                .map(GenerationResult::getId)
                .toList();
            assertThat(deductionsOf(memberIdOf("naver-gen-credit-deduct")))
                .hasSize(2)
                .allSatisfy(entry -> assertThat(entry.getAmount()).isEqualTo(-5))
                .extracting(CreditLedger::getGenerationResultId)
                .containsExactlyInAnyOrderElementsOf(succeededResultIds);
        }

        @Test
        @DisplayName("여러 채널이 동시에 확정되어도 차감이 유실 없이 전부 반영된다")
        void deductsAllResults_whenChannelsSettleConcurrently() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-credit-fanout");

            fixture.startedGenerationId(signup.accessToken(),
                List.of("BLOG", "INSTAGRAM", "DAANGN_BIZ", "THREADS"));

            assertThat(balanceOf(signup.accessToken())).isEqualTo(30);
            assertThat(deductionsOf(memberIdOf("naver-gen-credit-fanout")))
                .hasSize(4)
                .allSatisfy(entry -> assertThat(entry.getAmount()).isEqualTo(-5));
        }

        @Test
        @DisplayName("일부 채널만 성공하면 성공한 결과만 차감된다")
        void deductsOnlySucceededResults_whenPartiallySucceeded() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-credit-partial");
            llmApi.stubEmptyBodyForUntitled();

            fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            assertThat(balanceOf(signup.accessToken())).isEqualTo(45);
            assertThat(deductionsOf(memberIdOf("naver-gen-credit-partial"))).hasSize(1);
        }

        @Test
        @DisplayName("전 채널이 실패하면 차감되지 않는다")
        void doesNotDeduct_whenAllChannelsFail() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-credit-fail");
            llmApi.stubFailure(429);

            fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            assertThat(balanceOf(signup.accessToken())).isEqualTo(50);
            assertThat(deductionsOf(memberIdOf("naver-gen-credit-fail"))).isEmpty();
        }

        @Test
        @DisplayName("같은 입력으로 다시 만들어도 같은 규칙으로 차감된다")
        void deductsAgain_whenRegeneratedWithSameInput() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-credit-again");

            fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            assertThat(balanceOf(signup.accessToken())).isEqualTo(40);
            assertThat(deductionsOf(memberIdOf("naver-gen-credit-again"))).hasSize(2);
        }

        @Test
        @DisplayName("deadline 이 지나도록 결과가 확정되지 않은 작업은 차감되지 않는다")
        void doesNotDeduct_whenGenerationExpiredWithoutSettledResult() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-credit-late");
            taskExecutor.hold();
            fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            clock.advanceBy(Generation.DEADLINE.plusSeconds(1));
            taskExecutor.release();

            assertThat(balanceOf(signup.accessToken())).isEqualTo(50);
            assertThat(deductionsOf(memberIdOf("naver-gen-credit-late"))).isEmpty();
        }

        @Test
        @DisplayName("사이클 경계를 넘겨 배치가 돌면 잔액이 50 으로 돌아온다")
        void returnsFullBalance_whenCycleBoundaryPassedAndBatchRan() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-credit-cycle");
            fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));
            assertThat(balanceOf(signup.accessToken())).isEqualTo(40);

            clock.advanceBy(Duration.ofDays(31));
            creditCycleScheduler.renewCycles();

            assertThat(balanceOf(signup.accessToken())).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("크레딧 부족 판정")
    class InsufficiencyCheck {

        private static final List<String> ALL_CHANNELS = List.of("BLOG", "INSTAGRAM", "DAANGN_BIZ", "THREADS");

        @Test
        @DisplayName("잔액이 차감량 × 선택 채널 수보다 적으면 400 으로 거부되고 작업이 생성되지 않는다")
        void returns400AndCreatesNoGeneration_whenBalanceInsufficient() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-credit-short");
            fixture.startedGenerationId(signup.accessToken(), ALL_CHANNELS);
            fixture.startedGenerationId(signup.accessToken(), ALL_CHANNELS);
            assertThat(balanceOf(signup.accessToken())).isEqualTo(10);

            fixture.startGeneration(signup.accessToken(), ALL_CHANNELS)
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CreditErrorCode.CREDIT_INSUFFICIENT.getCode()));

            assertThat(generationsOf(memberIdOf("naver-gen-credit-short"))).hasSize(2);
            assertThat(balanceOf(signup.accessToken())).isEqualTo(10);
        }

        @Test
        @DisplayName("잔액을 소진할 때까지 생성하면 다음 생성이 거부된다")
        void rejectsNextGeneration_whenBalanceExhausted() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-credit-exhaust");
            fixture.startedGenerationId(signup.accessToken(), ALL_CHANNELS);
            fixture.startedGenerationId(signup.accessToken(), ALL_CHANNELS);
            fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));
            assertThat(balanceOf(signup.accessToken())).isEqualTo(0);

            fixture.startGeneration(signup.accessToken(), List.of("BLOG"))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CreditErrorCode.CREDIT_INSUFFICIENT.getCode()));
        }
    }

    private List<GenerationResult> resultsOf(Long generationId) {
        return generationResultRepository.findAllByGenerationIdOrderById(generationId);
    }

    private List<Generation> generationsOf(Long memberId) {
        return generationRepository.findAll().stream()
            .filter(generation -> generation.getMemberId().equals(memberId))
            .toList();
    }

    private List<CreditLedger> deductionsOf(Long memberId) {
        return creditLedgerRepository.findAll().stream()
            .filter(entry -> entry.getMemberId().equals(memberId) && entry.getType() == CreditLedgerType.DEDUCT)
            .toList();
    }

    private Long memberIdOf(String socialId) {
        return memberRepository.findByProviderAndSocialId(NAVER, socialId).orElseThrow().getId();
    }

    private int balanceOf(String accessToken) {
        return fixture.creditBalance(accessToken)
            .expectStatus().isOk()
            .expectBody(CreditBalanceResponse.class)
            .returnResult()
            .getResponseBody()
            .balance();
    }
}
