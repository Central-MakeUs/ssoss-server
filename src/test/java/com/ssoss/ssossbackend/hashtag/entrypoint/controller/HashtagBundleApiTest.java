package com.ssoss.ssossbackend.hashtag.entrypoint.controller;

import java.util.Comparator;
import java.util.List;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.hashtag.entrypoint.response.HashtagBundleListResponse;
import com.ssoss.ssossbackend.hashtag.entrypoint.response.HashtagBundleResponse;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("해시태그 묶음 목록 API")
class HashtagBundleApiTest extends IntegrationTest {

    @Nested
    @DisplayName("GET /v1/hashtag-bundles")
    class ListBundles {

        @Test
        @DisplayName("가입 회원이 조회하면 심어 둔 묶음이 id 역순으로 담긴다")
        void listsSeededBundlesInIdDescendingOrder_whenActiveMemberQueries() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-list");

            HashtagBundleListResponse body = fixture.hashtagBundleList(signup.accessToken(), "");

            assertThat(body.page()).isZero();
            assertThat(body.size()).isEqualTo(20);
            assertThat(body.totalCount()).isGreaterThanOrEqualTo(3);
            assertThat(body.bundles()).extracting(HashtagBundleResponse::id)
                .isSortedAccordingTo(Comparator.reverseOrder());
            assertThat(body.bundles()).extracting(HashtagBundleResponse::name)
                .contains("카공 카페", "이벤트/할인 홍보", "동네 고객 유입 해시태그");
        }

        @Test
        @DisplayName("항목마다 id 와 이름과 해시태그 전부가 담긴다")
        void carriesIdNameAndEveryHashtag_whenActiveMemberQueries() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-item");

            HashtagBundleListResponse body = fixture.hashtagBundleList(signup.accessToken(), "");

