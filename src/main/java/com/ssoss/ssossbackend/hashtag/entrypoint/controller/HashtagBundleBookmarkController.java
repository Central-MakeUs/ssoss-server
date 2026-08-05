package com.ssoss.ssossbackend.hashtag.entrypoint.controller;

import com.ssoss.ssossbackend.hashtag.application.service.HashtagBundleService;
import com.ssoss.ssossbackend.hashtag.entrypoint.response.BookmarkedHashtagBundleListResponse;
import com.ssoss.ssossbackend.hashtag.entrypoint.response.HashtagBundleResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class HashtagBundleBookmarkController implements HashtagBundleBookmarkApi {

    private final HashtagBundleService hashtagBundleService;

    @Override
    @GetMapping("/v1/members/me/hashtag-bundles")
    public BookmarkedHashtagBundleListResponse listBookmarked(@AuthenticationPrincipal Long memberId) {
        return new BookmarkedHashtagBundleListResponse(hashtagBundleService.listBookmarked(memberId).stream()
            .map(bundle -> new HashtagBundleResponse(bundle.id(), bundle.name(), bundle.hashtags()))
            .toList());
    }

    @Override
    @PutMapping("/v1/members/me/hashtag-bundles/{bundleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bookmark(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long bundleId
    ) {
        hashtagBundleService.bookmark(memberId, bundleId);
    }
}
