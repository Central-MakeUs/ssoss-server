package com.ssoss.ssossbackend.template.entrypoint.controller;

import java.util.Comparator;
import java.util.List;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;
import com.ssoss.ssossbackend.template.domain.model.TemplateErrorCode;
import com.ssoss.ssossbackend.template.entrypoint.response.TemplateAppliedResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.TemplateDetailResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.TemplateListResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.TemplateResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("추천 템플릿 API")
class TemplateApiTest extends IntegrationTest {

    private static final String EVERY_TEMPLATE = "?size=50";
    private static final String BY_CATEGORY = "?category={category}";
    private static final String EVERY_MATCHED_TEMPLATE = BY_CATEGORY + "&size=50";
    private static final List<String> CATEGORIES = List.of("NEW_MENU", "EVENT", "STORE_INTRO", "NOTICE");
    private static final String NEW_MENU_TITLE = "신메뉴 출시 안내";
    private static final long MISSING_TEMPLATE_ID = 999_999L;
    private static final long ANY_TEMPLATE_ID = 1L;
    private static final String STORE_NAME_MARK = "[가게명]";
    private static final String ADDRESS_MARK = "[주소]";
    private static final String BUSINESS_HOURS_MARK = "[영업시간]";
    private static final String PHONE_MARK = "[전화번호]";
    private static final String STORE_NAME = "보니스커피";
    private static final String STORE_ADDRESS = "서울 중구 을지로 100";
    private static final List<String> WEDNESDAY_TO_SUNDAY =
        List.of("WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");
    private static final String BUSINESS_HOURS = "수, 목, 금, 토, 일 오전 9:00 ~ 오후 8:00";

