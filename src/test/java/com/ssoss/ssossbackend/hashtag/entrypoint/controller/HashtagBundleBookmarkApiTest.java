package com.ssoss.ssossbackend.hashtag.entrypoint.controller;

import java.time.Duration;
import java.util.List;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.hashtag.domain.contract.HashtagBundleBookmarkRepository;
import com.ssoss.ssossbackend.hashtag.domain.model.HashtagBundleBookmark;
import com.ssoss.ssossbackend.hashtag.domain.model.HashtagErrorCode;
import com.ssoss.ssossbackend.hashtag.entrypoint.response.BookmarkedHashtagBundleListResponse;
import com.ssoss.ssossbackend.hashtag.entrypoint.response.BookmarkedHashtagBundleResponse;
import com.ssoss.ssossbackend.hashtag.entrypoint.response.HashtagBundleResponse;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("해시태그 묶음 북마크 API")
class HashtagBundleBookmarkApiTest extends IntegrationTest {

    @Autowired
    private HashtagBundleBookmarkRepository hashtagBundleBookmarkRepository;

    @Nested
    @DisplayName("GET /v1/members/me/hashtag-bundles")
    class ListBookmarkedBundles {

        @Test
        @DisplayName("북마크한 묶음이 없으면 빈 목록이 담긴다")
        void returnsEmptyBundles_whenNothingBookmarked() {
            SignupResponse signup = fixture.signupActiveMember("naver-bookmark-empty");

            BookmarkedHashtagBundleListResponse body = fixture.bookmarkedHashtagBundleList(signup.accessToken());

            assertThat(body.bundles()).isEmpty();
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 조회하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenQueries() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-bookmark-pending");

            fixture.getBookmarkedHashtagBundles(login.accessToken())
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 조회하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            client().get().uri("/v1/members/me/hashtag-bundles")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }

    @Nested
    @DisplayName("PUT /v1/members/me/hashtag-bundles/{bundleId}")
    class BookmarkBundle {

        @Test
        @DisplayName("가입 회원이 묶음을 북마크하면 204 를 반환하고 내 목록에 담긴다")
        void addsBundleToMyList_whenActiveMemberBookmarks() {
            SignupResponse signup = fixture.signupActiveMember("naver-bookmark-on");
            HashtagBundleResponse bundle = fixture.hashtagBundleList(signup.accessToken(), "").bundles().getFirst();

            fixture.bookmarkHashtagBundle(signup.accessToken(), bundle.id())
                .expectStatus().isNoContent();

            BookmarkedHashtagBundleListResponse body = fixture.bookmarkedHashtagBundleList(signup.accessToken());
            assertThat(body.bundles()).singleElement().satisfies(bookmarked -> {
                assertThat(bookmarked.id()).isEqualTo(bundle.id());
                assertThat(bookmarked.name()).isEqualTo(bundle.name());
                assertThat(bookmarked.hashtags()).isEqualTo(bundle.hashtags());
            });
        }

        @Test
        @DisplayName("이미 북마크한 묶음을 또 북마크해도 204 를 반환하고 내 목록에 하나만 남는다")
        void keepsSingleEntry_whenBookmarkedTwice() {
            SignupResponse signup = fixture.signupActiveMember("naver-bookmark-twice");
            HashtagBundleResponse bundle = fixture.hashtagBundleList(signup.accessToken(), "").bundles().getFirst();
            fixture.bookmarkedHashtagBundle(signup.accessToken(), bundle.id());

            fixture.bookmarkHashtagBundle(signup.accessToken(), bundle.id())
                .expectStatus().isNoContent();

            BookmarkedHashtagBundleListResponse body = fixture.bookmarkedHashtagBundleList(signup.accessToken());
            assertThat(body.bundles()).singleElement()
                .satisfies(bookmarked -> assertThat(bookmarked.id()).isEqualTo(bundle.id()));
        }

        @Test
        @DisplayName("여러 묶음을 북마크하면 최근에 담은 묶음부터 담긴다")
        void addsLatestBookmarkedBundleFirst_whenSeveralBookmarked() {
            SignupResponse signup = fixture.signupActiveMember("naver-bookmark-several");
            List<HashtagBundleResponse> bundles = fixture.hashtagBundleList(signup.accessToken(), "").bundles();
            Long earlier = bundles.get(0).id();
            Long later = bundles.get(1).id();

            fixture.bookmarkedHashtagBundle(signup.accessToken(), earlier);
            clock.advanceBy(Duration.ofMinutes(1));
            fixture.bookmarkedHashtagBundle(signup.accessToken(), later);

            BookmarkedHashtagBundleListResponse body = fixture.bookmarkedHashtagBundleList(signup.accessToken());
            assertThat(body.bundles()).extracting(BookmarkedHashtagBundleResponse::id)
                .containsExactly(later, earlier);
        }

