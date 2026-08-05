package com.ssoss.ssossbackend.hashtag.entrypoint.controller;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.hashtag.entrypoint.response.BookmarkedHashtagBundleListResponse;
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
}
