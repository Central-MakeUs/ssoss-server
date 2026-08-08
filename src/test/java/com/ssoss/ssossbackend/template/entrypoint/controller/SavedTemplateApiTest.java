package com.ssoss.ssossbackend.template.entrypoint.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;
import com.ssoss.ssossbackend.template.domain.model.Channel;
import com.ssoss.ssossbackend.template.domain.model.SavedTemplate;
import com.ssoss.ssossbackend.template.domain.model.SavedTemplateHistory;
import com.ssoss.ssossbackend.template.domain.model.TemplateErrorCode;
import com.ssoss.ssossbackend.template.entrypoint.response.SavedTemplateDetailResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.SavedTemplateListResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.SavedTemplateSaveResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.SavedTemplateSummaryResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.TemplateDetailResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.TemplateResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("저장한 템플릿 API")
class SavedTemplateApiTest extends IntegrationTest {

    private static final long MISSING_TEMPLATE_ID = 999_999L;
    private static final long MISSING_SAVED_TEMPLATE_ID = 999_999L;
    private static final String EDITED_BODY = """
        보니스커피에 새 메뉴가 출시되었습니다!

        🎁신메뉴: 흑임자 라떼
        💰가격: 6,500원""";
    private static final String LONGEST_BODY = "가".repeat(2000);
    private static final String TOO_LONG_BODY = "가".repeat(2001);
    private static final String EDITED_TITLE = "9월 신메뉴 안내";
    private static final String LONGEST_TITLE = "가".repeat(100);
    private static final String TOO_LONG_TITLE = "가".repeat(101);

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

    @Nested
    @DisplayName("GET /v1/saved-templates")
    class ListSavedTemplates {

        @Test
        @DisplayName("sort 를 생략하면 저장 시각 최신순으로 온다")
        void returnsSavedTemplatesInSavedAtDescendingOrder_whenSortOmitted() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-list-default");
            Long templateId = fixture.firstTemplate(signup.accessToken()).id();
            Long oldestId = fixture.savedTemplateId(signup.accessToken(), templateId, "먼저 저장한 본문");
            Long newestId = fixture.savedTemplateId(signup.accessToken(), templateId, "나중에 저장한 본문");

            assertThat(fixture.savedTemplateList(signup.accessToken(), "").savedTemplates())
                .extracting(SavedTemplateSummaryResponse::savedTemplateId)
                .containsExactly(newestId, oldestId);
        }

        @Test
        @DisplayName("sort 를 LATEST 로 부르면 저장 시각 최신순으로 온다")
        void returnsSavedTemplatesInSavedAtDescendingOrder_whenLatestRequested() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-list-latest");
            Long templateId = fixture.firstTemplate(signup.accessToken()).id();
            Long oldestId = fixture.savedTemplateId(signup.accessToken(), templateId, "먼저 저장한 본문");
            Long newestId = fixture.savedTemplateId(signup.accessToken(), templateId, "나중에 저장한 본문");