        @Test
        @DisplayName("없는 묶음을 북마크하면 404 와 HT0001 을 반환한다")
        void returns404_whenBundleMissing() {
            SignupResponse signup = fixture.signupActiveMember("naver-bookmark-missing");

            fixture.bookmarkHashtagBundle(signup.accessToken(), 999_999_999L)
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code())
                    .isEqualTo(HashtagErrorCode.HASHTAG_BUNDLE_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("다른 회원이 묶음을 북마크해도 내 목록에는 담기지 않는다")
        void excludesOtherMembersBookmark_whenActiveMemberQueries() {
            SignupResponse other = fixture.signupActiveMember("naver-bookmark-other");
            SignupResponse mine = fixture.signupActiveMember("naver-bookmark-mine");
            HashtagBundleResponse bundle = fixture.hashtagBundleList(other.accessToken(), "").bundles().getFirst();
            fixture.bookmarkedHashtagBundle(other.accessToken(), bundle.id());

            BookmarkedHashtagBundleListResponse body = fixture.bookmarkedHashtagBundleList(mine.accessToken());

            assertThat(body.bundles()).isEmpty();
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 북마크하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenBookmarks() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-bookmark-pending-put");

            fixture.bookmarkHashtagBundle(login.accessToken(), 1L)
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 북마크하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            client().put().uri("/v1/members/me/hashtag-bundles/1")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }

    @Nested
    @DisplayName("DELETE /v1/members/me/hashtag-bundles/{bundleId}")
    class UnbookmarkBundle {

        @Test
        @DisplayName("가입 회원이 북마크를 해제하면 204 를 반환하고 내 목록에서 사라진다")
        void removesBundleFromMyList_whenActiveMemberUnbookmarks() {
            SignupResponse signup = fixture.signupActiveMember("naver-unbookmark-off");
            HashtagBundleResponse bundle = fixture.hashtagBundleList(signup.accessToken(), "").bundles().getFirst();
            fixture.bookmarkedHashtagBundle(signup.accessToken(), bundle.id());

            fixture.unbookmarkHashtagBundle(signup.accessToken(), bundle.id())
                .expectStatus().isNoContent();

            BookmarkedHashtagBundleListResponse body = fixture.bookmarkedHashtagBundleList(signup.accessToken());
            assertThat(body.bundles()).isEmpty();
        }

        @Test
        @DisplayName("북마크를 해제해도 행은 남고 북마크한 시각만 비워진다")
        void keepsRowWithClearedBookmarkedAt_whenUnbookmarked() {
            SignupResponse signup = fixture.signupActiveMember("naver-unbookmark-row");
            HashtagBundleResponse bundle = fixture.hashtagBundleList(signup.accessToken(), "").bundles().getFirst();
            fixture.bookmarkedHashtagBundle(signup.accessToken(), bundle.id());

            fixture.unbookmarkedHashtagBundle(signup.accessToken(), bundle.id());

            assertThat(hashtagBundleBookmarkRepository
                .findByMemberIdAndBundleId(database.memberIdOf("naver-unbookmark-row"), bundle.id()))
                .get()
                .extracting(HashtagBundleBookmark::getBookmarkedAt)
                .isNull();
        }

        @Test
        @DisplayName("해제한 묶음을 다시 북마크하면 내 목록에 다시 담긴다")
        void addsBundleAgain_whenBookmarkedAfterUnbookmark() {
            SignupResponse signup = fixture.signupActiveMember("naver-unbookmark-again");
            HashtagBundleResponse bundle = fixture.hashtagBundleList(signup.accessToken(), "").bundles().getFirst();
            fixture.bookmarkedHashtagBundle(signup.accessToken(), bundle.id());
            fixture.unbookmarkedHashtagBundle(signup.accessToken(), bundle.id());

            fixture.bookmarkHashtagBundle(signup.accessToken(), bundle.id())
                .expectStatus().isNoContent();

            BookmarkedHashtagBundleListResponse body = fixture.bookmarkedHashtagBundleList(signup.accessToken());
            assertThat(body.bundles()).singleElement()
                .satisfies(bookmarked -> assertThat(bookmarked.id()).isEqualTo(bundle.id()));
        }

        @Test
        @DisplayName("해제한 묶음을 다시 담으면 목록 맨 앞으로 온다")
        void movesBundleToFront_whenBookmarkedAgainAfterUnbookmark() {
            SignupResponse signup = fixture.signupActiveMember("naver-unbookmark-front");
            List<HashtagBundleResponse> bundles = fixture.hashtagBundleList(signup.accessToken(), "").bundles();
            Long earlier = bundles.get(1).id();
            Long later = bundles.get(0).id();
            fixture.bookmarkedHashtagBundle(signup.accessToken(), earlier);
            clock.advanceBy(Duration.ofMinutes(1));
            fixture.bookmarkedHashtagBundle(signup.accessToken(), later);

            fixture.unbookmarkedHashtagBundle(signup.accessToken(), earlier);
            clock.advanceBy(Duration.ofMinutes(1));
            fixture.bookmarkedHashtagBundle(signup.accessToken(), earlier);

            BookmarkedHashtagBundleListResponse body = fixture.bookmarkedHashtagBundleList(signup.accessToken());
            assertThat(body.bundles()).extracting(BookmarkedHashtagBundleResponse::id)
                .containsExactly(earlier, later);
        }

        @Test
        @DisplayName("이미 해제한 북마크를 또 해제해도 204 를 반환한다")
        void returns204_whenUnbookmarkedTwice() {
            SignupResponse signup = fixture.signupActiveMember("naver-unbookmark-twice");
            HashtagBundleResponse bundle = fixture.hashtagBundleList(signup.accessToken(), "").bundles().getFirst();
            fixture.bookmarkedHashtagBundle(signup.accessToken(), bundle.id());
            fixture.unbookmarkedHashtagBundle(signup.accessToken(), bundle.id());

            fixture.unbookmarkHashtagBundle(signup.accessToken(), bundle.id())
                .expectStatus().isNoContent();
        }

        @Test
        @DisplayName("북마크한 적 없는 묶음을 해제하면 204 를 반환하고 행이 새로 생기지 않는다")
        void createsNoRow_whenNeverBookmarkedBundleUnbookmarked() {
            SignupResponse signup = fixture.signupActiveMember("naver-unbookmark-never");
            HashtagBundleResponse bundle = fixture.hashtagBundleList(signup.accessToken(), "").bundles().getFirst();

            fixture.unbookmarkHashtagBundle(signup.accessToken(), bundle.id())
                .expectStatus().isNoContent();

            assertThat(hashtagBundleBookmarkRepository
                .findByMemberIdAndBundleId(database.memberIdOf("naver-unbookmark-never"), bundle.id()))
                .isEmpty();
        }

        @Test
        @DisplayName("없는 묶음을 해제해도 204 를 반환한다")
        void returns204_whenBundleMissing() {
            SignupResponse signup = fixture.signupActiveMember("naver-unbookmark-missing");

            fixture.unbookmarkHashtagBundle(signup.accessToken(), 999_999_999L)
                .expectStatus().isNoContent();
        }

        @Test
        @DisplayName("내가 북마크를 해제해도 다른 회원의 같은 묶음 북마크는 남는다")
        void keepsOtherMembersBookmark_whenActiveMemberUnbookmarks() {
            SignupResponse other = fixture.signupActiveMember("naver-unbookmark-other");
            SignupResponse mine = fixture.signupActiveMember("naver-unbookmark-mine");
            HashtagBundleResponse bundle = fixture.hashtagBundleList(mine.accessToken(), "").bundles().getFirst();
            fixture.bookmarkedHashtagBundle(other.accessToken(), bundle.id());
            fixture.bookmarkedHashtagBundle(mine.accessToken(), bundle.id());

            fixture.unbookmarkedHashtagBundle(mine.accessToken(), bundle.id());

            BookmarkedHashtagBundleListResponse body = fixture.bookmarkedHashtagBundleList(other.accessToken());
            assertThat(body.bundles()).singleElement()
                .satisfies(bookmarked -> assertThat(bookmarked.id()).isEqualTo(bundle.id()));
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 해제하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenUnbookmarks() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-unbookmark-pending");

            fixture.unbookmarkHashtagBundle(login.accessToken(), 1L)
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 해제하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            client().delete().uri("/v1/members/me/hashtag-bundles/1")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }
}
