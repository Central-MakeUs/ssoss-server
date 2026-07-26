package com.ssoss.ssossbackend.content.entrypoint.controller;

import java.util.List;
import java.util.Map;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.content.domain.contract.ContentChannelRepository;
import com.ssoss.ssossbackend.content.domain.contract.ContentRepository;
import com.ssoss.ssossbackend.content.domain.contract.GenerationResultRepository;
import com.ssoss.ssossbackend.content.domain.model.Content;
import com.ssoss.ssossbackend.content.domain.model.ContentChannel;
import com.ssoss.ssossbackend.content.domain.model.ContentErrorCode;
import com.ssoss.ssossbackend.content.domain.model.ContentSource;
import com.ssoss.ssossbackend.content.domain.model.GenerationResult;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentChannelResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentChannelSummaryResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentDetailResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentSaveResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.GenerationChannelResultResponse;
import com.ssoss.ssossbackend.member.domain.contract.MemberRepository;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static com.ssoss.ssossbackend.member.domain.model.SocialProvider.NAVER;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("콘텐츠 API")
class ContentApiTest extends IntegrationTest {

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private ContentChannelRepository contentChannelRepository;

    @Autowired
    private GenerationResultRepository generationResultRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Nested
    @DisplayName("POST /v1/contents")
    class Save {

        @Test
        @DisplayName("여러 채널이 성공한 작업을 저장하면 콘텐츠 1건에 채널이 정해진 순서로 담긴다")
        void savesOneContentWithEveryChannelInChannelOrder_whenGenerationSaved() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-multi");
            Long generationId = fixture.startedGenerationId(signup.accessToken(),
                List.of("THREADS", "BLOG", "INSTAGRAM"));

            fixture.saveContents(signup.accessToken(), generationId)
                .expectStatus().isCreated()
                .expectBody(ContentSaveResponse.class)
                .value(body -> {
                    assertThat(body.contentId()).isNotNull();
                    assertThat(body.contents()).hasSize(3)
                        .allSatisfy(content -> assertThat(content.contentChannelId()).isNotNull());
                    assertThat(body.contents())
                        .extracting(ContentChannelSummaryResponse::channel)
                        .containsExactly("BLOG", "INSTAGRAM", "THREADS");
                });

            assertThat(contentsOf(memberIdOf("naver-save-multi"))).hasSize(1);
            assertThat(channelsOf(memberIdOf("naver-save-multi"))).hasSize(3);
        }

        @Test
        @DisplayName("채널별 콘텐츠는 원본 생성 결과의 채널·제목·본문·해시태그를 그대로 복사한다")
        void copiesChannelTitleBodyAndHashtags_whenGenerationSaved() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-copy");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            fixture.contentsOfGeneration(signup.accessToken(), generationId);

            List<GenerationResult> results = generationResultRepository
                .findAllByGenerationIdOrderById(generationId);
            assertThat(channelsOf(memberIdOf("naver-save-copy"))).hasSize(2).allSatisfy(channel -> {
                GenerationResult origin = results.stream()
                    .filter(result -> result.getId().equals(channel.getSourceGenerationResultId()))
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
        @DisplayName("콘텐츠는 원본이 생성 작업임을 남기고 목적·톤·키워드를 복사한다")
        void copiesSourceAndGenerationConditions_whenGenerationSaved() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-condition");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), Map.of(
                "channels", List.of("BLOG"),
                "purpose", "EVENT_DISCOUNT",
                "tone", "EMOTIONAL",
                "emphasis", "테스트 강조 내용",
                "keywords", List.of("디저트", "크루아상")));

            fixture.contentsOfGeneration(signup.accessToken(), generationId);

            assertThat(contentsOf(memberIdOf("naver-save-condition"))).singleElement().satisfies(content -> {
                assertThat(content.getSourceType()).isEqualTo(ContentSource.GENERATION);
                assertThat(content.getSourceId()).isEqualTo(generationId);
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
            assertThat(contentsOf(memberIdOf("naver-save-twice"))).hasSize(1);
            assertThat(channelsOf(memberIdOf("naver-save-twice"))).hasSize(2);
        }

        @Test
        @DisplayName("채널 하나가 실패한 작업을 저장하면 400 과 CT0003 을 반환한다")
        void returns400_whenOneChannelFailed() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-one-fail");
            llmApi.stubEmptyBodyForUntitled();
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            fixture.saveContents(signup.accessToken(), generationId)
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.GENERATION_FAILED.getCode()));

            assertThat(contentsOf(memberIdOf("naver-save-one-fail"))).isEmpty();
        }

        @Test
        @DisplayName("전 채널이 실패한 작업을 저장하면 400 과 CT0003 을 반환한다")
        void returns400_whenAllChannelsFailed() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-all-fail");
            llmApi.stubFailure(429);
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            fixture.saveContents(signup.accessToken(), generationId)
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.GENERATION_FAILED.getCode()));

            assertThat(contentsOf(memberIdOf("naver-save-all-fail"))).isEmpty();
        }

        @Test
        @DisplayName("아직 진행 중인 작업을 저장하면 409 와 CT0004 를 반환한다")
        void returns409_whenGenerationStillInProgress() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-in-progress");
            taskExecutor.hold();
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            fixture.saveContents(signup.accessToken(), generationId)
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code())
                    .isEqualTo(ContentErrorCode.GENERATION_NOT_FINISHED.getCode()));

            assertThat(contentsOf(memberIdOf("naver-save-in-progress"))).isEmpty();
        }

        @Test
        @DisplayName("없는 작업을 저장하면 404 와 CT0002 를 반환한다")
        void returns404_whenGenerationDoesNotExist() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-not-found");

            fixture.saveContents(signup.accessToken(), 999_999L)
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

            fixture.saveContents(other.accessToken(), generationId)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(ContentErrorCode.GENERATION_NOT_FOUND.getCode()));

            assertThat(contentsOf(memberIdOf("naver-save-other"))).isEmpty();
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

            fixture.saveContents(login.accessToken(), 1L)
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
                List.of("THREADS", "BLOG", "INSTAGRAM"));
            ContentSaveResponse saved = fixture.contentsOfGeneration(signup.accessToken(), generationId);

            fixture.getContent(signup.accessToken(), saved.contentId())
                .expectStatus().isOk()
                .expectBody(ContentDetailResponse.class)
                .value(body -> {
                    assertThat(body.contents())
                        .extracting(ContentChannelResponse::channel)
                        .containsExactly("BLOG", "INSTAGRAM", "THREADS");
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

    private List<Content> contentsOf(Long memberId) {
        return contentRepository.findAll().stream()
            .filter(content -> content.getMemberId().equals(memberId))
            .toList();
    }

    private List<ContentChannel> channelsOf(Long memberId) {
        return contentsOf(memberId).stream()
            .flatMap(content -> contentChannelRepository.findAllByContentId(content.getId()).stream())
            .toList();
    }

    private Long memberIdOf(String socialId) {
        return memberRepository.findByProviderAndSocialId(NAVER, socialId).orElseThrow().getId();
    }
}