            assertThat(body.bundles()).allSatisfy(bundle -> {
                assertThat(bundle.id()).isNotNull();
                assertThat(bundle.name()).isNotBlank();
                assertThat(bundle.hashtags()).isNotEmpty();
            });
            assertThat(body.bundles())
                .filteredOn(bundle -> bundle.name().equals("동네 고객 유입 해시태그"))
                .singleElement()
                .satisfies(bundle -> assertThat(bundle.hashtags()).contains("#00동카페"));
        }

        @Test
        @DisplayName("북마크한 적 없는 회원이 조회하면 항목마다 bookmarked 가 거짓이다")
        void marksEveryBundleUnbookmarked_whenMemberBookmarkedNothing() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-none-marked");

            HashtagBundleListResponse body = fixture.hashtagBundleList(signup.accessToken(), "");

            assertThat(body.bundles()).isNotEmpty();
            assertThat(body.bundles()).extracting(HashtagBundleResponse::bookmarked)
                .containsOnly(false);
        }

        @Test
        @DisplayName("북마크한 묶음만 bookmarked 가 참으로 담긴다")
        void marksOnlyBookmarkedBundle_whenMemberBookmarkedOne() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-one-marked");
            HashtagBundleResponse target =
                fixture.hashtagBundleList(signup.accessToken(), "").bundles().getFirst();
            fixture.bookmarkedHashtagBundle(signup.accessToken(), target.id());

            HashtagBundleListResponse body = fixture.hashtagBundleList(signup.accessToken(), "");

            assertThat(body.bundles()).filteredOn(bundle -> bundle.id().equals(target.id()))
                .singleElement()
                .satisfies(bundle -> assertThat(bundle.bookmarked()).isTrue());
            assertThat(body.bundles()).filteredOn(bundle -> !bundle.id().equals(target.id()))
                .allSatisfy(bundle -> assertThat(bundle.bookmarked()).isFalse());
        }

        @Test
        @DisplayName("북마크를 해제하면 bookmarked 가 거짓으로 돌아온다")
        void marksBundleUnbookmarked_whenBookmarkRemoved() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-unmarked");
            HashtagBundleResponse target =
                fixture.hashtagBundleList(signup.accessToken(), "").bundles().getFirst();
            fixture.bookmarkedHashtagBundle(signup.accessToken(), target.id());

            fixture.unbookmarkedHashtagBundle(signup.accessToken(), target.id());

            HashtagBundleListResponse body = fixture.hashtagBundleList(signup.accessToken(), "");
            assertThat(body.bundles()).filteredOn(bundle -> bundle.id().equals(target.id()))
                .singleElement()
                .satisfies(bundle -> assertThat(bundle.bookmarked()).isFalse());
        }

        @Test
        @DisplayName("다른 회원의 북마크는 내 응답의 bookmarked 에 영향을 주지 않는다")
        void ignoresOtherMembersBookmark_whenActiveMemberQueries() {
            SignupResponse other = fixture.signupActiveMember("naver-hashtag-other-marked");
            SignupResponse mine = fixture.signupActiveMember("naver-hashtag-mine-unmarked");
            HashtagBundleResponse target =
                fixture.hashtagBundleList(other.accessToken(), "").bundles().getFirst();
            fixture.bookmarkedHashtagBundle(other.accessToken(), target.id());

            HashtagBundleListResponse body = fixture.hashtagBundleList(mine.accessToken(), "");

            assertThat(body.bundles()).extracting(HashtagBundleResponse::bookmarked)
                .containsOnly(false);
        }

        @Test
        @DisplayName("페이지를 넘겨도 그 페이지의 bookmarked 가 맞다")
        void marksBookmarkedBundleOnFollowingPage_whenPaged() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-paged-mark");
            HashtagBundleResponse second =
                fixture.hashtagBundleList(signup.accessToken(), "?size=1&page=1").bundles().getFirst();
            fixture.bookmarkedHashtagBundle(signup.accessToken(), second.id());

            HashtagBundleListResponse first = fixture.hashtagBundleList(signup.accessToken(), "?size=1&page=0");
            HashtagBundleListResponse paged = fixture.hashtagBundleList(signup.accessToken(), "?size=1&page=1");

            assertThat(first.bundles()).singleElement()
                .satisfies(bundle -> assertThat(bundle.bookmarked()).isFalse());
            assertThat(paged.bundles()).singleElement().satisfies(bundle -> {
                assertThat(bundle.id()).isEqualTo(second.id());
                assertThat(bundle.bookmarked()).isTrue();
            });
        }

        @Test
        @DisplayName("size 를 1 로 부르면 한 건만 담기고 다음 페이지가 있다고 알려준다")
        void returnsSingleBundleWithNextPage_whenSizeIsOne() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-size");

            HashtagBundleListResponse body = fixture.hashtagBundleList(signup.accessToken(), "?size=1");

            assertThat(body.size()).isEqualTo(1);
            assertThat(body.bundles()).hasSize(1);
            assertThat(body.hasNext()).isTrue();
        }

        @Test
        @DisplayName("페이지를 넘기면 그다음 묶음이 담긴다")
        void returnsFollowingBundle_whenNextPageRequested() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-page");
            HashtagBundleListResponse first = fixture.hashtagBundleList(signup.accessToken(), "?size=1");

            HashtagBundleListResponse second = fixture.hashtagBundleList(signup.accessToken(), "?size=1&page=1");

            assertThat(second.page()).isEqualTo(1);
            assertThat(second.bundles()).singleElement().satisfies(bundle ->
                assertThat(bundle.id()).isLessThan(first.bundles().getFirst().id()));
        }

        @Test
        @DisplayName("size 가 50 을 넘으면 400 과 C0001 을 반환한다")
        void returns400_whenSizeExceedsLimit() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-size-limit");

            fixture.getHashtagBundles(signup.accessToken(), "?size=51")
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("page 가 음수면 400 과 C0001 을 반환한다")
        void returns400_whenPageIsNegative() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-page-negative");

            fixture.getHashtagBundles(signup.accessToken(), "?page=-1")
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 조회하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenQueries() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-hashtag-pending");

            fixture.getHashtagBundles(login.accessToken(), "")
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 조회하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            client().get().uri("/v1/hashtag-bundles")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }

    @Nested
    @DisplayName("GET /v1/hashtag-bundles?keyword=")
    class SearchBundles {

        private static final String BY_KEYWORD = "?keyword={keyword}";

        @Test
        @DisplayName("묶음 이름에만 있는 말로 검색하면 그 묶음만 남는다")
        void keepsBundleMatchedByName_whenKeywordHitsNameOnly() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-search-name");

            HashtagBundleListResponse body = fixture.hashtagBundleList(signup.accessToken(), BY_KEYWORD, "홍보");

            assertThat(body.totalCount()).isEqualTo(1);
            assertThat(body.bundles()).singleElement().satisfies(bundle -> {
                assertThat(bundle.name()).isEqualTo("이벤트/할인 홍보");
                assertThat(bundle.hashtags()).noneMatch(hashtag -> hashtag.contains("홍보"));
            });
        }

        @Test
        @DisplayName("태그를 통째로 쳐서 검색하면 그 태그를 가진 묶음이 남는다")
        void keepsBundleMatchedByWholeHashtag_whenKeywordIsHashtag() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-search-tag");

            HashtagBundleListResponse body =
                fixture.hashtagBundleList(signup.accessToken(), BY_KEYWORD, "#콘센트많은카페");

            assertThat(body.totalCount()).isEqualTo(1);
            assertThat(body.bundles()).singleElement().satisfies(bundle -> {
                assertThat(bundle.name()).isEqualTo("카공 카페");
                assertThat(bundle.hashtags()).contains("#콘센트많은카페");
            });
        }

        @Test
        @DisplayName("태그 일부만 쳐서 검색해도 그 태그를 가진 묶음이 남는다")
        void keepsBundleMatchedByHashtagFragment_whenKeywordIsPartial() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-search-partial");

            HashtagBundleListResponse body = fixture.hashtagBundleList(signup.accessToken(), BY_KEYWORD, "콘센트");

            assertThat(body.totalCount()).isEqualTo(1);
            assertThat(body.bundles()).singleElement().satisfies(bundle -> {
                assertThat(bundle.name()).doesNotContain("콘센트");
                assertThat(bundle.hashtags()).anyMatch(hashtag -> hashtag.contains("콘센트"));
            });
        }

        @Test
        @DisplayName("어디에도 없는 말로 검색하면 빈 목록과 totalCount 0 이 온다")
        void returnsEmptyList_whenKeywordMatchesNothing() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-search-none");

            HashtagBundleListResponse body = fixture.hashtagBundleList(signup.accessToken(), BY_KEYWORD, "없는말");

            assertThat(body.totalCount()).isZero();
            assertThat(body.bundles()).isEmpty();
            assertThat(body.hasNext()).isFalse();
        }

        @Test
        @DisplayName("keyword 를 비워 보내면 전체 목록이 내려온다")
        void returnsEveryBundle_whenKeywordIsEmpty() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-search-empty");

            HashtagBundleListResponse body = fixture.hashtagBundleList(signup.accessToken(), BY_KEYWORD, "");

            assertThat(body.totalCount())
                .isEqualTo(fixture.hashtagBundleList(signup.accessToken(), "").totalCount());
            assertThat(body.bundles()).extracting(HashtagBundleResponse::name)
                .contains("카공 카페", "이벤트/할인 홍보", "동네 고객 유입 해시태그");
        }

        @Test
        @DisplayName("공백만 보내도 전체 목록이 내려온다")
        void returnsEveryBundle_whenKeywordIsBlank() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-search-blank");

            HashtagBundleListResponse body = fixture.hashtagBundleList(signup.accessToken(), BY_KEYWORD, "  ");

            assertThat(body.totalCount())
                .isEqualTo(fixture.hashtagBundleList(signup.accessToken(), "").totalCount());
            assertThat(body.bundles()).extracting(HashtagBundleResponse::name)
                .contains("카공 카페", "이벤트/할인 홍보", "동네 고객 유입 해시태그");
        }

        @Test
        @DisplayName("검색어 앞뒤 공백은 무시하고 거른다")
        void ignoresSurroundingSpaces_whenKeywordIsPadded() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-search-padded");

            HashtagBundleListResponse body = fixture.hashtagBundleList(signup.accessToken(), BY_KEYWORD, " 콘센트 ");

            assertThat(body.totalCount()).isEqualTo(1);
            assertThat(body.bundles()).singleElement()
                .satisfies(bundle -> assertThat(bundle.name()).isEqualTo("카공 카페"));
        }

        @Test
        @DisplayName("여러 묶음이 걸리면 걸린 것만 id 역순으로 담긴다")
        void ordersMatchedBundlesByIdDescending_whenKeywordHitsSeveral() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-search-order");

            HashtagBundleListResponse body =
                fixture.hashtagBundleList(signup.accessToken(), BY_KEYWORD + "&size=50", "카페");

            assertThat(body.bundles()).hasSizeGreaterThan(1);
            assertThat(body.hasNext()).isFalse();
            assertThat(body.totalCount()).isEqualTo(body.bundles().size());
            assertThat(body.bundles()).extracting(HashtagBundleResponse::id)
                .isSortedAccordingTo(Comparator.reverseOrder());
            assertThat(body.bundles()).allSatisfy(bundle ->
                assertThat(bundle.name().contains("카페")
                    || bundle.hashtags().stream().anyMatch(hashtag -> hashtag.contains("카페"))).isTrue());
            assertThat(body.bundles()).extracting(HashtagBundleResponse::name)
                .doesNotContain("이벤트/할인 홍보");
        }

        @Test
        @DisplayName("검색 결과에도 페이징이 그대로 동작하고 totalCount 는 걸러진 개수다")
        void pagesThroughMatchedBundles_whenKeywordGiven() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-search-paged");
            HashtagBundleListResponse all =
                fixture.hashtagBundleList(signup.accessToken(), BY_KEYWORD + "&size=50", "카페");
            List<Long> matchedIds = all.bundles().stream().map(HashtagBundleResponse::id).toList();

            HashtagBundleListResponse first =
                fixture.hashtagBundleList(signup.accessToken(), BY_KEYWORD + "&size=1", "카페");
            HashtagBundleListResponse second =
                fixture.hashtagBundleList(signup.accessToken(), BY_KEYWORD + "&size=1&page=1", "카페");

            assertThat(all.hasNext()).isFalse();
            assertThat(first.totalCount()).isEqualTo(matchedIds.size());
            assertThat(first.hasNext()).isTrue();
            assertThat(first.bundles()).singleElement()
                .satisfies(bundle -> assertThat(bundle.id()).isEqualTo(matchedIds.getFirst()));
            assertThat(second.totalCount()).isEqualTo(matchedIds.size());
            assertThat(second.page()).isEqualTo(1);
            assertThat(second.bundles()).singleElement()
                .satisfies(bundle -> assertThat(bundle.id()).isEqualTo(matchedIds.get(1)));
        }

        @Test
        @DisplayName("와일드카드 문자를 쳐도 글자 그대로 찾는다")
        void treatsWildcardAsPlainText_whenKeywordHasWildcard() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-search-wildcard");

            HashtagBundleListResponse percent = fixture.hashtagBundleList(signup.accessToken(), BY_KEYWORD, "%");
            HashtagBundleListResponse underscore = fixture.hashtagBundleList(signup.accessToken(), BY_KEYWORD, "_");

            assertThat(percent.totalCount()).isZero();
            assertThat(percent.bundles()).isEmpty();
            assertThat(underscore.totalCount()).isZero();
            assertThat(underscore.bundles()).isEmpty();
        }

        @Test
        @DisplayName("태그를 담은 JSON 의 구두점은 검색 대상이 아니다")
        void ignoresJsonPunctuation_whenKeywordIsSeparator() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-search-json");

            assertThat(fixture.hashtagBundleList(signup.accessToken(), BY_KEYWORD, ",").bundles()).isEmpty();
            assertThat(fixture.hashtagBundleList(signup.accessToken(), BY_KEYWORD, "\"").bundles()).isEmpty();
            assertThat(fixture.hashtagBundleList(signup.accessToken(), BY_KEYWORD, "[").bundles()).isEmpty();
        }

        @Test
        @DisplayName("태그 경계를 넘는 말은 걸리지 않는다")
        void ignoresMatchAcrossHashtagBoundary_whenKeywordSpansTwoTags() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-search-boundary");

            HashtagBundleListResponse body =
                fixture.hashtagBundleList(signup.accessToken(), BY_KEYWORD, "카페\", \"#노트북");

            assertThat(body.totalCount()).isZero();
            assertThat(body.bundles()).isEmpty();
        }

        @Test
        @DisplayName("검색 결과에도 bookmarked 가 그대로 실린다")
        void carriesBookmarkedFlag_whenKeywordGiven() {
            SignupResponse signup = fixture.signupActiveMember("naver-hashtag-search-marked");
            HashtagBundleResponse target =
                fixture.hashtagBundleList(signup.accessToken(), BY_KEYWORD, "콘센트").bundles().getFirst();
            fixture.bookmarkedHashtagBundle(signup.accessToken(), target.id());

            HashtagBundleListResponse body = fixture.hashtagBundleList(signup.accessToken(), BY_KEYWORD, "콘센트");

            assertThat(body.bundles()).singleElement()
                .satisfies(bundle -> assertThat(bundle.bookmarked()).isTrue());
        }
    }
}
