package com.ssoss.ssossbackend.content.entrypoint.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.content.domain.contract.ContentRepository;
import com.ssoss.ssossbackend.content.domain.contract.GenerationResultRepository;
import com.ssoss.ssossbackend.content.domain.model.ContentErrorCode;
import com.ssoss.ssossbackend.content.domain.model.GenerationResult;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentChannelResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentChannelSummaryResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentDetailResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentListResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentSaveResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentSummaryResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.GenerationChannelResultResponse;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("콘텐츠 API")
class ContentApiTest extends IntegrationTest {

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private GenerationResultRepository generationResultRepository;

    @Nested
    @DisplayName("POST /v1/contents")
    class Save {

        @Test
        @DisplayName("여러 채널이 성공한 작업을 저장하면 콘텐츠 1건에 채널이 정해진 순서로 담긴다")
        void savesOneContentWithEveryChannelInChannelOrder_whenGenerationSaved() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-multi");
            Long generationId = fixture.startedGenerationId(signup.accessToken(),
                List.of("THREADS", "DAANGN_BIZ", "BLOG", "INSTAGRAM"));

            fixture.saveGeneratedContents(signup.accessToken(), generationId)
                .expectStatus().isCreated()
                .expectBody(ContentSaveResponse.class)
                .value(body -> {
                    assertThat(body.contentId()).isNotNull();
                    assertThat(body.contents()).hasSize(4)
                        .allSatisfy(content -> assertThat(content.contentChannelId()).isNotNull());
                    assertThat(body.contents())
                        .extracting(ContentChannelSummaryResponse::channel)
                        .containsExactly("BLOG", "INSTAGRAM", "DAANGN_BIZ", "THREADS");
                });

            assertThat(database.contentsOf(database.memberIdOf("naver-save-multi"))).hasSize(1);
            assertThat(database.channelsOf(database.memberIdOf("naver-save-multi"))).hasSize(4);
        }

        @Test
        @DisplayName("생성 결과를 그대로 보내면 그 값이 저장되고 원본 생성 결과 참조가 남는다")
        void savesRequestedValuesWithSourceGenerationResult_whenGenerationSaved() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-copy");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            fixture.contentsOfGeneration(signup.accessToken(), generationId);

