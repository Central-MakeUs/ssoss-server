package com.ssoss.ssossbackend.hashtag.entrypoint.controller;

import com.ssoss.ssossbackend.hashtag.application.result.HashtagBundleResult;
import com.ssoss.ssossbackend.hashtag.application.service.HashtagBundleService;
import com.ssoss.ssossbackend.hashtag.entrypoint.request.HashtagBundleListRequest;
import com.ssoss.ssossbackend.hashtag.entrypoint.response.HashtagBundleListResponse;
import com.ssoss.ssossbackend.hashtag.entrypoint.response.HashtagBundleResponse;
import com.ssoss.ssossbackend.shared.paging.PagedResult;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class HashtagBundleController implements HashtagBundleApi {

    private final HashtagBundleService hashtagBundleService;

    @Override
    @GetMapping("/v1/hashtag-bundles")
    public HashtagBundleListResponse list(
        @AuthenticationPrincipal Long memberId,
        @Valid @ParameterObject HashtagBundleListRequest request
    ) {
        PagedResult<HashtagBundleResult> result = hashtagBundleService.list(request.toCommand(memberId));
        return new HashtagBundleListResponse(result.totalCount(), result.page(), result.size(), result.hasNext(),
            result.items().stream()
                .map(bundle -> new HashtagBundleResponse(bundle.id(), bundle.name(), bundle.hashtags(),
                    bundle.bookmarked()))
                .toList());
    }
}
