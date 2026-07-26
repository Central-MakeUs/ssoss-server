package com.ssoss.ssossbackend.content.entrypoint.controller;

import java.util.List;
import java.util.Map;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.content.domain.contract.ContentRepository;
import com.ssoss.ssossbackend.content.domain.contract.GenerationResultRepository;
import com.ssoss.ssossbackend.content.domain.model.Content;
import com.ssoss.ssossbackend.content.domain.model.ContentErrorCode;
import com.ssoss.ssossbackend.content.domain.model.GenerationResult;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentItemResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentSaveResponse;
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
    private GenerationResultRepository generationResultRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Nested
    @DisplayName("POST /v1/contents")
    class Save {

        @Test
        @DisplayName("여러 채널이 성공한 작업을 저장하면 채널마다 콘텐츠가 정해진 채널 순서로 생긴다")
        void savesOneContentPerSucceededChannelInChannelOrder_whenGenerationSaved() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-multi");
            Long generationId = fixture.startedGenerationId(signup.accessToken(),
                List.of("THREADS", "BLOG", "INSTAGRAM"));

            fixture.saveContents(signup.accessToken(), generationId)
                .expectStatus().isCreated()
                .expectBody(ContentSaveResponse.class)
                .value(body -> {
                    assertThat(body.contents()).hasSize(3)
                        .allSatisfy(content -> assertThat(content.contentId()).isNotNull());
                    assertThat(body.contents())
                        .extracting(ContentItemResponse::channel)
                        .containsExactly("BLOG", "INSTAGRAM", "THREADS");
                });

            assertThat(contentsOf(memberIdOf("naver-save-multi"))).hasSize(3);
        }

        @Test
        @DisplayName("콘텐츠는 원본 생성 결과의 채널·제목·본문·해시태그를 그대로 복사한다")
        void copiesChannelTitleBodyAndHashtags_whenGenerationSaved() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-copy");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));

            fixture.contentsOfGeneration(signup.accessToken(), generationId);

            List<GenerationResult> results = generationResultRepository
                .findAllByGenerationIdOrderById(generationId);
            assertThat(contentsOf(memberIdOf("naver-save-copy"))).hasSize(2).allSatisfy(content -> {
                GenerationResult origin = results.stream()
                    .filter(result -> result.getId().equals(content.getGenerationResultId()))
                    .findFirst()
                    .orElseThrow();
                assertThat(content.getGenerationId()).isEqualTo(generationId);
                assertThat(content.getChannel()).isEqualTo(origin.getChannel());
                assertThat(content.getTitle()).isEqualTo(origin.getTitle());
                assertThat(content.getBody()).isEqualTo(origin.getBody());
                assertThat(content.getHashtags()).isEqualTo(origin.getHashtags());
                assertThat(content.getDeletedAt()).isNull();
            });
        }

        @Test
        @DisplayName("같은 작업을 다시 저장해도 콘텐츠가 늘지 않고 기존이 그대로 반환된다")
        void returnsExistingContents_whenSavedAgain() {
            SignupResponse signup = fixture.signupActiveMember("naver-save-twice");
            Long generationId = fixture.startedGenerationId(signup.accessToken(), List.of("BLOG", "INSTAGRAM"));
            ContentSaveResponse first = fixture.contentsOfGeneration(signup.accessToken(), generationId);

            ContentSaveResponse second = fixture.contentsOfGeneration(signup.accessToken(), generationId);

            assertThat(second.contents())
                .extracting(ContentItemResponse::contentId)
                .containsExactlyElementsOf(first.contents().stream()
                    .map(ContentItemResponse::contentId)
                    .toList());
            assertThat(contentsOf(memberIdOf("naver-save-twice"))).hasSize(2);
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

    private List<Content> contentsOf(Long memberId) {
        return contentRepository.findAll().stream()
            .filter(content -> content.getMemberId().equals(memberId))
            .toList();
    }

    private Long memberIdOf(String socialId) {
        return memberRepository.findByProviderAndSocialId(NAVER, socialId).orElseThrow().getId();
    }
}