            List<GenerationResult> results = generationResultRepository
                .findAllByGenerationIdOrderById(generationId);
            assertThat(database.channelsOf(database.memberIdOf("naver-save-copy"))).hasSize(2).allSatisfy(channel -> {
                GenerationResult origin = results.stream()
                    .filter(result -> result.getId().equals(channel.getGenerationResultId()))
                    .findFirst()
                    .orElseThrow();
                assertThat(channel.getChannel()).isEqualTo(origin.getChannel());
                assertThat(channel.getTitle()).isEqualTo(origin.getTitle());
                assertThat(channel.getBody()).isEqualTo(origin.getBody());
                assertThat(channel.getHashtags()).isEqualTo(origin.getHashtags());
                assertThat(channel.getDeletedAt()).isNull();
            });
        }

        @Test
        @DisplayName("생성 결과와 다른 제목·본문·해시태그를 보내면 보낸 값이 저장된다")
        void savesRequestedValues_whenRequestCarriesEditedValues() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-edited");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            ContentSaveResponse saved = fixture.saveContents(signup.accessToken(), Map.of(
                    "generationId", generationId,
                    "contents", List.of(
                        fixture.channelContent("BLOG", "고친 제목", "고친 블로그 본문", List.of("#고친태그")),
                        fixture.channelContent("INSTAGRAM", null, "고친 인스타 본문", List.of()))))
                .expectStatus().isCreated()
                .expectBody(ContentSaveResponse.class)
                .returnResult()
                .getResponseBody();

            assertThat(fixture.contentDetail(signup.accessToken(), saved.contentId()).contents()).satisfiesExactly(
                blog -> {
                    assertThat(blog.channel()).isEqualTo("BLOG");
                    assertThat(blog.title()).isEqualTo("고친 제목");
                    assertThat(blog.body()).isEqualTo("고친 블로그 본문");
                    assertThat(blog.hashtags()).containsExactly("#고친태그");
                },
                instagram -> {
                    assertThat(instagram.channel()).isEqualTo("INSTAGRAM");
                    assertThat(instagram.title()).isNull();
                    assertThat(instagram.body()).isEqualTo("고친 인스타 본문");
                    assertThat(instagram.hashtags()).isEmpty();
                });
        }

        @Test
        @DisplayName("작업을 저장하면 콘텐츠에 원본 작업 참조와 목적·톤·키워드가 복사된다")
        void copiesGenerationReferenceAndConditions_whenGenerationSaved() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-condition");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), Map.of(
                "channels", List.of("BLOG"),
                "purpose", "EVENT_DISCOUNT",
                "tone", "EMOTIONAL",
                "emphasis", "테스트 강조 내용",
                "keywords", List.of("디저트", "크루아상")));

            fixture.contentsOfGeneration(signup.accessToken(), generationId);

            assertThat(database.contentsOf(database.memberIdOf("naver-save-condition"))).singleElement().satisfies(content -> {
                assertThat(content.getGenerationId()).isEqualTo(generationId);
                assertThat(content.getPurpose().name()).isEqualTo("EVENT_DISCOUNT");
                assertThat(content.getTone().name()).isEqualTo("EMOTIONAL");
                assertThat(content.keywordList()).containsExactly("디저트", "크루아상");
            });
        }

        @Test
        @DisplayName("같은 작업을 다시 저장해도 콘텐츠가 늘지 않고 기존이 그대로 반환된다")
        void returnsExistingContents_whenSavedAgain() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-twice");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));
            ContentSaveResponse first = fixture.contentsOfGeneration(signup.accessToken(), generationId);

            ContentSaveResponse second = fixture.contentsOfGeneration(signup.accessToken(), generationId);

            assertThat(second.contentId()).isEqualTo(first.contentId());
            assertThat(second.contents())
                .extracting(ContentChannelSummaryResponse::contentChannelId)
                .containsExactlyElementsOf(first.contents().stream()
                    .map(ContentChannelSummaryResponse::contentChannelId)
                    .toList());
            assertThat(database.contentsOf(database.memberIdOf("naver-save-twice"))).hasSize(1);
            assertThat(database.channelsOf(database.memberIdOf("naver-save-twice"))).hasSize(2);
        }

        @Test
        @DisplayName("같은 작업을 다른 값으로 다시 저장해도 처음 저장한 값이 그대로 남는다")
        void keepsFirstSavedValues_whenSavedAgainWithOtherValues() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-twice-values");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            ContentSaveResponse first = fixture.contentsOfGeneration(signup.accessToken(), generationId);

            fixture.saveContents(signup.accessToken(), Map.of(
                    "generationId", generationId,
                    "contents", List.of(fixture.channelContent("BLOG", "나중 제목", "나중 본문", List.of("#나중태그")))))
                .expectStatus().isCreated();

            assertThat(fixture.contentDetail(signup.accessToken(), first.contentId()).contents())
                .singleElement()
                .satisfies(content -> {
                    assertThat(content.title()).isEqualTo("테스트 제목");
                    assertThat(content.body()).isNotEqualTo("나중 본문");
                    assertThat(content.hashtags()).doesNotContain("#나중태그");
                });
        }

        @Test
        @DisplayName("삭제한 콘텐츠의 작업을 다시 저장하면 409 와 CT0009 를 반환하고 콘텐츠는 삭제된 채로 남는다")
        void returns409_whenSavingGenerationOfDeletedContent() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-deleted");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));
            Long contentId = fixture.savedContentId(signup.accessToken(), generationId);
            fixture.deletedContent(signup.accessToken(), contentId);

            fixture.saveGeneratedContents(signup.accessToken(), generationId)
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.CONTENT_DELETED.getCode()));

            assertThat(database.contentsOf(database.memberIdOf("naver-save-deleted"))).hasSize(1);
            assertThat(database.channelsOf(database.memberIdOf("naver-save-deleted"))).hasSize(2)
                .allSatisfy(channel -> assertThat(channel.getDeletedAt()).isNotNull());
        }

        @Test
        @DisplayName("채널 하나가 실패한 작업을 저장하면 400 과 CT0003 을 반환한다")
        void returns400_whenOneChannelFailed() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-one-fail");
            llmApi.stubEmptyBodyForUntitled();
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            fixture.saveContents(signup.accessToken(), generationId, List.of("BLOG", "INSTAGRAM"))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.GENERATION_FAILED.getCode()));

            assertThat(database.contentsOf(database.memberIdOf("naver-save-one-fail"))).isEmpty();
        }

        @Test
        @DisplayName("전 채널이 실패한 작업을 저장하면 400 과 CT0003 을 반환한다")
        void returns400_whenAllChannelsFailed() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-all-fail");
            llmApi.stubFailure(429);
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            fixture.saveContents(signup.accessToken(), generationId, List.of("BLOG", "INSTAGRAM"))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.GENERATION_FAILED.getCode()));

            assertThat(database.contentsOf(database.memberIdOf("naver-save-all-fail"))).isEmpty();
        }

        @Test
        @DisplayName("아직 진행 중인 작업을 저장하면 409 와 CT0004 를 반환한다")
        void returns409_whenGenerationStillInProgress() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-in-progress");
            taskExecutor.hold();
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            fixture.saveContents(signup.accessToken(), generationId, List.of("BLOG", "INSTAGRAM"))
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code())
                    .isEqualTo(ContentErrorCode.GENERATION_NOT_FINISHED.getCode()));

            assertThat(database.contentsOf(database.memberIdOf("naver-save-in-progress"))).isEmpty();
        }

        @Test
        @DisplayName("없는 작업을 저장하면 404 와 CT0002 를 반환한다")
        void returns404_whenGenerationDoesNotExist() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-not-found");

            fixture.saveContents(signup.accessToken(), 999_999L, List.of("BLOG"))
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.GENERATION_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("다른 회원의 작업을 저장하면 404 와 CT0002 를 반환한다")
        void returns404_whenSavingOtherMembersGeneration() {
            SignupResponse owner = fixture.signupActiveMember("naver-save-owner");
            Long generationId = fixture.startedGenerationId(owner.accessToken(), List.of("BLOG"));
            SignupResponse other = fixture.signupActiveMember("naver-save-other");

            fixture.saveContents(other.accessToken(), generationId, List.of("BLOG"))
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.GENERATION_NOT_FOUND.getCode()));

            assertThat(database.contentsOf(database.memberIdOf("naver-save-other"))).isEmpty();
        }

        @Test
        @DisplayName("작업의 채널을 빠뜨리고 저장하면 400 과 CT0010 을 반환한다")
        void returns400_whenChannelMissingFromRequest() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-missing-channel");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            fixture.saveContents(signup.accessToken(), generationId, List.of("BLOG"))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code())
                    .isEqualTo(ContentErrorCode.SAVE_CHANNELS_MISMATCHED.getCode()));

            assertThat(database.contentsOf(database.memberIdOf("naver-save-missing-channel"))).isEmpty();
        }

        @Test
        @DisplayName("작업에 없는 채널을 보내면 400 과 CT0010 을 반환한다")
        void returns400_whenRequestHasChannelOutsideGeneration() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-extra-channel");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.saveContents(signup.accessToken(), generationId, List.of("BLOG", "THREADS"))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code())
                    .isEqualTo(ContentErrorCode.SAVE_CHANNELS_MISMATCHED.getCode()));

            assertThat(database.contentsOf(database.memberIdOf("naver-save-extra-channel"))).isEmpty();
        }

        @Test
        @DisplayName("같은 채널을 두 번 보내면 400 과 CT0010 을 반환한다")
        void returns400_whenChannelDuplicated() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-duplicate-channel");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.saveContents(signup.accessToken(), Map.of(
                    "generationId", generationId,
                    "contents", List.of(
                        fixture.channelContent("BLOG", "제목 하나", "본문 하나", List.of()),
                        fixture.channelContent("BLOG", "제목 둘", "본문 둘", List.of()))))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code())
                    .isEqualTo(ContentErrorCode.SAVE_CHANNELS_MISMATCHED.getCode()));

            assertThat(database.contentsOf(database.memberIdOf("naver-save-duplicate-channel"))).isEmpty();
        }

        @Test
        @DisplayName("없는 채널 이름을 보내면 400 과 C0001 을 반환한다")
        void returns400_whenChannelUnknown() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-unknown-channel");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.saveContents(signup.accessToken(), Map.of(
                    "generationId", generationId,
                    "contents", List.of(fixture.channelContent("FACEBOOK", "제목", "본문", List.of()))))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));

            assertThat(database.contentsOf(database.memberIdOf("naver-save-unknown-channel"))).isEmpty();
        }

        @Test
        @DisplayName("블로그를 제목 없이 저장하면 400 과 CT0007 을 반환하고 아무것도 저장되지 않는다")
        void returns400_whenTitleMissingForBlog() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-no-title");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.saveContents(signup.accessToken(), Map.of(
                    "generationId", generationId,
                    "contents", List.of(fixture.channelContent("BLOG", null, "제목 없는 본문", List.of()))))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.TITLE_REQUIRED.getCode()));

            assertThat(database.contentsOf(database.memberIdOf("naver-save-no-title"))).isEmpty();
            assertThat(database.channelsOf(database.memberIdOf("naver-save-no-title"))).isEmpty();
        }

        @Test
        @DisplayName("이미 저장한 작업에 제목 없는 블로그를 다시 보내도 400 과 CT0007 을 반환한다")
        void returns400_whenSavedAgainWithInvalidPayload() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-again-invalid");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            fixture.contentsOfGeneration(signup.accessToken(), generationId);

            fixture.saveContents(signup.accessToken(), Map.of(
                    "generationId", generationId,
                    "contents", List.of(fixture.channelContent("BLOG", null, "제목 없는 본문", List.of()))))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.TITLE_REQUIRED.getCode()));
        }

        @Test
        @DisplayName("제목 없는 채널에 제목을 보내면 400 과 CT0008 을 반환하고 아무것도 저장되지 않는다")
        void returns400_whenTitleSentForUntitledChannel() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-extra-title");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("INSTAGRAM"));

            fixture.saveContents(signup.accessToken(), Map.of(
                    "generationId", generationId,
                    "contents", List.of(fixture.channelContent("INSTAGRAM", "쓸 수 없는 제목", "본문", List.of()))))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.TITLE_NOT_ALLOWED.getCode()));

            assertThat(database.contentsOf(database.memberIdOf("naver-save-extra-title"))).isEmpty();
            assertThat(database.channelsOf(database.memberIdOf("naver-save-extra-title"))).isEmpty();
        }

        @Test
        @DisplayName("본문이 비어 있으면 400 과 C0001 을 반환한다")
        void returns400_whenBodyBlank() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-blank-body");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.saveContents(signup.accessToken(), Map.of(
                    "generationId", generationId,
                    "contents", List.of(fixture.channelContent("BLOG", "제목", " ", List.of()))))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));

            assertThat(database.contentsOf(database.memberIdOf("naver-save-blank-body"))).isEmpty();
        }

        @Test
        @DisplayName("채널별 콘텐츠 자리에 빈 값을 보내면 400 과 C0001 을 반환한다")
        void returns400_whenChannelContentNull() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-null-content");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.saveContents(signup.accessToken(), Map.of(
                    "generationId", generationId,
                    "contents", Collections.singletonList(null)))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));

            assertThat(database.contentsOf(database.memberIdOf("naver-save-null-content"))).isEmpty();
        }

        @Test
        @DisplayName("해시태그에 빈 값이 섞이면 400 과 C0001 을 반환한다")
        void returns400_whenHashtagNull() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-null-hashtag");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.saveContents(signup.accessToken(), Map.of(
                    "generationId", generationId,
                    "contents", List.of(
                        fixture.channelContent("BLOG", "제목", "본문", Collections.singletonList(null)))))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));

            assertThat(database.contentsOf(database.memberIdOf("naver-save-null-hashtag"))).isEmpty();
        }

        @Test
        @DisplayName("제목이 60자를 넘으면 400 과 C0001 을 반환한다")
        void returns400_whenTitleTooLong() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-long-title");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.saveContents(signup.accessToken(), Map.of(
                    "generationId", generationId,
                    "contents", List.of(fixture.channelContent("BLOG", "가".repeat(61), "본문", List.of()))))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));

            assertThat(database.contentsOf(database.memberIdOf("naver-save-long-title"))).isEmpty();
        }

        @Test
        @DisplayName("해시태그가 20개를 넘으면 400 과 C0001 을 반환한다")
        void returns400_whenTooManyHashtags() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-many-hashtags");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.saveContents(signup.accessToken(), Map.of(
                    "generationId", generationId,
                    "contents", List.of(fixture.channelContent("BLOG", "제목", "본문",
                        IntStream.rangeClosed(1, 21).mapToObj(index -> "#태그" + index).toList()))))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));

            assertThat(database.contentsOf(database.memberIdOf("naver-save-many-hashtags"))).isEmpty();
        }

        @Test
        @DisplayName("채널별 콘텐츠를 보내지 않으면 400 과 CT0010 을 반환한다")
        void returns400_whenContentsMissing() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-no-contents");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.saveContents(signup.accessToken(), Map.of("generationId", generationId))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code())
                    .isEqualTo(ContentErrorCode.SAVE_CHANNELS_MISMATCHED.getCode()));

            assertThat(database.contentsOf(database.memberIdOf("naver-save-no-contents"))).isEmpty();
        }

        @Test
        @DisplayName("진행 중인 작업은 채널별 콘텐츠 없이 저장해도 409 와 CT0004 를 반환한다")
        void returns409_whenInProgressGenerationSavedWithoutContents() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-in-progress-empty");
            taskExecutor.hold();
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.saveContents(signup.accessToken(), Map.of("generationId", generationId))
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code())
                    .isEqualTo(ContentErrorCode.GENERATION_NOT_FINISHED.getCode()));
        }

        @Test
        @DisplayName("실패한 작업은 채널별 콘텐츠 없이 저장해도 400 과 CT0003 을 반환한다")
        void returns400_whenFailedGenerationSavedWithoutContents() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-failed-empty");
            llmApi.stubFailure(429);
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));

            fixture.saveContents(signup.accessToken(), Map.of("generationId", generationId))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.GENERATION_FAILED.getCode()));
        }

        @Test
        @DisplayName("작업 id 를 보내지 않으면 400 을 반환한다")
        void returns400_whenGenerationIdMissing() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-no-id");

            fixture.saveContents(signup.accessToken(), Map.of())
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 저장하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            fixture.client().post().uri("/v1/contents")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 저장하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenRequests() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-save-pending");

            fixture.saveContents(login.accessToken(), 1L, List.of("BLOG"))
                .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }
    }

    @Nested
    @DisplayName("GET /v1/contents")
    class GetList {

        @Test
        @DisplayName("3채널을 저장한 작업이 카드 1건으로 나오고 채널이 정해진 순서로 담긴다")
        void returnsOneCardWithEveryChannelInChannelOrder_whenThreeChannelsSaved() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-one-card");
            Long generationId = fixture.startedGenerationId(signup.accessToken(),
                List.of("THREADS", "BLOG", "INSTAGRAM"));
            Long contentId = fixture.savedContentId(signup.accessToken(), generationId);

            fixture.getContents(signup.accessToken(), "")
                .expectStatus().isOk()
                .expectBody(ContentListResponse.class)
                .value(body -> {
                    assertThat(body.totalCount()).isEqualTo(1);
                    assertThat(body.contents()).singleElement().satisfies(content -> {
                        assertThat(content.contentId()).isEqualTo(contentId);
                        assertThat(content.channels()).containsExactly("BLOG", "INSTAGRAM", "THREADS");
                    });
                });
        }

        @Test
        @DisplayName("저장한 콘텐츠가 저장 시각 최신순으로 온다")
        void returnsContentsInSavedAtDescendingOrder() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-order");
            Long oldestId = fixture.savedContentId(signup.accessToken(), List.of("BLOG"));
            Long middleId = fixture.savedContentId(signup.accessToken(), List.of("INSTAGRAM"));
            Long newestId = fixture.savedContentId(signup.accessToken(), List.of("THREADS"));

            assertThat(fixture.contentList(signup.accessToken(), "").contents())
                .extracting(ContentSummaryResponse::contentId)
                .containsExactly(newestId, middleId, oldestId);
        }

        @Test
        @DisplayName("sort 를 LATEST 로 부르면 생략한 것과 같이 최신순으로 온다")
        void returnsContentsInSavedAtDescendingOrder_whenLatestRequested() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-latest-explicit");
            Long oldestId = fixture.savedContentId(signup.accessToken(), List.of("BLOG"));
            Long newestId = fixture.savedContentId(signup.accessToken(), List.of("THREADS"));

            assertThat(fixture.contentList(signup.accessToken(), "?sort=LATEST").contents())
                .extracting(ContentSummaryResponse::contentId)
                .containsExactly(newestId, oldestId);
        }

        @Test
        @DisplayName("sort 를 빈 값으로 보내면 생략한 것과 같이 최신순으로 온다")
        void returnsContentsInSavedAtDescendingOrder_whenSortBlank() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-blank-sort");
            Long oldestId = fixture.savedContentId(signup.accessToken(), List.of("BLOG"));
            Long newestId = fixture.savedContentId(signup.accessToken(), List.of("THREADS"));

            assertThat(fixture.contentList(signup.accessToken(), "?sort=").contents())
                .extracting(ContentSummaryResponse::contentId)
                .containsExactly(newestId, oldestId);
        }

        @Test
        @DisplayName("sort 를 OLDEST 로 부르면 저장 시각 오래된 순으로 온다")
        void returnsContentsInSavedAtAscendingOrder_whenOldestRequested() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-oldest");
            Long oldestId = fixture.savedContentId(signup.accessToken(), List.of("BLOG"));
            Long middleId = fixture.savedContentId(signup.accessToken(), List.of("INSTAGRAM"));
            Long newestId = fixture.savedContentId(signup.accessToken(), List.of("THREADS"));

            assertThat(fixture.contentList(signup.accessToken(), "?sort=OLDEST").contents())
                .extracting(ContentSummaryResponse::contentId)
                .containsExactly(oldestId, middleId, newestId);
        }

        @Test
        @DisplayName("sort 를 OLDEST 로 부르고 페이지를 넘겨도 오래된 순이 이어진다")
        void keepsAscendingOrderAcrossPages_whenOldestRequested() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-oldest-paging");
            Long oldestId = fixture.savedContentId(signup.accessToken(), List.of("BLOG"));
            Long middleId = fixture.savedContentId(signup.accessToken(), List.of("INSTAGRAM"));
            Long newestId = fixture.savedContentId(signup.accessToken(), List.of("THREADS"));

            ContentListResponse firstPage = fixture.contentList(signup.accessToken(), "?sort=OLDEST&page=0&size=2");
            ContentListResponse secondPage = fixture.contentList(signup.accessToken(), "?sort=OLDEST&page=1&size=2");

            assertThat(firstPage.totalCount()).isEqualTo(3);
            assertThat(firstPage.hasNext()).isTrue();
            assertThat(firstPage.contents())
                .extracting(ContentSummaryResponse::contentId)
                .containsExactly(oldestId, middleId);
            assertThat(secondPage.totalCount()).isEqualTo(3);
            assertThat(secondPage.hasNext()).isFalse();
            assertThat(secondPage.contents())
                .extracting(ContentSummaryResponse::contentId)
                .containsExactly(newestId);
        }

        @Test
        @DisplayName("콘텐츠를 편집해도 목록 정렬 순서는 그대로다")
        void keepsOrder_whenContentEdited() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-edited-order");
            ContentSaveResponse oldest = fixture.contentsOfGeneration(signup.accessToken(),
                fixture.startedGenerationId(signup.accessToken(), List.of("BLOG")));
            clock.advanceBy(Duration.ofMinutes(1));
            Long newestId = fixture.savedContentId(signup.accessToken(), List.of("THREADS"));

            fixture.editContentChannel(signup.accessToken(), oldest.contentId(),
                    oldest.contents().getFirst().contentChannelId(), Map.of(
                        "title", "직접 고친 제목",
                        "body", "직접 고친 본문",
                        "hashtags", List.of()))
                .expectStatus().isOk();

            assertThat(fixture.contentList(signup.accessToken(), "").contents())
                .extracting(ContentSummaryResponse::contentId)
                .containsExactly(newestId, oldest.contentId());
        }

        @Test
        @DisplayName("page 와 size 로 페이지를 넘겨도 전체 건수는 그대로이고 마지막 페이지는 hasNext 가 false 다")
        void returnsRequestedPageWithTotalCountAndHasNext() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-paging");
            Long firstId = fixture.savedContentId(signup.accessToken(), List.of("BLOG"));
            Long secondId = fixture.savedContentId(signup.accessToken(), List.of("INSTAGRAM"));
            Long thirdId = fixture.savedContentId(signup.accessToken(), List.of("THREADS"));

            ContentListResponse firstPage = fixture.contentList(signup.accessToken(), "?page=0&size=2");
            ContentListResponse secondPage = fixture.contentList(signup.accessToken(), "?page=1&size=2");

            assertThat(firstPage.totalCount()).isEqualTo(3);
            assertThat(firstPage.page()).isZero();
            assertThat(firstPage.size()).isEqualTo(2);
            assertThat(firstPage.hasNext()).isTrue();
            assertThat(firstPage.contents())
                .extracting(ContentSummaryResponse::contentId)
                .containsExactly(thirdId, secondId);
            assertThat(secondPage.totalCount()).isEqualTo(3);
            assertThat(secondPage.page()).isEqualTo(1);
            assertThat(secondPage.hasNext()).isFalse();
            assertThat(secondPage.contents())
                .extracting(ContentSummaryResponse::contentId)
                .containsExactly(firstId);
        }

        @Test
        @DisplayName("size 를 3 으로 부르면 홈의 최근 저장 3건을 얻는다")
        void returnsThreeMostRecentContents_whenSizeIsThree() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-home");
            fixture.savedContentId(signup.accessToken(), List.of("BLOG"));
            Long secondId = fixture.savedContentId(signup.accessToken(), List.of("BLOG"));
            Long thirdId = fixture.savedContentId(signup.accessToken(), List.of("INSTAGRAM"));
            Long fourthId = fixture.savedContentId(signup.accessToken(), List.of("THREADS"));

            ContentListResponse home = fixture.contentList(signup.accessToken(), "?size=3");

            assertThat(home.totalCount()).isEqualTo(4);
            assertThat(home.hasNext()).isTrue();
            assertThat(home.contents())
                .extracting(ContentSummaryResponse::contentId)
                .containsExactly(fourthId, thirdId, secondId);
        }

        @Test
        @DisplayName("없는 채널로 거르면 400 과 C0001 을 반환한다")
        void returns400_whenChannelUnknown() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-bad-channel");

            fixture.getContents(signup.accessToken(), "?channel=FACEBOOK")
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("채널로 거르면 그 채널을 포함한 저장 건만 오고 카드의 채널 목록은 잘리지 않는다")
        void returnsOnlyContentsHavingChannelWithFullChannelList_whenChannelFiltered() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-filter");
            Long instagramId = fixture.savedContentId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));
            fixture.savedContentId(signup.accessToken(), List.of("THREADS"));

            ContentListResponse filtered = fixture.contentList(signup.accessToken(), "?channel=INSTAGRAM");

            assertThat(filtered.totalCount()).isEqualTo(1);
            assertThat(filtered.contents()).singleElement().satisfies(content -> {
                assertThat(content.contentId()).isEqualTo(instagramId);
                assertThat(content.channels()).containsExactly("BLOG", "INSTAGRAM");
            });
        }

        @Test
        @DisplayName("대표 채널에 제목이 있으면 저장된 제목이 목록 제목이 된다")
        void fillsTitleFromSavedTitle_whenRepresentativeChannelHasTitle() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-titled");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));
            fixture.savedContentId(signup.accessToken(), generationId);

            assertThat(fixture.contentList(signup.accessToken(), "").contents())
                .singleElement()
                .satisfies(content -> assertThat(content.title()).isEqualTo("테스트 제목"));
        }

        @Test
        @DisplayName("대표 채널에 제목이 없으면 사진 가이드 태그를 걷어낸 본문이 목록 제목이 된다")
        void fillsTitleFromBodyWithoutPhotoGuideTags_whenRepresentativeChannelHasNoTitle() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-untitled");
            Long generationId = fixture.photoGuidedGenerationId(signup.accessToken(), List.of("INSTAGRAM"));
            fixture.savedContentId(signup.accessToken(), generationId);

            assertThat(fixture.contentList(signup.accessToken(), "").contents())
                .singleElement()
                .satisfies(content -> assertThat(content.title())
                    .startsWith("테스트 본문 이어지는 본문 1")
                    .doesNotContain("<photo-guide", "\n"));
        }

        @Test
        @DisplayName("목록 제목이 20자를 넘으면 20자까지만 담고 말줄임표를 붙인다")
        void cutsTitleAtTwentyCharactersWithEllipsis() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-long-title");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("THREADS"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);
            fixture.editContentChannel(signup.accessToken(), saved.contentId(),
                    saved.contents().getFirst().contentChannelId(),
                    Map.of("body", "가".repeat(21), "hashtags", List.of()))
                .expectStatus().isOk();

            assertThat(fixture.contentList(signup.accessToken(), "").contents())
                .singleElement()
                .satisfies(content -> assertThat(content.title()).isEqualTo("가".repeat(20) + "…"));
        }

        @Test
        @DisplayName("목록 제목이 20자 이하면 말줄임표 없이 그대로 온다")
        void keepsTitleAsIs_whenNotLongerThanTwentyCharacters() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-short-title");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("THREADS"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);
            fixture.editContentChannel(signup.accessToken(), saved.contentId(),
                    saved.contents().getFirst().contentChannelId(),
                    Map.of("body", "가".repeat(20), "hashtags", List.of()))
                .expectStatus().isOk();

            assertThat(fixture.contentList(signup.accessToken(), "").contents())
                .singleElement()
                .satisfies(content -> assertThat(content.title()).isEqualTo("가".repeat(20)));
        }

        @Test
        @DisplayName("제목 있는 채널의 제목도 20자에서 잘린다")
        void cutsSavedTitleAtTwentyCharacters_whenRepresentativeChannelHasTitle() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-long-saved-title");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);
            fixture.editContentChannel(signup.accessToken(), saved.contentId(),
                    saved.contents().getFirst().contentChannelId(), Map.of(
                        "title", "나".repeat(25),
                        "body", "직접 고친 본문",
                        "hashtags", List.of()))
                .expectStatus().isOk();

            assertThat(fixture.contentList(signup.accessToken(), "").contents())
                .singleElement()
                .satisfies(content -> assertThat(content.title()).isEqualTo("나".repeat(20) + "…"));
        }

        @Test
        @DisplayName("해시태그는 앞 2개만 온다")
        void returnsAtMostTwoHashtags() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-hashtags");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);
            fixture.editContentChannel(signup.accessToken(), saved.contentId(),
                    saved.contents().getFirst().contentChannelId(), Map.of(
                        "title", "직접 고친 제목",
                        "body", "직접 고친 본문",
                        "hashtags", List.of("#첫째", "#둘째", "#셋째")))
                .expectStatus().isOk();

            assertThat(fixture.contentList(signup.accessToken(), "").contents())
                .singleElement()
                .satisfies(content -> assertThat(content.hashtags()).containsExactly("#첫째", "#둘째"));
        }

        @Test
        @DisplayName("스레드와 당근 비즈를 저장하면 고정 순서에서 앞선 당근 비즈가 채널 목록의 첫 채널이 된다")
        void listsLeadingChannelFirst_whenThreadsAndDaangnBizSaved() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-representative");
            Long generationId = fixture.startedGenerationId(signup.accessToken(),
                List.of("THREADS", "DAANGN_BIZ"));
            fixture.savedContentId(signup.accessToken(), generationId);

            assertThat(fixture.contentList(signup.accessToken(), "").contents())
                .singleElement()
                .satisfies(content -> assertThat(content.channels()).containsExactly("DAANGN_BIZ", "THREADS"));
        }

        @Test
        @DisplayName("첫 채널에 해시태그가 없으면 해시태그가 있는 다음 채널에서 가져온다")
        void returnsHashtagsOfFirstChannelHavingThem_whenLeadingChannelHasNone() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-hashtag-source");
            Long generationId = fixture.startedGenerationId(signup.accessToken(),
                List.of("THREADS", "DAANGN_BIZ"));
            fixture.savedContentId(signup.accessToken(), generationId);

            assertThat(fixture.contentList(signup.accessToken(), "").contents())
                .singleElement()
                .satisfies(content -> assertThat(content.hashtags()).containsExactly("#테스트", "#쏘쓰"));
        }

        @Test
        @DisplayName("어느 채널에도 해시태그가 없으면 빈 배열이 온다")
        void returnsNoHashtags_whenEveryChannelHasNone() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-hashtag-none");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("DAANGN_BIZ"));
            fixture.savedContentId(signup.accessToken(), generationId);

            assertThat(fixture.contentList(signup.accessToken(), "").contents())
                .singleElement()
                .satisfies(content -> assertThat(content.hashtags()).isEmpty());
        }

        @Test
        @DisplayName("카드에 저장할 때의 목적·톤과 저장 시각이 담긴다")
        void returnsPurposeToneAndSavedAt() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-condition");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), Map.of(
                "channels", List.of("BLOG"),
                "purpose", "EVENT_DISCOUNT",
                "tone", "EMOTIONAL",
                "emphasis", "테스트 강조 내용"));
            Long contentId = fixture.savedContentId(signup.accessToken(), generationId);

            assertThat(fixture.contentList(signup.accessToken(), "").contents())
                .singleElement()
                .satisfies(content -> {
                    assertThat(content.purpose()).isEqualTo("EVENT_DISCOUNT");
                    assertThat(content.tone()).isEqualTo("EMOTIONAL");
                    assertThat(content.savedAt())
                        .isEqualTo(contentRepository.findById(contentId).orElseThrow().getCreatedAt());
                });
        }

        @Test
        @DisplayName("삭제한 콘텐츠는 목록에서 빠지고 전체 건수에서도 빠진다")
        void excludesDeletedContentsFromListAndTotalCount() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-deleted");
            Long deletedId = fixture.savedContentId(signup.accessToken(), List.of("BLOG"));
            Long keptId = fixture.savedContentId(signup.accessToken(), List.of("THREADS"));
            fixture.deletedContent(signup.accessToken(), deletedId);

            ContentListResponse list = fixture.contentList(signup.accessToken(), "");

            assertThat(list.totalCount()).isEqualTo(1);
            assertThat(list.contents())
                .extracting(ContentSummaryResponse::contentId)
                .containsExactly(keptId);
        }

        @Test
        @DisplayName("다른 회원의 콘텐츠는 목록에 오지 않는다")
        void excludesOtherMembersContents() {
            SignupResponse owner = fixture.signupActiveMember("naver-list-owner");
            fixture.savedContentId(owner.accessToken(), List.of("BLOG"));
            SignupResponse other = fixture.signupActiveMember("naver-list-other");
            Long otherContentId = fixture.savedContentId(other.accessToken(), List.of("THREADS"));

            ContentListResponse list = fixture.contentList(other.accessToken(), "");

            assertThat(list.totalCount()).isEqualTo(1);
            assertThat(list.contents())
                .extracting(ContentSummaryResponse::contentId)
                .containsExactly(otherContentId);
        }

        @Test
        @DisplayName("저장한 콘텐츠가 없으면 빈 목록과 0 건이 온다")
        void returnsEmptyList_whenNothingSaved() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-empty");

            ContentListResponse list = fixture.contentList(signup.accessToken(), "");

            assertThat(list.totalCount()).isZero();
            assertThat(list.contents()).isEmpty();
        }

        @Test
        @DisplayName("채널로 걸러도 저장 시각 최신순과 페이징이 그대로 적용된다")
        void appliesOrderAndPagingToFilteredList() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-filter-paging");
            Long oldestId = fixture.savedContentId(signup.accessToken(), List.of("BLOG"));
            fixture.savedContentId(signup.accessToken(), List.of("THREADS"));
            Long middleId = fixture.savedContentId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));
            Long newestId = fixture.savedContentId(signup.accessToken(), List.of("BLOG"));

            ContentListResponse firstPage = fixture.contentList(signup.accessToken(), "?channel=BLOG&page=0&size=2");
            ContentListResponse secondPage = fixture.contentList(signup.accessToken(), "?channel=BLOG&page=1&size=2");

            assertThat(firstPage.totalCount()).isEqualTo(3);
            assertThat(firstPage.hasNext()).isTrue();
            assertThat(firstPage.contents())
                .extracting(ContentSummaryResponse::contentId)
                .containsExactly(newestId, middleId);
            assertThat(secondPage.totalCount()).isEqualTo(3);
            assertThat(secondPage.hasNext()).isFalse();
            assertThat(secondPage.contents())
                .extracting(ContentSummaryResponse::contentId)
                .containsExactly(oldestId);
        }

        @Test
        @DisplayName("채널로 걸러도 고른 정렬이 그대로 적용된다")
        void appliesChosenOrderToFilteredList() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-filter-oldest");
            Long oldestId = fixture.savedContentId(signup.accessToken(), List.of("BLOG"));
            fixture.savedContentId(signup.accessToken(), List.of("THREADS"));
            Long newestId = fixture.savedContentId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            ContentListResponse filtered = fixture.contentList(signup.accessToken(), "?channel=BLOG&sort=OLDEST");

            assertThat(filtered.totalCount()).isEqualTo(2);
            assertThat(filtered.contents())
                .extracting(ContentSummaryResponse::contentId)
                .containsExactly(oldestId, newestId);
        }

        @Test
        @DisplayName("채널로 걸러도 삭제한 콘텐츠는 목록과 전체 건수에서 빠진다")
        void excludesDeletedContentsFromFilteredList() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-filter-deleted");
            Long deletedId = fixture.savedContentId(signup.accessToken(), List.of("BLOG"));
            Long keptId = fixture.savedContentId(signup.accessToken(), List.of("BLOG"));
            fixture.deletedContent(signup.accessToken(), deletedId);

            ContentListResponse filtered = fixture.contentList(signup.accessToken(), "?channel=BLOG");

            assertThat(filtered.totalCount()).isEqualTo(1);
            assertThat(filtered.contents())
                .extracting(ContentSummaryResponse::contentId)
                .containsExactly(keptId);
        }

        @Test
        @DisplayName("채널 필터 결과가 없으면 빈 목록과 0 건이 온다")
        void returnsEmptyList_whenNoContentHasChannel() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-filter-empty");
            fixture.savedContentId(signup.accessToken(), List.of("BLOG"));

            ContentListResponse filtered = fixture.contentList(signup.accessToken(), "?channel=DAANGN_BIZ");

            assertThat(filtered.totalCount()).isZero();
            assertThat(filtered.contents()).isEmpty();
        }

        @Test
        @DisplayName("size 가 상한을 넘으면 400 과 C0001 을 반환한다")
        void returns400_whenSizeExceedsLimit() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-huge-size");

            fixture.getContents(signup.accessToken(), "?size=51")
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("없는 정렬 값으로 부르면 400 과 C0001 을 반환한다")
        void returns400_whenSortUnknown() {
            SignupResponse signup = fixture.signupActiveMember("naver-list-bad-sort");

            fixture.getContents(signup.accessToken(), "?sort=POPULAR")
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 목록을 조회하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            fixture.client().get().uri("/v1/contents")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 목록을 조회하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenRequests() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-list-pending");

            fixture.getContents(login.accessToken(), "")
                .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }
    }

    @Nested
    @DisplayName("GET /v1/contents/{contentId}")
    class GetById {

        @Test
        @DisplayName("저장한 콘텐츠를 조회하면 채널·제목·본문·해시태그가 저장된 그대로 온다")
        void returnsSavedChannelTitleBodyAndHashtags_whenContentRequested() {
            SignupResponse signup = fixture.signupActiveMember("naver-detail-copy");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            GenerationChannelResultResponse origin = fixture
                .generationDetail(signup.accessToken(), generationId).results().getFirst();
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);

            fixture.getContent(signup.accessToken(), saved.contentId())
                .expectStatus().isOk()
                .expectBody(ContentDetailResponse.class)
                .value(body -> {
                    assertThat(body.contentId()).isEqualTo(saved.contentId());
                    assertThat(body.contents()).singleElement().satisfies(content -> {
                        assertThat(content.contentChannelId())
                            .isEqualTo(saved.contents().getFirst().contentChannelId());
                        assertThat(content.channel()).isEqualTo("BLOG");
                        assertThat(content.title()).isEqualTo(origin.title());
                        assertThat(content.body()).isEqualTo(origin.body());
                        assertThat(content.hashtags()).isEqualTo(origin.hashtags());
                    });
                });
        }

        @Test
        @DisplayName("여러 채널을 저장한 콘텐츠를 조회하면 채널이 정해진 순서로 전부 온다")
        void returnsEveryChannelInChannelOrder_whenContentRequested() {
            SignupResponse signup = fixture.signupActiveMember("naver-detail-unit");
            Long generationId = fixture.startedGenerationId(signup.accessToken(),
                List.of("THREADS", "DAANGN_BIZ", "BLOG", "INSTAGRAM"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);

            fixture.getContent(signup.accessToken(), saved.contentId())
                .expectStatus().isOk()
                .expectBody(ContentDetailResponse.class)
                .value(body -> {
                    assertThat(body.contents())
                        .extracting(ContentChannelResponse::channel)
                        .containsExactly("BLOG", "INSTAGRAM", "DAANGN_BIZ", "THREADS");
                    assertThat(body.contents())
                        .extracting(ContentChannelResponse::contentChannelId)
                        .containsExactlyInAnyOrderElementsOf(saved.contents().stream()
                            .map(ContentChannelSummaryResponse::contentChannelId)
                            .toList());
                });
        }

        @Test
        @DisplayName("다른 콘텐츠의 채널은 섞이지 않는다")
        void excludesChannelsOfOtherContents_whenContentRequested() {
            SignupResponse signup = fixture.signupActiveMember("naver-detail-other-unit");
            Long firstGenerationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            ContentSaveResponse first = fixture.contentsOfGeneration(signup.accessToken(), firstGenerationId);
            Long secondGenerationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "THREADS"));
            fixture.contentsOfGeneration(signup.accessToken(), secondGenerationId);

            fixture.getContent(signup.accessToken(), first.contentId())
                .expectStatus().isOk()
                .expectBody(ContentDetailResponse.class)
                .value(body -> assertThat(body.contents())
                    .extracting(ContentChannelResponse::contentChannelId)
                    .containsExactly(first.contents().getFirst().contentChannelId()));
        }

        @Test
        @DisplayName("제목 없는 채널의 상세는 제목이 없는 채로 온다")
        void returnsNullTitle_whenChannelHasNoTitle() {
            SignupResponse signup = fixture.signupActiveMember("naver-detail-untitled");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("INSTAGRAM"));
            Long contentId = fixture.savedContentId(signup.accessToken(), generationId);

            fixture.getContent(signup.accessToken(), contentId)
                .expectStatus().isOk()
                .expectBody(ContentDetailResponse.class)
                .value(body -> assertThat(body.contents()).singleElement().satisfies(content -> {
                    assertThat(content.channel()).isEqualTo("INSTAGRAM");
                    assertThat(content.title()).isNull();
                    assertThat(content.body()).isNotBlank();
                }));
        }

        @Test
        @DisplayName("당근 비즈 채널의 상세는 해시태그가 빈 배열로 온다")
        void returnsEmptyHashtags_whenChannelIsDaangnBiz() {
            SignupResponse signup = fixture.signupActiveMember("naver-detail-daangn");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("DAANGN_BIZ"));
            Long contentId = fixture.savedContentId(signup.accessToken(), generationId);

            fixture.getContent(signup.accessToken(), contentId)
                .expectStatus().isOk()
                .expectBody(ContentDetailResponse.class)
                .value(body -> assertThat(body.contents()).singleElement().satisfies(content -> {
                    assertThat(content.channel()).isEqualTo("DAANGN_BIZ");
                    assertThat(content.hashtags()).isEmpty();
                    assertThat(content.body()).isNotBlank();
                }));
        }

        @Test
        @DisplayName("상세에 저장할 때 복사해 둔 목적·톤·키워드가 함께 온다")
        void returnsCopiedPurposeToneAndKeywords_whenContentRequested() {
            SignupResponse signup = fixture.signupActiveMember("naver-detail-condition");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), Map.of(
                "channels", List.of("BLOG"),
                "purpose", "EVENT_DISCOUNT",
                "tone", "EMOTIONAL",
                "emphasis", "테스트 강조 내용",
                "keywords", List.of("디저트", "크루아상", "을지로베이커리")));
            Long contentId = fixture.savedContentId(signup.accessToken(), generationId);

            fixture.getContent(signup.accessToken(), contentId)
                .expectStatus().isOk()
                .expectBody(ContentDetailResponse.class)
                .value(body -> {
                    assertThat(body.purpose()).isEqualTo("EVENT_DISCOUNT");
                    assertThat(body.tone()).isEqualTo("EMOTIONAL");
                    assertThat(body.keywords()).containsExactly("디저트", "크루아상", "을지로베이커리");
                });
        }

        @Test
        @DisplayName("키워드 없이 만든 콘텐츠의 상세는 키워드가 빈 배열이다")
        void returnsEmptyKeywords_whenGeneratedWithoutKeywords() {
            SignupResponse signup = fixture.signupActiveMember("naver-detail-no-keyword");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            Long contentId = fixture.savedContentId(signup.accessToken(), generationId);

            fixture.getContent(signup.accessToken(), contentId)
                .expectStatus().isOk()
                .expectBody(ContentDetailResponse.class)
                .value(body -> assertThat(body.keywords()).isEmpty());
        }

        @Test
        @DisplayName("없는 콘텐츠를 조회하면 404 와 CT0005 를 반환한다")
        void returns404_whenContentDoesNotExist() {
            SignupResponse signup = fixture.signupActiveMember("naver-detail-not-found");

            fixture.getContent(signup.accessToken(), 999_999L)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.CONTENT_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("다른 회원의 콘텐츠를 조회하면 404 와 CT0005 를 반환한다")
        void returns404_whenRequestingOtherMembersContent() {
            SignupResponse owner = fixture.signupActiveMember("naver-detail-owner");
            Long generationId = fixture.startedGenerationId(owner.accessToken(), List.of("BLOG"));
            Long contentId = fixture.savedContentId(owner.accessToken(), generationId);
            SignupResponse other = fixture.signupActiveMember("naver-detail-other");

            fixture.getContent(other.accessToken(), contentId)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.CONTENT_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 조회하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            fixture.client().get().uri("/v1/contents/1")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 조회하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenRequests() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-detail-pending");

            fixture.getContent(login.accessToken(), 1L)
                .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }
    }

    @Nested
    @DisplayName("PUT /v1/contents/{contentId}/channels/{contentChannelId}")
    class Edit {

        private static final Map<String, Object> TITLED_EDIT = Map.of(
            "title", "직접 고친 제목",
            "body", "직접 고친 본문",
            "hashtags", List.of("#직접고친태그"));

        @Test
        @DisplayName("편집하면 상세 조회에 편집한 값이 온다")
        void reflectsEditedValues_whenChannelEdited() {
            SignupResponse signup = fixture.signupActiveMember("naver-edit-reflect");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);
            Long contentChannelId = saved.contents().getFirst().contentChannelId();

            fixture.editContentChannel(signup.accessToken(), saved.contentId(), contentChannelId, TITLED_EDIT)
                .expectStatus().isOk()
                .expectBody(ContentChannelResponse.class)
                .value(body -> {
                    assertThat(body.contentChannelId()).isEqualTo(contentChannelId);
                    assertThat(body.channel()).isEqualTo("BLOG");
                    assertThat(body.title()).isEqualTo("직접 고친 제목");
                    assertThat(body.body()).isEqualTo("직접 고친 본문");
                    assertThat(body.hashtags()).containsExactly("#직접고친태그");
                });

            assertThat(fixture.contentDetail(signup.accessToken(), saved.contentId()).contents())
                .singleElement()
                .satisfies(content -> {
                    assertThat(content.title()).isEqualTo("직접 고친 제목");
                    assertThat(content.body()).isEqualTo("직접 고친 본문");
                    assertThat(content.hashtags()).containsExactly("#직접고친태그");
                });
        }

        @Test
        @DisplayName("제목 없는 채널은 제목 없이 편집된다")
        void editsWithoutTitle_whenChannelHasNoTitle() {
            SignupResponse signup = fixture.signupActiveMember("naver-edit-untitled");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("INSTAGRAM"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);

            fixture.editContentChannel(signup.accessToken(), saved.contentId(),
                    saved.contents().getFirst().contentChannelId(),
                    Map.of("body", "직접 고친 본문", "hashtags", List.of("#직접고친태그")))
                .expectStatus().isOk()
                .expectBody(ContentChannelResponse.class)
                .value(body -> {
                    assertThat(body.title()).isNull();
                    assertThat(body.body()).isEqualTo("직접 고친 본문");
                });
        }

        @Test
        @DisplayName("편집해도 원본 생성 결과 참조는 유지된다")
        void keepsGenerationResultId_whenChannelEdited() {
            SignupResponse signup = fixture.signupActiveMember("naver-edit-source");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);
            Long contentChannelId = saved.contents().getFirst().contentChannelId();
            Long generationResultId = database.channelById(contentChannelId).getGenerationResultId();

            fixture.editContentChannel(signup.accessToken(), saved.contentId(), contentChannelId, TITLED_EDIT)
                .expectStatus().isOk();

            assertThat(generationResultId).isNotNull();
            assertThat(database.channelById(contentChannelId).getGenerationResultId())
                .isEqualTo(generationResultId);
        }

        @Test
        @DisplayName("편집해도 저장 시각은 바뀌지 않고 수정 시각만 움직인다")
        void keepsSavedAtAndMovesUpdatedAt_whenChannelEdited() {
            SignupResponse signup = fixture.signupActiveMember("naver-edit-saved-at");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);
            Long contentChannelId = saved.contents().getFirst().contentChannelId();
            Instant savedAt = contentRepository.findById(saved.contentId()).orElseThrow().getCreatedAt();
            Instant updatedAt = database.channelById(contentChannelId).getUpdatedAt();
            clock.advanceBy(Duration.ofMinutes(10));

            fixture.editContentChannel(signup.accessToken(), saved.contentId(), contentChannelId, TITLED_EDIT)
                .expectStatus().isOk();

            assertThat(contentRepository.findById(saved.contentId()).orElseThrow().getCreatedAt())
                .isEqualTo(savedAt);
            assertThat(database.channelById(contentChannelId).getUpdatedAt()).isAfter(updatedAt);
        }

        @Test
        @DisplayName("한 채널만 편집하면 나머지 채널은 그대로다")
        void keepsOtherChannels_whenOneChannelEdited() {
            SignupResponse signup = fixture.signupActiveMember("naver-edit-one-channel");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "THREADS"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);
            ContentChannelResponse threads = fixture.contentDetail(signup.accessToken(), saved.contentId())
                .contents().getLast();

            fixture.editContentChannel(signup.accessToken(), saved.contentId(),
                    saved.contents().getFirst().contentChannelId(), TITLED_EDIT)
                .expectStatus().isOk();

            assertThat(fixture.contentDetail(signup.accessToken(), saved.contentId()).contents().getLast())
                .isEqualTo(threads);
        }

        @Test
        @DisplayName("편집하면 이전 값이 히스토리에 남는다")
        void writesPreviousValuesToHistory_whenChannelEdited() {
            SignupResponse signup = fixture.signupActiveMember("naver-edit-history");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);
            Long contentChannelId = saved.contents().getFirst().contentChannelId();
            ContentChannelResponse origin = fixture.contentDetail(signup.accessToken(), saved.contentId())
                .contents().getFirst();

            fixture.editContentChannel(signup.accessToken(), saved.contentId(), contentChannelId, TITLED_EDIT)
                .expectStatus().isOk();

            assertThat(database.historiesOf(contentChannelId))
                .singleElement()
                .satisfies(history -> {
                    assertThat(history.getTitle()).isEqualTo(origin.title());
                    assertThat(history.getBody()).isEqualTo(origin.body());
                    assertThat(history.getHashtags().values()).isEqualTo(origin.hashtags());
                    assertThat(history.getCreatedAt()).isNotNull();
                });
        }

        @Test
        @DisplayName("값이 그대로인 편집은 히스토리를 남기지 않고 수정 시각도 움직이지 않는다")
        void writesNoHistory_whenValuesUnchanged() {
            SignupResponse signup = fixture.signupActiveMember("naver-edit-unchanged");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);
            Long contentChannelId = saved.contents().getFirst().contentChannelId();
            ContentChannelResponse origin = fixture.contentDetail(signup.accessToken(), saved.contentId())
                .contents().getFirst();
            Instant updatedAt = database.channelById(contentChannelId).getUpdatedAt();
            clock.advanceBy(Duration.ofMinutes(10));

            fixture.editContentChannel(signup.accessToken(), saved.contentId(), contentChannelId, Map.of(
                    "title", origin.title(),
                    "body", origin.body(),
                    "hashtags", origin.hashtags()))
                .expectStatus().isOk();

            assertThat(database.historiesOf(contentChannelId))
                .isEmpty();
            assertThat(database.channelById(contentChannelId).getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("제목이 60자를 넘으면 400 을 반환한다")
        void returns400_whenTitleTooLong() {
            SignupResponse signup = fixture.signupActiveMember("naver-edit-long-title");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);

            fixture.editContentChannel(signup.accessToken(), saved.contentId(),
                    saved.contents().getFirst().contentChannelId(), Map.of(
                        "title", "가".repeat(61),
                        "body", "직접 고친 본문",
                        "hashtags", List.of()))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("해시태그가 20개를 넘으면 400 을 반환한다")
        void returns400_whenTooManyHashtags() {
            SignupResponse signup = fixture.signupActiveMember("naver-edit-many-hashtags");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);

            fixture.editContentChannel(signup.accessToken(), saved.contentId(),
                    saved.contents().getFirst().contentChannelId(), Map.of(
                        "title", "직접 고친 제목",
                        "body", "직접 고친 본문",
                        "hashtags", IntStream.rangeClosed(1, 21).mapToObj(index -> "#태그" + index).toList()))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("본문이 비어 있으면 400 을 반환한다")
        void returns400_whenBodyBlank() {
            SignupResponse signup = fixture.signupActiveMember("naver-edit-blank-body");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);

            fixture.editContentChannel(signup.accessToken(), saved.contentId(),
                    saved.contents().getFirst().contentChannelId(), Map.of(
                        "title", "직접 고친 제목",
                        "body", " ",
                        "hashtags", List.of()))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("제목 없는 채널에 제목을 보내면 400 과 CT0008 을 반환한다")
        void returns400_whenTitleSentToUntitledChannel() {
            SignupResponse signup = fixture.signupActiveMember("naver-edit-title-not-allowed");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("INSTAGRAM"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);

            fixture.editContentChannel(signup.accessToken(), saved.contentId(),
                    saved.contents().getFirst().contentChannelId(), TITLED_EDIT)
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.TITLE_NOT_ALLOWED.getCode()));
        }

        @Test
        @DisplayName("제목 없는 채널에 빈 제목을 보내면 제목 없음으로 편집된다")
        void treatsBlankTitleAsNoTitle_whenChannelHasNoTitle() {
            SignupResponse signup = fixture.signupActiveMember("naver-edit-blank-title");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("THREADS"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);

            fixture.editContentChannel(signup.accessToken(), saved.contentId(),
                    saved.contents().getFirst().contentChannelId(), Map.of(
                        "title", " ",
                        "body", "직접 고친 본문",
                        "hashtags", List.of("#직접고친태그")))
                .expectStatus().isOk()
                .expectBody(ContentChannelResponse.class)
                .value(body -> assertThat(body.title()).isNull());
        }

        @Test
        @DisplayName("제목 있는 채널에 제목을 보내지 않으면 400 과 CT0007 을 반환한다")
        void returns400_whenTitleMissingForTitledChannel() {
            SignupResponse signup = fixture.signupActiveMember("naver-edit-title-required");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);

            fixture.editContentChannel(signup.accessToken(), saved.contentId(),
                    saved.contents().getFirst().contentChannelId(),
                    Map.of("body", "직접 고친 본문", "hashtags", List.of()))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.TITLE_REQUIRED.getCode()));
        }

        @Test
        @DisplayName("없는 콘텐츠의 채널을 편집하면 404 와 CT0005 를 반환한다")
        void returns404_whenContentDoesNotExist() {
            SignupResponse signup = fixture.signupActiveMember("naver-edit-no-content");

            fixture.editContentChannel(signup.accessToken(), 999_999L, 999_999L, TITLED_EDIT)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.CONTENT_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("없는 채널별 콘텐츠를 편집하면 404 와 CT0006 을 반환한다")
        void returns404_whenContentChannelDoesNotExist() {
            SignupResponse signup = fixture.signupActiveMember("naver-edit-no-channel");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);

            fixture.editContentChannel(signup.accessToken(), saved.contentId(), 999_999L, TITLED_EDIT)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code())
                    .isEqualTo(ContentErrorCode.CONTENT_CHANNEL_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("다른 콘텐츠에 속한 채널을 편집하면 404 와 CT0006 을 반환한다")
        void returns404_whenChannelBelongsToAnotherContent() {
            SignupResponse signup = fixture.signupActiveMember("naver-edit-other-content");
            Long firstGenerationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            ContentSaveResponse first = fixture.contentsOfGeneration(signup.accessToken(), firstGenerationId);
            Long secondGenerationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            ContentSaveResponse second = fixture.contentsOfGeneration(signup.accessToken(), secondGenerationId);

            fixture.editContentChannel(signup.accessToken(), first.contentId(),
                    second.contents().getFirst().contentChannelId(), TITLED_EDIT)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code())
                    .isEqualTo(ContentErrorCode.CONTENT_CHANNEL_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("다른 회원의 콘텐츠를 편집하면 404 와 CT0005 를 반환하고 값도 그대로다")
        void returns404_whenEditingOtherMembersContent() {
            SignupResponse owner = fixture.signupActiveMember("naver-edit-owner");
            Long generationId = fixture.startedGenerationId(owner.accessToken(), List.of("BLOG"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(owner.accessToken(), generationId);
            Long contentChannelId = saved.contents().getFirst().contentChannelId();
            ContentChannelResponse origin = fixture.contentDetail(owner.accessToken(), saved.contentId())
                .contents().getFirst();
            SignupResponse other = fixture.signupActiveMember("naver-edit-other");

            fixture.editContentChannel(other.accessToken(), saved.contentId(), contentChannelId, TITLED_EDIT)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.CONTENT_NOT_FOUND.getCode()));

            assertThat(fixture.contentDetail(owner.accessToken(), saved.contentId()).contents().getFirst())
                .isEqualTo(origin);
            assertThat(database.historiesOf(contentChannelId))
                .isEmpty();
        }

        @Test
        @DisplayName("액세스 토큰 없이 편집하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            fixture.client().put().uri("/v1/contents/1/channels/1")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 편집하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenRequests() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-edit-pending");

            fixture.editContentChannel(login.accessToken(), 1L, 1L, TITLED_EDIT)
                .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }
    }

    @Nested
    @DisplayName("DELETE /v1/contents/{contentId}")
    class Delete {

        @Test
        @DisplayName("삭제하면 콘텐츠와 채널 전부에 같은 삭제 시각이 찍힌다")
        void stampsDeletedAtOnContentAndEveryChannel_whenContentDeleted() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-every-channel");
            Long generationId = fixture.startedGenerationId(signup.accessToken(),
                List.of("BLOG", "INSTAGRAM", "THREADS"));
            Long contentId = fixture.savedContentId(signup.accessToken(), generationId);

            fixture.deleteContent(signup.accessToken(), contentId)
                .expectStatus().isNoContent();

            Instant deletedAt = contentRepository.findById(contentId).orElseThrow().getDeletedAt();
            assertThat(deletedAt).isNotNull();
            assertThat(database.channelsOf(database.memberIdOf("naver-delete-every-channel"))).hasSize(3)
                .allSatisfy(channel -> assertThat(channel.getDeletedAt()).isEqualTo(deletedAt));
        }

        @Test
        @DisplayName("삭제한 콘텐츠를 조회하면 404 와 CT0005 를 반환한다")
        void returns404FromDetail_whenContentDeleted() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-detail");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            Long contentId = fixture.savedContentId(signup.accessToken(), generationId);
            fixture.deletedContent(signup.accessToken(), contentId);

            fixture.getContent(signup.accessToken(), contentId)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.CONTENT_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("이미 삭제한 콘텐츠를 다시 삭제하면 404 와 CT0005 를 반환한다")
        void returns404_whenDeletingAgain() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-twice");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            Long contentId = fixture.savedContentId(signup.accessToken(), generationId);
            fixture.deletedContent(signup.accessToken(), contentId);
            Instant deletedAt = database.channelsOf(database.memberIdOf("naver-delete-twice")).getFirst().getDeletedAt();
            clock.advanceBy(Duration.ofMinutes(10));

            fixture.deleteContent(signup.accessToken(), contentId)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.CONTENT_NOT_FOUND.getCode()));

            assertThat(database.channelsOf(database.memberIdOf("naver-delete-twice")))
                .allSatisfy(channel -> assertThat(channel.getDeletedAt()).isEqualTo(deletedAt));
        }

        @Test
        @DisplayName("삭제한 콘텐츠의 채널을 편집하면 404 와 CT0005 를 반환한다")
        void returns404FromEdit_whenContentDeleted() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-edit");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);
            fixture.deletedContent(signup.accessToken(), saved.contentId());

            fixture.editContentChannel(signup.accessToken(), saved.contentId(),
                    saved.contents().getFirst().contentChannelId(), Map.of(
                        "title", "직접 고친 제목",
                        "body", "직접 고친 본문",
                        "hashtags", List.of()))
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.CONTENT_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("한 콘텐츠를 삭제해도 다른 콘텐츠는 그대로 조회된다")
        void keepsOtherContents_whenOneContentDeleted() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-one");
            Long deletedGenerationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG"));
            Long deletedContentId = fixture.savedContentId(signup.accessToken(), deletedGenerationId);
            Long keptGenerationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "THREADS"));
            Long keptContentId = fixture.savedContentId(signup.accessToken(), keptGenerationId);

            fixture.deletedContent(signup.accessToken(), deletedContentId);

            assertThat(fixture.contentDetail(signup.accessToken(), keptContentId).contents())
                .extracting(ContentChannelResponse::channel)
                .containsExactly("BLOG", "THREADS");
        }

        @Test
        @DisplayName("없는 콘텐츠를 삭제하면 404 와 CT0005 를 반환한다")
        void returns404_whenContentDoesNotExist() {
            SignupResponse signup = fixture.signupActiveMember("naver-delete-not-found");

            fixture.deleteContent(signup.accessToken(), 999_999L)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.CONTENT_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("다른 회원의 콘텐츠를 삭제하면 404 와 CT0005 를 반환하고 콘텐츠는 그대로다")
        void returns404_whenDeletingOtherMembersContent() {
            SignupResponse owner = fixture.signupActiveMember("naver-delete-owner");
            Long generationId = fixture.startedGenerationId(owner.accessToken(), List.of("BLOG"));
            Long contentId = fixture.savedContentId(owner.accessToken(), generationId);
            SignupResponse other = fixture.signupActiveMember("naver-delete-other");

            fixture.deleteContent(other.accessToken(), contentId)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.CONTENT_NOT_FOUND.getCode()));

            assertThat(fixture.contentDetail(owner.accessToken(), contentId).contents()).hasSize(1);
            assertThat(database.channelsOf(database.memberIdOf("naver-delete-owner")))
                .allSatisfy(channel -> assertThat(channel.getDeletedAt()).isNull());
        }

        @Test
        @DisplayName("액세스 토큰 없이 삭제하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            fixture.client().delete().uri("/v1/contents/1")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 삭제하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenRequests() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-delete-pending");

            fixture.deleteContent(login.accessToken(), 1L)
                .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }
    }
}
