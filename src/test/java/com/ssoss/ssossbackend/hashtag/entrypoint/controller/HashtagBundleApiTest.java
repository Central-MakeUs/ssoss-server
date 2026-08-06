package com.ssoss.ssossbackend.hashtag.entrypoint.controller;

import java.util.Comparator;

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

@DisplayName("해시태그 묶음 카탈로그 API")
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
}
