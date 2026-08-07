package com.ssoss.ssossbackend.template.entrypoint.controller;

import java.util.Comparator;
import java.util.List;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;
import com.ssoss.ssossbackend.template.entrypoint.response.TemplateListResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.TemplateResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("추천 템플릿 목록 API")
class TemplateApiTest extends IntegrationTest {

    private static final String EVERY_TEMPLATE = "?size=50";
    private static final String BY_CATEGORY = "?category={category}";
    private static final String EVERY_MATCHED_TEMPLATE = BY_CATEGORY + "&size=50";
    private static final List<String> CATEGORIES = List.of("NEW_MENU", "EVENT", "STORE_INTRO", "NOTICE");

    @Nested
    @DisplayName("GET /v1/templates")
    class ListTemplates {

        @Test
        @DisplayName("가입 회원이 조회하면 심어 둔 템플릿이 id 역순으로 담긴다")
        void listsSeededTemplatesInIdDescendingOrder_whenActiveMemberQueries() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-list");

            TemplateListResponse body = fixture.templateList(signup.accessToken(), EVERY_TEMPLATE);

            assertThat(body.page()).isZero();
            assertThat(body.totalCount()).isGreaterThanOrEqualTo(8);
            assertThat(body.templates()).extracting(TemplateResponse::id)
                .isSortedAccordingTo(Comparator.reverseOrder());
            assertThat(body.templates()).extracting(TemplateResponse::title)
                .contains("신메뉴 출시 안내", "오픈 기념 할인 안내", "가게 첫인사", "휴무 안내");
        }

        @Test
        @DisplayName("카드마다 분류와 제목과 설명과 추천 채널이 담긴다")
        void carriesCategoryTitleDescriptionAndRecommendedChannels_whenActiveMemberQueries() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-card");

            TemplateListResponse body = fixture.templateList(signup.accessToken(), EVERY_TEMPLATE);