    private TemplateResponse cardOf(String accessToken, String title) {
        return fixture.templateList(accessToken, EVERY_TEMPLATE).templates().stream()
            .filter(template -> template.title().equals(title))
            .findFirst()
            .orElseThrow();
    }

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
                .contains(NEW_MENU_TITLE, "오픈 기념 할인 안내", "가게 첫인사", "휴무 안내");
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
                .filteredOn(template -> template.title().equals(NEW_MENU_TITLE))
                .singleElement()
                .satisfies(template -> {
                    assertThat(template.category()).isEqualTo("NEW_MENU");
                    assertThat(template.recommendedChannels()).containsExactly("INSTAGRAM", "BLOG", "THREADS");
                });
        }

        @Test
        @DisplayName("아무것도 북마크하지 않은 회원은 카드마다 북마크 여부가 거짓으로 담긴다")
        void marksEveryTemplateUnbookmarked_whenNothingBookmarked() {
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

    @Nested
    @DisplayName("GET /v1/templates/{templateId}")
    class GetTemplate {

        @Test
        @DisplayName("가입 회원이 조회하면 카드 필드에 원문과 예시 본문이 더해져 담긴다")
        void carriesBodyAndExampleBodyOnTopOfCardFields_whenActiveMemberQueries() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-detail");
            TemplateResponse card = cardOf(signup.accessToken(), NEW_MENU_TITLE);

            TemplateDetailResponse body = fixture.templateDetail(signup.accessToken(), card.id());

            assertThat(body.id()).isEqualTo(card.id());
            assertThat(body.category()).isEqualTo(card.category());
            assertThat(body.title()).isEqualTo(card.title());
            assertThat(body.description()).isEqualTo(card.description());
            assertThat(body.recommendedChannels()).isEqualTo(card.recommendedChannels());
            assertThat(body.bookmarked()).isFalse();
            assertThat(body.body()).isNotBlank();
            assertThat(body.exampleBody()).isNotBlank().isNotEqualTo(body.body());
        }

        @Test
        @DisplayName("없는 템플릿을 조회하면 404 와 TP0001 을 반환한다")
        void returns404_whenTemplateIsMissing() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-detail-missing");

            fixture.getTemplate(signup.accessToken(), MISSING_TEMPLATE_ID)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code())
                    .isEqualTo(TemplateErrorCode.TEMPLATE_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("있는 템플릿이라도 가입 대기(PENDING) 토큰으로 조회하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenQueries() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-detail-seed");
            Long templateId = cardOf(signup.accessToken(), NEW_MENU_TITLE).id();
            SocialLoginResponse login = fixture.naverLoginMember("naver-template-detail-pending");

            fixture.getTemplate(login.accessToken(), templateId)
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 조회하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            client().get().uri("/v1/templates/" + ANY_TEMPLATE_ID)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }

    @Nested
    @DisplayName("GET /v1/templates/{templateId}/applied")
    class ApplyTemplate {

        private void writeBasicInfo(String accessToken) {
            fixture.savedStoreBasicInfo(accessToken,
                fixture.storeBasicInfoBody(STORE_NAME, "CAFE", STORE_ADDRESS, null));
        }

        private void writeOperationInfo(String accessToken, String openTime, String closeTime) {
            fixture.savedStoreOperationInfo(accessToken,
                fixture.storeOperationInfoBody(WEDNESDAY_TO_SUNDAY, openTime, closeTime, List.of(),
                    false, false, false));
        }

        private String applyNewMenuTemplate(String accessToken) {
            return fixture.appliedTemplate(accessToken, cardOf(accessToken, NEW_MENU_TITLE).id()).body();
        }

        @Test
        @DisplayName("매장 정보를 채운 회원이 부르면 매장명과 주소와 영업시간이 본문에 담긴다")
        void fillsStoreNameAddressAndBusinessHours_whenStoreInfoWritten() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-applied");
            writeBasicInfo(signup.accessToken());
            writeOperationInfo(signup.accessToken(), "09:00", "20:00");

            String body = applyNewMenuTemplate(signup.accessToken());

            assertThat(body).contains(STORE_NAME, STORE_ADDRESS, BUSINESS_HOURS);
            assertThat(body).doesNotContain(STORE_NAME_MARK, ADDRESS_MARK, BUSINESS_HOURS_MARK);
        }

        @Test
        @DisplayName("적용한 본문에 원본 템플릿 id 가 함께 담긴다")
        void carriesTemplateId_whenApplied() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-applied-id");
            TemplateResponse card = cardOf(signup.accessToken(), NEW_MENU_TITLE);

            TemplateAppliedResponse body = fixture.appliedTemplate(signup.accessToken(), card.id());

            assertThat(body.id()).isEqualTo(card.id());
        }

        @Test
        @DisplayName("매장 정보를 하나도 채우지 않은 회원이 불러도 자리표시자가 그대로 남는다")
        void keepsEveryPlaceholder_whenNoStoreInfoWritten() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-applied-empty");

            String body = applyNewMenuTemplate(signup.accessToken());

            assertThat(body).contains(STORE_NAME_MARK, ADDRESS_MARK, BUSINESS_HOURS_MARK);
        }

        @Test
        @DisplayName("기본 정보만 채운 회원은 매장명과 주소만 채워진다")
        void fillsBasicInfoOnly_whenOperationInfoMissing() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-applied-basic-only");
            writeBasicInfo(signup.accessToken());

            String body = applyNewMenuTemplate(signup.accessToken());

            assertThat(body).contains(STORE_NAME, STORE_ADDRESS, BUSINESS_HOURS_MARK);
            assertThat(body).doesNotContain(STORE_NAME_MARK, ADDRESS_MARK);
        }

        @Test
        @DisplayName("운영 정보만 채운 회원은 영업시간만 채워진다")
        void fillsBusinessHoursOnly_whenBasicInfoMissing() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-applied-operation-only");
            writeOperationInfo(signup.accessToken(), "09:00", "20:00");

            String body = applyNewMenuTemplate(signup.accessToken());

            assertThat(body).contains(BUSINESS_HOURS, STORE_NAME_MARK, ADDRESS_MARK);
            assertThat(body).doesNotContain(BUSINESS_HOURS_MARK);
        }

        @Test
        @DisplayName("영업 요일만 있고 시각이 없으면 영업시간 자리표시자가 그대로 남는다")
        void keepsBusinessHoursPlaceholder_whenBusinessClockMissing() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-applied-no-clock");
            writeOperationInfo(signup.accessToken(), null, null);

            String body = applyNewMenuTemplate(signup.accessToken());

            assertThat(body).contains(BUSINESS_HOURS_MARK);
        }

        @Test
        @DisplayName("전화번호 자리표시자는 매장 정보를 다 채워도 그대로 남는다")
        void keepsPhonePlaceholder_whenEveryStoreInfoWritten() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-applied-phone");
            writeBasicInfo(signup.accessToken());
            writeOperationInfo(signup.accessToken(), "09:00", "20:00");

            String body = applyNewMenuTemplate(signup.accessToken());

            assertThat(body).contains(PHONE_MARK);
        }

        @Test
        @DisplayName("상세 조회는 매장 정보를 채운 회원에게도 치환하지 않은 원문을 준다")
        void leavesDetailBodyUntouched_whenStoreInfoWritten() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-applied-detail");
            writeBasicInfo(signup.accessToken());
            writeOperationInfo(signup.accessToken(), "09:00", "20:00");
            TemplateResponse card = cardOf(signup.accessToken(), NEW_MENU_TITLE);

            TemplateDetailResponse detail = fixture.templateDetail(signup.accessToken(), card.id());

            assertThat(detail.body()).contains(STORE_NAME_MARK, ADDRESS_MARK, BUSINESS_HOURS_MARK);
            assertThat(detail.body()).doesNotContain(STORE_NAME, STORE_ADDRESS);
        }

        @Test
        @DisplayName("없는 템플릿을 적용하면 404 와 TP0001 을 반환한다")
        void returns404_whenTemplateIsMissing() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-applied-missing");

            fixture.getAppliedTemplate(signup.accessToken(), MISSING_TEMPLATE_ID)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code())
                    .isEqualTo(TemplateErrorCode.TEMPLATE_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 적용하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenQueries() {
            SignupResponse signup = fixture.signupActiveMember("naver-template-applied-seed");
            Long templateId = cardOf(signup.accessToken(), NEW_MENU_TITLE).id();
            SocialLoginResponse login = fixture.naverLoginMember("naver-template-applied-pending");

            fixture.getAppliedTemplate(login.accessToken(), templateId)
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 적용하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            client().get().uri("/v1/templates/" + ANY_TEMPLATE_ID + "/applied")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }
}
