package com.ssoss.ssossbackend.template.entrypoint.controller;

import java.util.List;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;
import com.ssoss.ssossbackend.template.domain.model.TemplateErrorCode;
import com.ssoss.ssossbackend.template.entrypoint.response.TemplateDetailResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.TemplateResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("추천 템플릿 북마크 API")
class TemplateBookmarkApiTest extends IntegrationTest {

    private static final String EVERY_TEMPLATE = "?size=50";
    private static final long MISSING_TEMPLATE_ID = 999_999L;
    private static final long ANY_TEMPLATE_ID = 1L;

    private TemplateResponse cardOf(String accessToken, Long templateId) {
        return fixture.templateList(accessToken, EVERY_TEMPLATE).templates().stream()
            .filter(template -> template.id().equals(templateId))
            .findFirst()
            .orElseThrow();
    }

    @Nested
    @DisplayName("PUT /v1/members/me/templates/{templateId}")
    class BookmarkTemplate {

        @Test
        @DisplayName("가입 회원이 북마크하면 204 를 반환하고 그 카드의 북마크 여부가 참이 된다")
        void marksCardBookmarked_whenActiveMemberBookmarks() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-bookmark-on");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());

            fixture.bookmarkTemplate(signup.accessToken(), card.id())
                .expectStatus().isNoContent();

            assertThat(cardOf(signup.accessToken(), card.id()).bookmarked()).isTrue();
        }

        @Test
        @DisplayName("이미 북마크한 템플릿을 또 북마크해도 204 를 반환하고 행이 하나만 남는다")
        void keepsSingleRow_whenBookmarkedTwice() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-bookmark-twice");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());
            fixture.bookmarkedTemplate(signup.accessToken(), card.id());

            fixture.bookmarkTemplate(signup.accessToken(), card.id())
                .expectStatus().isNoContent();

            assertThat(database.templateBookmarksOf(database.memberIdOf("naver-template-bookmark-twice")))
                .singleElement()
                .satisfies(bookmark -> assertThat(bookmark.getTemplateId()).isEqualTo(card.id()));
            assertThat(cardOf(signup.accessToken(), card.id()).bookmarked()).isTrue();
        }

        @Test
        @DisplayName("북마크한 템플릿은 상세 조회에도 북마크 여부가 참으로 담긴다")
        void marksDetailBookmarked_whenActiveMemberBookmarks() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-bookmark-detail");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());
            fixture.bookmarkedTemplate(signup.accessToken(), card.id());

            TemplateDetailResponse body = fixture.templateDetail(signup.accessToken(), card.id());

            assertThat(body.bookmarked()).isTrue();
        }

        @Test
        @DisplayName("여러 템플릿을 북마크하면 목록에서 그 카드들만 북마크 여부가 참이 된다")
        void marksEveryBookmarkedCard_whenSeveralBookmarked() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-bookmark-several");
            List<TemplateResponse> templates = fixture.templateList(signup.accessToken(), EVERY_TEMPLATE).templates();
            Long first = templates.get(0).id();
            Long second = templates.get(1).id();

            fixture.bookmarkedTemplate(signup.accessToken(), first);
            fixture.bookmarkedTemplate(signup.accessToken(), second);

            assertThat(fixture.templateList(signup.accessToken(), EVERY_TEMPLATE).templates())
                .filteredOn(TemplateResponse::bookmarked)
                .extracting(TemplateResponse::id)
                .containsExactlyInAnyOrder(first, second);
        }

        @Test
        @DisplayName("북마크하지 않은 템플릿은 북마크 여부가 거짓으로 남는다")
        void leavesOtherCardsUnbookmarked_whenOneTemplateBookmarked() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-bookmark-others");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());

            fixture.bookmarkedTemplate(signup.accessToken(), card.id());

            assertThat(fixture.templateList(signup.accessToken(), EVERY_TEMPLATE).templates())
                .filteredOn(template -> !template.id().equals(card.id()))
                .isNotEmpty()
                .extracting(TemplateResponse::bookmarked)
                .containsOnly(false);
        }

        @Test
        @DisplayName("다른 회원이 북마크한 템플릿은 내 조회에서 거짓이다")
        void keepsMyCardUnbookmarked_whenOtherMemberBookmarked() {
            SignupResponse other = fixture.signupActiveMember("naver-template-bookmark-other");
            SignupResponse mine = fixture.signupActiveMember("naver-template-bookmark-mine");
            TemplateResponse card = fixture.firstTemplate(other.accessToken());
            fixture.bookmarkedTemplate(other.accessToken(), card.id());

            assertThat(cardOf(mine.accessToken(), card.id()).bookmarked()).isFalse();
            assertThat(cardOf(other.accessToken(), card.id()).bookmarked()).isTrue();
        }

        @Test
        @DisplayName("없는 템플릿을 북마크하면 404 와 TP0001 을 반환한다")
        void returns404_whenTemplateMissing() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-bookmark-missing");

            fixture.bookmarkTemplate(signup.accessToken(), MISSING_TEMPLATE_ID)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code())
                    .isEqualTo(TemplateErrorCode.TEMPLATE_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 북마크하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenBookmarks() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-template-bookmark-pending");

            fixture.bookmarkTemplate(login.accessToken(), ANY_TEMPLATE_ID)
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 북마크하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            client().put().uri("/v1/members/me/templates/" + ANY_TEMPLATE_ID)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }
}