            assertThat(fixture.savedTemplateList(signup.accessToken(), "?sort=LATEST").savedTemplates())
                .extracting(SavedTemplateSummaryResponse::savedTemplateId)
                .containsExactly(newestId, oldestId);
        }

        @Test
        @DisplayName("sort 를 빈 값으로 보내면 생략한 것과 같이 최신순으로 온다")
        void returnsSavedTemplatesInSavedAtDescendingOrder_whenSortBlank() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-list-blank-sort");
            Long templateId = fixture.firstTemplate(signup.accessToken()).id();
            Long oldestId = fixture.savedTemplateId(signup.accessToken(), templateId, "먼저 저장한 본문");
            Long newestId = fixture.savedTemplateId(signup.accessToken(), templateId, "나중에 저장한 본문");

            assertThat(fixture.savedTemplateList(signup.accessToken(), "?sort=").savedTemplates())
                .extracting(SavedTemplateSummaryResponse::savedTemplateId)
                .containsExactly(newestId, oldestId);
        }

        @Test
        @DisplayName("sort 를 OLDEST 로 부르면 저장 시각 오래된 순으로 온다")
        void returnsSavedTemplatesInSavedAtAscendingOrder_whenOldestRequested() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-list-oldest");
            Long templateId = fixture.firstTemplate(signup.accessToken()).id();
            Long oldestId = fixture.savedTemplateId(signup.accessToken(), templateId, "먼저 저장한 본문");
            Long middleId = fixture.savedTemplateId(signup.accessToken(), templateId, "그다음 저장한 본문");
            Long newestId = fixture.savedTemplateId(signup.accessToken(), templateId, "나중에 저장한 본문");

            assertThat(fixture.savedTemplateList(signup.accessToken(), "?sort=OLDEST").savedTemplates())
                .extracting(SavedTemplateSummaryResponse::savedTemplateId)
                .containsExactly(oldestId, middleId, newestId);
        }

        @Test
        @DisplayName("페이지를 넘겨도 고른 정렬이 이어지고 전체 건수와 다음 페이지 여부가 함께 온다")
        void keepsChosenOrderAcrossPages_withTotalCountAndHasNext() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-list-paging");
            Long templateId = fixture.firstTemplate(signup.accessToken()).id();
            Long oldestId = fixture.savedTemplateId(signup.accessToken(), templateId, "먼저 저장한 본문");
            Long middleId = fixture.savedTemplateId(signup.accessToken(), templateId, "그다음 저장한 본문");
            Long newestId = fixture.savedTemplateId(signup.accessToken(), templateId, "나중에 저장한 본문");

            SavedTemplateListResponse firstPage =
                fixture.savedTemplateList(signup.accessToken(), "?sort=OLDEST&page=0&size=2");
            SavedTemplateListResponse secondPage =
                fixture.savedTemplateList(signup.accessToken(), "?sort=OLDEST&page=1&size=2");

            assertThat(firstPage.totalCount()).isEqualTo(3);
            assertThat(firstPage.page()).isZero();
            assertThat(firstPage.size()).isEqualTo(2);
            assertThat(firstPage.hasNext()).isTrue();
            assertThat(firstPage.savedTemplates())
                .extracting(SavedTemplateSummaryResponse::savedTemplateId)
                .containsExactly(oldestId, middleId);
            assertThat(secondPage.totalCount()).isEqualTo(3);
            assertThat(secondPage.hasNext()).isFalse();
            assertThat(secondPage.savedTemplates())
                .extracting(SavedTemplateSummaryResponse::savedTemplateId)
                .containsExactly(newestId);
        }

        @Test
        @DisplayName("카드에 분류와 제목과 설명과 저장일이 담긴다")
        void returnsCategoryTitleDescriptionAndSavedAt_onEachCard() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-list-card");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());
            Long savedTemplateId = fixture.savedTemplateId(signup.accessToken(), card.id(), EDITED_BODY);

            assertThat(fixture.savedTemplateList(signup.accessToken(), "").savedTemplates())
                .singleElement()
                .satisfies(saved -> {
                    assertThat(saved.savedTemplateId()).isEqualTo(savedTemplateId);
                    assertThat(saved.category()).isEqualTo(card.category());
                    assertThat(saved.title()).isEqualTo(card.title());
                    assertThat(saved.description()).isEqualTo(card.description());
                    assertThat(saved.savedAt())
                        .isEqualTo(database.savedTemplateOf(savedTemplateId).getCreatedAt());
                });
        }

        @Test
        @DisplayName("나중에 저장한 글의 저장 시각이 더 이르면 오래된 순에서 그 글이 먼저 온다")
        void ordersBySavedAtNotById_whenOldestRequested() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-list-rewound-oldest");
            Long templateId = fixture.firstTemplate(signup.accessToken()).id();
            Long laterSavedId = fixture.savedTemplate(signup.accessToken(), templateId, "저장 시각이 늦은 본문")
                .savedTemplateId();
            clock.advanceBy(Duration.ofMinutes(-1));
            Long earlierSavedId = fixture.savedTemplate(signup.accessToken(), templateId, "저장 시각이 이른 본문")
                .savedTemplateId();

            assertThat(fixture.savedTemplateList(signup.accessToken(), "?sort=OLDEST").savedTemplates())
                .extracting(SavedTemplateSummaryResponse::savedTemplateId)
                .containsExactly(earlierSavedId, laterSavedId);
        }

        @Test
        @DisplayName("나중에 저장한 글의 저장 시각이 더 이르면 최신순에서 그 글이 뒤에 온다")
        void ordersBySavedAtNotById_whenLatestRequested() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-list-rewound-latest");
            Long templateId = fixture.firstTemplate(signup.accessToken()).id();
            Long laterSavedId = fixture.savedTemplate(signup.accessToken(), templateId, "저장 시각이 늦은 본문")
                .savedTemplateId();
            clock.advanceBy(Duration.ofMinutes(-1));
            Long earlierSavedId = fixture.savedTemplate(signup.accessToken(), templateId, "저장 시각이 이른 본문")
                .savedTemplateId();

            assertThat(fixture.savedTemplateList(signup.accessToken(), "?sort=LATEST").savedTemplates())
                .extracting(SavedTemplateSummaryResponse::savedTemplateId)
                .containsExactly(laterSavedId, earlierSavedId);
        }

        @Test
        @DisplayName("저장 시각이 같으면 나중에 저장한 글이 최신순에서 먼저 온다")
        void ordersByIdDescending_whenSavedAtIsSame() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-list-same-clock");
            Long templateId = fixture.firstTemplate(signup.accessToken()).id();
            Long firstId = fixture.savedTemplate(signup.accessToken(), templateId, "먼저 저장한 본문").savedTemplateId();
            Long secondId = fixture.savedTemplate(signup.accessToken(), templateId, "나중에 저장한 본문").savedTemplateId();

            assertThat(fixture.savedTemplateList(signup.accessToken(), "").savedTemplates())
                .extracting(SavedTemplateSummaryResponse::savedTemplateId)
                .containsExactly(secondId, firstId);
        }

        @Test
        @DisplayName("다른 회원이 저장한 글은 내 목록과 전체 건수에 들어가지 않는다")
        void excludesSavedTemplatesOfOtherMembers() {
            SignupResponse mine = fixture.signupActiveMember("naver-saved-template-list-mine");
            SignupResponse other = fixture.signupActiveMember("naver-saved-template-list-other");
            Long templateId = fixture.firstTemplate(mine.accessToken()).id();
            Long mineId = fixture.savedTemplateId(mine.accessToken(), templateId, EDITED_BODY);
            fixture.savedTemplateId(other.accessToken(), templateId, "다른 회원의 본문");

            SavedTemplateListResponse body = fixture.savedTemplateList(mine.accessToken(), "");

            assertThat(body.totalCount()).isEqualTo(1);
            assertThat(body.savedTemplates())
                .extracting(SavedTemplateSummaryResponse::savedTemplateId)
                .containsExactly(mineId);
        }

        @Test
        @DisplayName("저장한 글이 없으면 빈 목록과 전체 건수 0 을 반환한다")
        void returnsEmptyList_whenNothingSaved() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-list-empty");

            SavedTemplateListResponse body = fixture.savedTemplateList(signup.accessToken(), "");

            assertThat(body.totalCount()).isZero();
            assertThat(body.hasNext()).isFalse();
            assertThat(body.savedTemplates()).isEmpty();
        }

        @Test
        @DisplayName("없는 정렬값으로 부르면 400 과 C0001 을 반환한다")
        void returns400_whenSortIsUnknown() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-list-bad-sort");

            fixture.getSavedTemplates(signup.accessToken(), "?sort=NEWEST")
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("size 가 상한을 넘으면 400 과 C0001 을 반환한다")
        void returns400_whenSizeExceedsLimit() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-list-bad-size");

            fixture.getSavedTemplates(signup.accessToken(), "?size=51")
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 조회하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenLists() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-saved-template-list-pending");

            fixture.getSavedTemplates(login.accessToken(), "")
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 조회하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            client().get().uri("/v1/saved-templates")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }

    @Nested
    @DisplayName("GET /v1/saved-templates/{savedTemplateId}")
    class GetSavedTemplate {

        @Test
        @DisplayName("저장한 본문과 분류와 제목과 설명과 추천 채널과 저장 시각을 반환한다")
        void returnsSavedBodyWithCategoryTitleDescriptionRecommendedChannelsAndSavedAt() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-detail");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());
            Long savedTemplateId = fixture.savedTemplateId(signup.accessToken(), card.id(), EDITED_BODY);

            SavedTemplateDetailResponse body =
                fixture.savedTemplateDetail(signup.accessToken(), savedTemplateId);

            assertThat(body.savedTemplateId()).isEqualTo(savedTemplateId);
            assertThat(body.body()).isEqualTo(EDITED_BODY);
            assertThat(body.category()).isEqualTo(card.category());
            assertThat(body.title()).isEqualTo(card.title());
            assertThat(body.description()).isEqualTo(card.description());
            assertThat(body.recommendedChannels()).isEqualTo(card.recommendedChannels());
            assertThat(body.savedAt()).isEqualTo(database.savedTemplateOf(savedTemplateId).getCreatedAt());
        }

        @Test
        @DisplayName("저장한 글이 여러 건이면 요청한 id 의 글을 반환한다")
        void returnsRequestedSavedTemplate_whenMemberHasSeveralSavedTemplates() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-detail-many");
            List<TemplateResponse> cards = fixture.templateList(signup.accessToken(), "").templates();
            TemplateResponse firstCard = cards.getFirst();
            TemplateResponse secondCard = cards.get(1);
            Long firstSavedTemplateId =
                fixture.savedTemplateId(signup.accessToken(), firstCard.id(), EDITED_BODY);
            Long secondSavedTemplateId =
                fixture.savedTemplateId(signup.accessToken(), secondCard.id(), "두 번째로 저장한 본문");

            SavedTemplateDetailResponse first =
                fixture.savedTemplateDetail(signup.accessToken(), firstSavedTemplateId);
            SavedTemplateDetailResponse second =
                fixture.savedTemplateDetail(signup.accessToken(), secondSavedTemplateId);

            assertThat(first.savedTemplateId()).isEqualTo(firstSavedTemplateId);
            assertThat(first.body()).isEqualTo(EDITED_BODY);
            assertThat(first.title()).isEqualTo(firstCard.title());
            assertThat(second.savedTemplateId()).isEqualTo(secondSavedTemplateId);
            assertThat(second.body()).isEqualTo("두 번째로 저장한 본문");
            assertThat(second.title()).isEqualTo(secondCard.title());
        }

        @Test
        @DisplayName("다른 회원이 저장한 글의 상세를 조회하면 404 와 TP0002 를 반환한다")
        void returns404_whenSavedTemplateBelongsToAnotherMember() {
            SignupResponse mine = fixture.signupActiveMember("naver-saved-template-detail-mine");
            SignupResponse other = fixture.signupActiveMember("naver-saved-template-detail-other");
            Long templateId = fixture.firstTemplate(other.accessToken()).id();
            Long othersSavedTemplateId =
                fixture.savedTemplateId(other.accessToken(), templateId, "다른 회원의 본문");

            fixture.getSavedTemplate(mine.accessToken(), othersSavedTemplateId)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code())
                    .isEqualTo(TemplateErrorCode.SAVED_TEMPLATE_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("없는 id 로 조회하면 404 와 TP0002 를 반환한다")
        void returns404_whenSavedTemplateIsMissing() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-detail-missing");

            fixture.getSavedTemplate(signup.accessToken(), MISSING_SAVED_TEMPLATE_ID)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code())
                    .isEqualTo(TemplateErrorCode.SAVED_TEMPLATE_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 조회하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenGets() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-saved-template-detail-pending");

            fixture.getSavedTemplate(login.accessToken(), MISSING_SAVED_TEMPLATE_ID)
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 조회하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            client().get().uri("/v1/saved-templates/" + MISSING_SAVED_TEMPLATE_ID)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }

    @Nested
    @DisplayName("PUT /v1/saved-templates/{savedTemplateId}")
    class EditSavedTemplate {

        @Test
        @DisplayName("제목만 고치면 목록과 상세에 새 제목이 나오고 본문은 그대로다")
        void returnsEditedTitleInListAndDetail_whenOnlyTitleEdited() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-edit-title");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());
            Long savedTemplateId = fixture.savedTemplateId(signup.accessToken(), card.id(), EDITED_BODY);

            SavedTemplateDetailResponse edited =
                fixture.editedSavedTemplate(signup.accessToken(), savedTemplateId, EDITED_TITLE, EDITED_BODY);

            assertThat(edited.title()).isEqualTo(EDITED_TITLE);
            assertThat(edited.body()).isEqualTo(EDITED_BODY);
            assertThat(fixture.savedTemplateDetail(signup.accessToken(), savedTemplateId).title())
                .isEqualTo(EDITED_TITLE);
            assertThat(fixture.savedTemplateList(signup.accessToken(), "").savedTemplates())
                .singleElement()
                .satisfies(saved -> assertThat(saved.title()).isEqualTo(EDITED_TITLE));
        }

        @Test
        @DisplayName("본문만 고치면 상세에 새 본문이 나오고 제목은 그대로다")
        void returnsEditedBodyInDetail_whenOnlyBodyEdited() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-edit-body");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());
            Long savedTemplateId = fixture.savedTemplateId(signup.accessToken(), card.id(), EDITED_BODY);

            SavedTemplateDetailResponse edited =
                fixture.editedSavedTemplate(signup.accessToken(), savedTemplateId, card.title(), "다시 고친 본문");

            assertThat(edited.body()).isEqualTo("다시 고친 본문");
            assertThat(edited.title()).isEqualTo(card.title());
            assertThat(fixture.savedTemplateDetail(signup.accessToken(), savedTemplateId).body())
                .isEqualTo("다시 고친 본문");
        }

        @Test
        @DisplayName("같은 템플릿에서 저장한 두 건의 제목을 각각 다르게 고칠 수 있다")
        void editsEachSavedTemplateSeparately_whenBothCameFromSameTemplate() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-edit-siblings");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());
            Long firstId = fixture.savedTemplateId(signup.accessToken(), card.id(), "8월에 쓴 본문");
            Long secondId = fixture.savedTemplateId(signup.accessToken(), card.id(), "9월에 쓴 본문");

            fixture.editedSavedTemplate(signup.accessToken(), firstId, "8월 신메뉴 안내", "8월에 쓴 본문");
            fixture.editedSavedTemplate(signup.accessToken(), secondId, "9월 신메뉴 안내", "9월에 쓴 본문");

            assertThat(fixture.savedTemplateDetail(signup.accessToken(), firstId).title())
                .isEqualTo("8월 신메뉴 안내");
            assertThat(fixture.savedTemplateDetail(signup.accessToken(), secondId).title())
                .isEqualTo("9월 신메뉴 안내");
        }

        @Test
        @DisplayName("편집하면 이전 제목과 본문이 히스토리에 남는다")
        void writesPreviousValuesToHistory_whenEdited() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-edit-history");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());
            Long savedTemplateId = fixture.savedTemplateId(signup.accessToken(), card.id(), EDITED_BODY);

            fixture.editedSavedTemplate(signup.accessToken(), savedTemplateId, EDITED_TITLE, "다시 고친 본문");

            assertThat(database.savedTemplateHistoriesOf(savedTemplateId))
                .singleElement()
                .satisfies(history -> {
                    assertThat(history.getTitle()).isEqualTo(card.title());
                    assertThat(history.getBody()).isEqualTo(EDITED_BODY);
                    assertThat(history.getCreatedAt()).isNotNull();
                });
        }

        @Test
        @DisplayName("두 번 편집하면 히스토리가 두 건 쌓인다")
        void writesHistoryPerEdit_whenEditedTwice() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-edit-history-twice");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());
            Long savedTemplateId = fixture.savedTemplateId(signup.accessToken(), card.id(), EDITED_BODY);

            fixture.editedSavedTemplate(signup.accessToken(), savedTemplateId, EDITED_TITLE, "처음 고친 본문");
            fixture.editedSavedTemplate(signup.accessToken(), savedTemplateId, EDITED_TITLE, "다시 고친 본문");

            assertThat(database.savedTemplateHistoriesOf(savedTemplateId))
                .extracting(SavedTemplateHistory::getBody)
                .containsExactly(EDITED_BODY, "처음 고친 본문");
        }

        @Test
        @DisplayName("값이 그대로인 편집은 히스토리를 남기지 않고 수정 시각도 움직이지 않는다")
        void writesNoHistory_whenValuesUnchanged() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-edit-unchanged");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());
            Long savedTemplateId = fixture.savedTemplateId(signup.accessToken(), card.id(), EDITED_BODY);
            Instant updatedAt = database.savedTemplateOf(savedTemplateId).getUpdatedAt();
            clock.advanceBy(Duration.ofMinutes(10));

            fixture.editedSavedTemplate(signup.accessToken(), savedTemplateId, card.title(), EDITED_BODY);

            assertThat(database.savedTemplateHistoriesOf(savedTemplateId)).isEmpty();
            assertThat(database.savedTemplateOf(savedTemplateId).getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("편집해도 저장 시각은 그대로고 수정 시각만 움직인다")
        void keepsSavedAtAndMovesUpdatedAt_whenEdited() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-edit-saved-at");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());
            Long savedTemplateId = fixture.savedTemplateId(signup.accessToken(), card.id(), EDITED_BODY);
            Instant savedAt = database.savedTemplateOf(savedTemplateId).getCreatedAt();
            Instant updatedAt = database.savedTemplateOf(savedTemplateId).getUpdatedAt();
            clock.advanceBy(Duration.ofMinutes(10));

            SavedTemplateDetailResponse edited =
                fixture.editedSavedTemplate(signup.accessToken(), savedTemplateId, EDITED_TITLE, "다시 고친 본문");

            assertThat(edited.savedAt()).isEqualTo(savedAt);
            assertThat(database.savedTemplateOf(savedTemplateId).getCreatedAt()).isEqualTo(savedAt);
            assertThat(database.savedTemplateOf(savedTemplateId).getUpdatedAt()).isAfter(updatedAt);
        }

        @Test
        @DisplayName("편집해도 원본 템플릿의 제목과 본문은 그대로다")
        void keepsOriginTemplate_whenEdited() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-edit-origin");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());
            TemplateDetailResponse origin = fixture.templateDetail(signup.accessToken(), card.id());
            Long savedTemplateId = fixture.savedTemplateId(signup.accessToken(), card.id(), EDITED_BODY);

            fixture.editedSavedTemplate(signup.accessToken(), savedTemplateId, EDITED_TITLE, "다시 고친 본문");

            assertThat(fixture.templateDetail(signup.accessToken(), card.id())).isEqualTo(origin);
        }

        @Test
        @DisplayName("다른 회원이 같은 템플릿에서 저장한 글은 편집에 영향받지 않는다")
        void keepsSavedTemplatesOfOtherMembers_whenEdited() {
            SignupResponse mine = fixture.signupActiveMember("naver-saved-template-edit-mine");
            SignupResponse other = fixture.signupActiveMember("naver-saved-template-edit-other");
            TemplateResponse card = fixture.firstTemplate(mine.accessToken());
            Long mineId = fixture.savedTemplateId(mine.accessToken(), card.id(), EDITED_BODY);
            Long othersId = fixture.savedTemplateId(other.accessToken(), card.id(), "다른 회원의 본문");

            fixture.editedSavedTemplate(mine.accessToken(), mineId, EDITED_TITLE, "다시 고친 본문");

            SavedTemplateDetailResponse others = fixture.savedTemplateDetail(other.accessToken(), othersId);
            assertThat(others.title()).isEqualTo(card.title());
            assertThat(others.body()).isEqualTo("다른 회원의 본문");
        }

        @Test
        @DisplayName("제목이 100자면 편집된다")
        void editsTitle_whenTitleIsAtLengthLimit() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-edit-longest-title");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());
            Long savedTemplateId = fixture.savedTemplateId(signup.accessToken(), card.id(), EDITED_BODY);

            SavedTemplateDetailResponse edited =
                fixture.editedSavedTemplate(signup.accessToken(), savedTemplateId, LONGEST_TITLE, EDITED_BODY);

            assertThat(edited.title()).isEqualTo(LONGEST_TITLE);
        }

        @Test
        @DisplayName("제목이 100자를 넘으면 400 과 C0001 을 반환하고 원래 값이 그대로다")
        void returns400_whenTitleExceedsLengthLimit() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-edit-long-title");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());
            Long savedTemplateId = fixture.savedTemplateId(signup.accessToken(), card.id(), EDITED_BODY);

            fixture.editSavedTemplate(signup.accessToken(), savedTemplateId,
                    Map.of("title", TOO_LONG_TITLE, "body", EDITED_BODY))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
            assertThat(database.savedTemplateOf(savedTemplateId).getTitle()).isEqualTo(card.title());
        }

        @Test
        @DisplayName("본문이 2000자를 넘으면 400 과 C0001 을 반환하고 원래 값이 그대로다")
        void returns400_whenBodyExceedsLengthLimit() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-edit-long-body");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());
            Long savedTemplateId = fixture.savedTemplateId(signup.accessToken(), card.id(), EDITED_BODY);

            fixture.editSavedTemplate(signup.accessToken(), savedTemplateId,
                    Map.of("title", EDITED_TITLE, "body", TOO_LONG_BODY))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
            assertThat(database.savedTemplateOf(savedTemplateId).getBody()).isEqualTo(EDITED_BODY);
        }

        @Test
        @DisplayName("본문이 2000자면 편집된다")
        void editsBody_whenBodyIsAtLengthLimit() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-edit-longest-body");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());
            Long savedTemplateId = fixture.savedTemplateId(signup.accessToken(), card.id(), EDITED_BODY);

            SavedTemplateDetailResponse edited =
                fixture.editedSavedTemplate(signup.accessToken(), savedTemplateId, EDITED_TITLE, LONGEST_BODY);

            assertThat(edited.body()).isEqualTo(LONGEST_BODY);
        }

        @Test
        @DisplayName("제목이 비면 400 과 C0001 을 반환한다")
        void returns400_whenTitleIsBlank() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-edit-blank-title");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());
            Long savedTemplateId = fixture.savedTemplateId(signup.accessToken(), card.id(), EDITED_BODY);

            fixture.editSavedTemplate(signup.accessToken(), savedTemplateId,
                    Map.of("title", " ", "body", EDITED_BODY))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("본문이 비면 400 과 C0001 을 반환한다")
        void returns400_whenBodyIsBlank() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-edit-blank-body");
            TemplateResponse card = fixture.firstTemplate(signup.accessToken());
            Long savedTemplateId = fixture.savedTemplateId(signup.accessToken(), card.id(), EDITED_BODY);

            fixture.editSavedTemplate(signup.accessToken(), savedTemplateId,
                    Map.of("title", EDITED_TITLE, "body", ""))
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("다른 회원이 저장한 글을 편집하면 404 와 TP0002 를 반환한다")
        void returns404_whenSavedTemplateBelongsToAnotherMember() {
            SignupResponse mine = fixture.signupActiveMember("naver-saved-template-edit-404-mine");
            SignupResponse other = fixture.signupActiveMember("naver-saved-template-edit-404-other");
            Long templateId = fixture.firstTemplate(other.accessToken()).id();
            Long othersSavedTemplateId =
                fixture.savedTemplateId(other.accessToken(), templateId, "다른 회원의 본문");

            fixture.editSavedTemplate(mine.accessToken(), othersSavedTemplateId,
                    Map.of("title", EDITED_TITLE, "body", EDITED_BODY))
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code())
                    .isEqualTo(TemplateErrorCode.SAVED_TEMPLATE_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("없는 id 를 편집하면 404 와 TP0002 를 반환한다")
        void returns404_whenSavedTemplateIsMissing() {
            SignupResponse signup = fixture.signupActiveMember("naver-saved-template-edit-missing");

            fixture.editSavedTemplate(signup.accessToken(), MISSING_SAVED_TEMPLATE_ID,
                    Map.of("title", EDITED_TITLE, "body", EDITED_BODY))
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code())
                    .isEqualTo(TemplateErrorCode.SAVED_TEMPLATE_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 편집하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenEdits() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-saved-template-edit-pending");

            fixture.editSavedTemplate(login.accessToken(), MISSING_SAVED_TEMPLATE_ID,
                    Map.of("title", EDITED_TITLE, "body", EDITED_BODY))
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 편집하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            client().put().uri("/v1/saved-templates/" + MISSING_SAVED_TEMPLATE_ID)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }
}