            assertThat(body.templates()).allSatisfy(template -> {
                assertThat(template.id()).isNotNull();
                assertThat(template.category()).isIn(CATEGORIES);
                assertThat(template.title()).isNotBlank();
                assertThat(template.description()).isNotBlank();
                assertThat(template.recommendedChannels()).isNotEmpty();
            });
            assertThat(body.templates())
                .filteredOn(template -> template.title().equals("신메뉴 출시 안내"))
                .singleElement()
                .satisfies(template -> {
                    assertThat(template.category()).isEqualTo("NEW_MENU");
                    assertThat(template.recommendedChannels()).containsExactly("INSTAGRAM", "BLOG", "THREADS");
                });
        }

        @Test
        @DisplayName("카드마다 북마크 여부가 거짓으로 담긴다")
        void marksEveryTemplateUnbookmarked_whenActiveMemberQueries() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-bookmark");

            TemplateListResponse body = fixture.templateList(signup.accessToken(), EVERY_TEMPLATE);

            assertThat(body.templates()).isNotEmpty();
            assertThat(body.templates()).extracting(TemplateResponse::bookmarked).containsOnly(false);
        }

        @Test
        @DisplayName("본문과 예시 본문은 카드에 담기지 않는다")
        void omitsBodyAndExampleBody_whenActiveMemberQueries() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-no-body");

            fixture.getTemplates(signup.accessToken(), EVERY_TEMPLATE)
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(json -> assertThat(json).doesNotContain("\"body\"", "\"exampleBody\""));
        }

        @Test
        @DisplayName("분류를 주면 그 분류의 템플릿만 담긴다")
        void keepsOnlyMatchingCategory_whenCategoryGiven() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-category");

            TemplateListResponse body =
                fixture.templateList(signup.accessToken(), EVERY_MATCHED_TEMPLATE, "NOTICE");

            assertThat(body.templates()).isNotEmpty();
            assertThat(body.templates()).extracting(TemplateResponse::category).containsOnly("NOTICE");
            assertThat(body.totalCount()).isEqualTo(body.templates().size());
            assertThat(body.templates()).extracting(TemplateResponse::id)
                .isSortedAccordingTo(Comparator.reverseOrder());
        }

        @Test
        @DisplayName("분류를 생략하면 모든 분류가 함께 담긴다")
        void keepsEveryCategory_whenCategoryOmitted() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-every-category");

            TemplateListResponse body = fixture.templateList(signup.accessToken(), EVERY_TEMPLATE);

            assertThat(body.templates()).extracting(TemplateResponse::category)
                .containsAll(CATEGORIES);
        }

        @Test
        @DisplayName("분류별 건수를 모두 더하면 전체 건수와 같다")
        void sumsUpToTotalCount_whenEveryCategoryQueried() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-category-sum");
            long all = fixture.templateList(signup.accessToken(), EVERY_TEMPLATE).totalCount();

            long summed = CATEGORIES.stream()
                .mapToLong(category ->
                    fixture.templateList(signup.accessToken(), EVERY_MATCHED_TEMPLATE, category).totalCount())
                .sum();

            assertThat(summed).isEqualTo(all);
        }

        @Test
        @DisplayName("어느 분류에도 없는 값을 주면 400 과 C0001 을 반환한다")
        void returns400_whenCategoryIsUnknown() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-category-unknown");

            fixture.getTemplates(signup.accessToken(), BY_CATEGORY, "없는분류")
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("size 를 1 로 부르면 한 건만 담기고 다음 페이지가 있다고 알려준다")
        void returnsSingleTemplateWithNextPage_whenSizeIsOne() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-size");

            TemplateListResponse body = fixture.templateList(signup.accessToken(), "?size=1");

            assertThat(body.size()).isEqualTo(1);
            assertThat(body.templates()).hasSize(1);
            assertThat(body.hasNext()).isTrue();
        }

        @Test
        @DisplayName("페이지를 넘기면 그다음 템플릿이 담긴다")
        void returnsFollowingTemplate_whenNextPageRequested() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-page");
            TemplateListResponse first = fixture.templateList(signup.accessToken(), "?size=1");

            TemplateListResponse second = fixture.templateList(signup.accessToken(), "?size=1&page=1");

            assertThat(second.page()).isEqualTo(1);
            assertThat(second.templates()).singleElement().satisfies(template ->
                assertThat(template.id()).isLessThan(first.templates().getFirst().id()));
        }

        @Test
        @DisplayName("마지막 페이지에서는 다음 페이지가 없다고 알려준다")
        void reportsNoNextPage_whenLastPageRequested() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-last-page");
            long totalCount = fixture.templateList(signup.accessToken(), "?size=1").totalCount();

            TemplateListResponse last =
                fixture.templateList(signup.accessToken(), "?size=1&page=" + (totalCount - 1));

            assertThat(last.hasNext()).isFalse();
            assertThat(last.templates()).hasSize(1);
        }

        @Test
        @DisplayName("분류를 건 목록에도 페이징이 그대로 동작한다")
        void pagesThroughMatchedTemplates_whenCategoryGiven() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-category-paged");
            List<Long> matchedIds =
                fixture.templateList(signup.accessToken(), EVERY_MATCHED_TEMPLATE, "EVENT").templates().stream()
                    .map(TemplateResponse::id)
                    .toList();

            TemplateListResponse first =
                fixture.templateList(signup.accessToken(), BY_CATEGORY + "&size=1", "EVENT");
            TemplateListResponse second =
                fixture.templateList(signup.accessToken(), BY_CATEGORY + "&size=1&page=1", "EVENT");

            assertThat(first.totalCount()).isEqualTo(matchedIds.size());
            assertThat(first.hasNext()).isTrue();
            assertThat(first.templates()).singleElement()
                .satisfies(template -> assertThat(template.id()).isEqualTo(matchedIds.getFirst()));
            assertThat(second.totalCount()).isEqualTo(matchedIds.size());
            assertThat(second.templates()).singleElement()
                .satisfies(template -> assertThat(template.id()).isEqualTo(matchedIds.get(1)));
        }

        @Test
        @DisplayName("size 가 50 을 넘으면 400 과 C0001 을 반환한다")
        void returns400_whenSizeExceedsLimit() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-size-limit");

            fixture.getTemplates(signup.accessToken(), "?size=51")
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("page 가 음수면 400 과 C0001 을 반환한다")
        void returns400_whenPageIsNegative() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-page-negative");

            fixture.getTemplates(signup.accessToken(), "?page=-1")
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 조회하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenQueries() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-template-pending");

            fixture.getTemplates(login.accessToken(), "")
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 조회하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            client().get().uri("/v1/templates")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }
}
