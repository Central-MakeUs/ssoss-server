package com.ssoss.ssossbackend.hashtag.entrypoint.controller;

import java.util.List;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.hashtag.domain.model.HashtagErrorCode;
import com.ssoss.ssossbackend.hashtag.entrypoint.response.BookmarkedHashtagBundleListResponse;
import com.ssoss.ssossbackend.hashtag.entrypoint.response.HashtagBundleResponse;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("해시태그 묶음 북마크 API")
class HashtagBundleBookmarkApiTest extends IntegrationTest {

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
        @DisplayName("여러 묶음을 북마크하면 내 목록에 모두 담긴다")
        void addsEveryBookmarkedBundle_whenSeveralBookmarked() {
            SignupResponse signup = fixture.signupActiveMember("naver-bookmark-several");
            List<HashtagBundleResponse> bundles = fixture.hashtagBundleList(signup.accessToken(), "").bundles();
            Long first = bundles.get(0).id();
            Long second = bundles.get(1).id();

            fixture.bookmarkedHashtagBundle(signup.accessToken(), first);
            fixture.bookmarkedHashtagBundle(signup.accessToken(), second);

            BookmarkedHashtagBundleListResponse body = fixture.bookmarkedHashtagBundleList(signup.accessToken());
            assertThat(body.bundles()).extracting(HashtagBundleResponse::id)
                .containsExactly(first, second);
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
}
