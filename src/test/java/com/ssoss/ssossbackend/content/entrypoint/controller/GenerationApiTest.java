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
import com.ssoss.ssossbackend.content.domain.model.ContentErrorCode;
import com.ssoss.ssossbackend.content.domain.model.Generation;
import com.ssoss.ssossbackend.content.domain.model.GenerationResult;
import com.ssoss.ssossbackend.content.domain.model.GenerationResultStatus;
import com.ssoss.ssossbackend.content.entrypoint.response.GenerationChannelResultResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.GenerationPollResponse;
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
    class Poll {

        @Test
        @DisplayName("블로그 생성이 끝나면 성공 상태와 제목·본문·해시태그가 반환된다")
        void returnsSucceededBlogResultWithTitle_whenBlogGenerationFinished() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-blog");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.pollGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationPollResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("SUCCEEDED");
                    assertThat(body.pendingChannels()).isEmpty();
                    assertThat(body.results()).singleElement().satisfies(result -> {
                        assertThat(result.channel()).isEqualTo("BLOG");
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

            fixture.pollGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationPollResponse.class)
                .value(body -> assertThat(body.results()).singleElement().satisfies(result -> {
                    assertThat(result.title()).isNull();
                    assertThat(result.body()).isNotBlank();
                }));
        }

        @Test
        @DisplayName("다중 채널을 선택하면 채널 수만큼 결과가 오고 성공으로 파생된다")
        void returnsResultPerChannel_whenMultipleChannelsSelected() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-multi");
            Long generationId = fixture.startedGenerationId(signup.accessToken(),
                List.of("BLOG", "INSTAGRAM", "DAANGN_BIZ", "THREADS"));

            fixture.pollGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationPollResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("SUCCEEDED");
                    assertThat(body.pendingChannels()).isEmpty();
                    assertThat(body.results())
                        .extracting(GenerationChannelResultResponse::channel)
                        .containsExactlyInAnyOrder("BLOG", "INSTAGRAM", "DAANGN_BIZ", "THREADS");
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
        @DisplayName("작업이 끝나기 전에는 진행 중 상태와 진행 중 채널 목록이 반환된다")
        void returnsInProgressWithPendingChannels_whenGenerationNotFinished() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-progress");
            taskExecutor.hold();
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            fixture.pollGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationPollResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("IN_PROGRESS");
                    assertThat(body.results()).isEmpty();
                    assertThat(body.pendingChannels()).containsExactlyInAnyOrder("BLOG", "INSTAGRAM");
                });

            taskExecutor.release();

            fixture.pollGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationPollResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("SUCCEEDED");
                    assertThat(body.results()).hasSize(2);
                    assertThat(body.pendingChannels()).isEmpty();
                });
        }

        @Test
        @DisplayName("없는 작업을 폴링하면 404 와 CT0002 를 반환한다")
        void returns404_whenGenerationDoesNotExist() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-not-found");

            fixture.pollGeneration(signup.accessToken(), 999_999L)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.GENERATION_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("다른 회원의 작업을 폴링하면 404 와 CT0002 를 반환한다")
        void returns404_whenPollingOtherMembersGeneration() {
            SignupResponse owner = fixture.signupActiveMember("naver-gen-owner");
            Long generationId = fixture.startedGenerationId(owner.accessToken(), List.of("BLOG"));
            SignupResponse other = fixture.signupActiveMember("naver-gen-other");

            fixture.pollGeneration(other.accessToken(), generationId)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.GENERATION_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("모든 채널이 실패하면 실패 상태와 원인 범주가 반환된다")
        void returnsFailedWithReason_whenAllChannelsFail() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-all-fail");
            llmApi.stubFailure(429);
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            fixture.pollGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationPollResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("FAILED");
                    assertThat(body.failureReason()).isEqualTo("OVERLOADED");
                    assertThat(body.results()).isEmpty();
                });
        }

        @Test
        @DisplayName("deadline 이 지나도록 끝나지 않은 작업은 실패로 파생되고 원인은 null 이다")
        void returnsFailedWithoutReason_whenDeadlinePassedWithoutFinish() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-timeout");
            taskExecutor.hold();
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            clock.advanceBy(Generation.DEADLINE.plusSeconds(1));

            fixture.pollGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationPollResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("FAILED");
                    assertThat(body.failureReason()).isNull();
                    assertThat(body.results()).isEmpty();
                });
        }

        @Test
        @DisplayName("일부 채널만 성공해도 성공으로 파생되고 실패 원인은 실리지 않는다")
        void derivesSucceededWithoutReason_whenPartiallySucceeded() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-partial");
            llmApi.stubEmptyBodyForUntitled();
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            fixture.pollGeneration(signup.accessToken(), generationId)
                .expectStatus().isOk()
                .expectBody(GenerationPollResponse.class)
                .value(body -> {
                    assertThat(body.status()).isEqualTo("SUCCEEDED");
                    assertThat(body.failureReason()).isNull();
                    assertThat(body.results()).hasSize(1);
                    assertThat(body.pendingChannels()).isEmpty();
                });
        }

        @Test
        @DisplayName("액세스 토큰 없이 폴링하면 401 과 A0006 을 반환한다")
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
        @DisplayName("LLM 이 429 를 반환하면 RATE_LIMITED 행으로 기록되고 폴링 결과에서 빠진다")
        void recordsRateLimitedResult_whenLlmReturns429() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-obs-429");
            llmApi.stubFailure(429);

            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            assertThat(resultsOf(generationId)).singleElement().satisfies(result -> {
                assertThat(result.getStatus()).isEqualTo(GenerationResultStatus.RATE_LIMITED);
                assertThat(result.getInputTokens()).isNull();
                assertThat(result.getBody()).isNull();
            });
            fixture.pollGeneration(signup.accessToken(), generationId)
                .expectBody(GenerationPollResponse.class)
                .value(body -> assertThat(body.results()).isEmpty());
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
        @DisplayName("본문이 빈 산출은 EMPTY_OUTPUT 행으로 기록되고 폴링 결과에서 빠진다")
        void recordsEmptyOutputResult_whenBodyBlank() {
            SignupResponse signup = fixture.signupActiveMember("naver-gen-obs-empty");
            llmApi.stubEmptyBody();

            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("INSTAGRAM"));

            assertThat(resultsOf(generationId)).singleElement().satisfies(result -> {
                assertThat(result.getStatus()).isEqualTo(GenerationResultStatus.EMPTY_OUTPUT);
                assertThat(result.getBody()).isNull();
            });
            fixture.pollGeneration(signup.accessToken(), generationId)
                .expectBody(GenerationPollResponse.class)
                .value(body -> assertThat(body.results()).isEmpty());
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
                .filter(GenerationResult::isSucceeded)
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
