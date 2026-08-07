package com.ssoss.ssossbackend.template.entrypoint.controller;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;
import com.ssoss.ssossbackend.template.domain.model.Channel;
import com.ssoss.ssossbackend.template.domain.model.SavedTemplate;
import com.ssoss.ssossbackend.template.domain.model.TemplateErrorCode;
import com.ssoss.ssossbackend.template.entrypoint.response.SavedTemplateSaveResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.TemplateResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("저장한 템플릿 API")
class SavedTemplateApiTest extends IntegrationTest {

    private static final long MISSING_TEMPLATE_ID = 999_999L;
    private static final String EDITED_BODY = """
        보니스커피에 새 메뉴가 출시되었습니다!

        🎁신메뉴: 흑임자 라떼
        💰가격: 6,500원""";
    private static final String LONGEST_BODY = "가".repeat(2000);
    private static final String TOO_LONG_BODY = "가".repeat(2001);

    @Nested
    @DisplayName("POST /v1/saved-templates")
    class SaveTemplate {

        @Test
        @DisplayName("저장하면 201 과 저장한 템플릿 id 를 반환한다")
        void returns201WithSavedTemplateId_whenSaved() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());

            SavedTemplateSaveResponse body = fixture.savedTemplate(signup.accessToken(), card.id(), EDITED_BODY);

            assertThat(body.savedTemplateId()).isNotNull();
        }

        @Test
        @DisplayName("요청에서 보낸 본문이 그대로 저장된다")
        void storesRequestedBodyAsIs_whenSaved() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-body");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());

            fixture.savedTemplate(signup.accessToken(), card.id(), EDITED_BODY);

            assertThat(database.savedTemplatesOf(database.memberIdOf("naver-saved-template-body")))
                .singleElement()
                .satisfies(saved -> assertThat(saved.getBody()).isEqualTo(EDITED_BODY));
        }

        @Test
        @DisplayName("제목과 설명과 분류와 추천 채널이 원본 템플릿에서 복사된다")
        void copiesTitleDescriptionCategoryAndRecommendedChannels_whenSaved() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-copy");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());

            fixture.savedTemplate(signup.accessToken(), card.id(), EDITED_BODY);

            assertThat(database.savedTemplatesOf(database.memberIdOf("naver-saved-template-copy")))
                .singleElement()
                .satisfies(saved -> {
                    assertThat(saved.getTemplateId()).isEqualTo(card.id());
                    assertThat(saved.getTitle()).isEqualTo(card.title());
                    assertThat(saved.getDescription()).isEqualTo(card.description());
                    assertThat(saved.getCategory().name()).isEqualTo(card.category());
                    assertThat(saved.getRecommendedChannels().values()).extracting(Channel::name)
                        .isEqualTo(card.recommendedChannels());
                });
        }

        @Test
        @DisplayName("원본 템플릿이 같아도 저장할 때마다 각각 남는다")
        void keepsEveryRow_whenSameTemplateSavedTwice() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-twice");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());

            SavedTemplateSaveResponse first = fixture.savedTemplate(signup.accessToken(), card.id(), EDITED_BODY);
            SavedTemplateSaveResponse second = fixture.savedTemplate(signup.accessToken(), card.id(), "다른 본문");

            assertThat(second.savedTemplateId()).isNotEqualTo(first.savedTemplateId());
            assertThat(database.savedTemplatesOf(database.memberIdOf("naver-saved-template-twice")))
                .hasSize(2)
                .extracting(SavedTemplate::getBody)
                .containsExactlyInAnyOrder(EDITED_BODY, "다른 본문");
        }

        @Test
        @DisplayName("같은 템플릿을 다른 회원이 저장해도 각자의 것만 남는다")
        void keepsSavedTemplatesPerMember_whenAnotherMemberSaves() {
            SignupResponse mine = fixture.signupActiveMember("naver-saved-template-mine");
            SignupResponse other = fixture.signupActiveMember("naver-saved-template-other");
            TemplateResponse card = fixture.firstTemplate(mine.accessToken());

            fixture.savedTemplate(mine.accessToken(), card.id(), EDITED_BODY);
            fixture.savedTemplate(other.accessToken(), card.id(), "다른 회원의 본문");

            assertThat(database.savedTemplatesOf(database.memberIdOf("naver-saved-template-mine")))
                .singleElement()
                .satisfies(saved -> assertThat(saved.getBody()).isEqualTo(EDITED_BODY));
            assertThat(database.savedTemplatesOf(database.memberIdOf("naver-saved-template-other")))
                .singleElement()
                .satisfies(saved -> assertThat(saved.getBody()).isEqualTo("다른 회원의 본문"));
        }

        @Test
        @DisplayName("본문이 2000자면 저장된다")
        void savesBody_whenBodyIsAtLengthLimit() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-longest");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());

            fixture.saveTemplate(signup.accessToken(), card.id(), LONGEST_BODY)
                .expectStatus().isCreated();
        }

        @Test
        @DisplayName("본문이 2000자를 넘으면 400 과 C0001 을 반환한다")
        void returns400_whenBodyExceedsLengthLimit() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-too-long");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());

            fixture.saveTemplate(signup.accessToken(), card.id(), TOO_LONG_BODY)
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("본문이 비면 400 과 C0001 을 반환한다")
        void returns400_whenBodyIsBlank() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-blank");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());

            fixture.saveTemplate(signup.accessToken(), card.id(), "")
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("템플릿 id 를 보내지 않으면 400 과 C0001 을 반환한다")
        void returns400_whenTemplateIdMissing() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-no-id");

            fixture.saveTemplate(signup.accessToken(), null, EDITED_BODY)
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("없는 템플릿을 저장하면 404 와 TP0001 을 반환한다")
        void returns404_whenTemplateIsMissing() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-missing");

            fixture.saveTemplate(signup.accessToken(), MISSING_TEMPLATE_ID, EDITED_BODY)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(TemplateErrorCode.TEMPLATE_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("저장해도 크레딧 잔액이 그대로다")
        void keepsCreditBalance_whenSaved() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-credit");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());
            int before = fixture.creditBalance(signup.accessToken()).balance();

            fixture.savedTemplate(signup.accessToken(), card.id(), EDITED_BODY);

            assertThat(fixture.creditBalance(signup.accessToken()).balance()).isEqualTo(before);
            assertThat(database.deductionsOf(database.memberIdOf("naver-saved-template-credit"))).isEmpty();
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 저장하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenSaves() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-seed");
            Long templateId = fixture.firstTemplate(signup.accessToken()).id();
            SocialLoginResponse login = fixture.naverLoginMember("naver-saved-template-pending");

            fixture.saveTemplate(login.accessToken(), templateId, EDITED_BODY)
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 저장하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            client().post().uri("/v1/saved-templates")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }
}
