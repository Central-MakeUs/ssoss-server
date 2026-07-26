package com.ssoss.ssossbackend.content.entrypoint.controller;

import com.ssoss.ssossbackend.content.application.result.ContentChannelResult;
import com.ssoss.ssossbackend.content.application.result.ContentDetailResult;
import com.ssoss.ssossbackend.content.application.result.ContentListResult;
import com.ssoss.ssossbackend.content.application.result.ContentSaveResult;
import com.ssoss.ssossbackend.content.application.service.ContentService;
import com.ssoss.ssossbackend.content.entrypoint.request.ContentChannelEditRequest;
import com.ssoss.ssossbackend.content.entrypoint.request.ContentListRequest;
import com.ssoss.ssossbackend.content.entrypoint.request.ContentSaveRequest;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentChannelResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentChannelSummaryResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentDetailResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentListResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentSaveResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentSummaryResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class ContentController implements ContentApi {

    private final ContentService contentService;

    @Override
    @PostMapping("/v1/contents")
    public ResponseEntity<ContentSaveResponse> save(
        @AuthenticationPrincipal Long memberId,
        @Valid @RequestBody ContentSaveRequest request
    ) {
        ContentSaveResult result = contentService.save(request.toCommand(memberId));
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ContentSaveResponse(result.contentId(), result.contents().stream()
                .map(content -> new ContentChannelSummaryResponse(content.contentChannelId(), content.channel()))
                .toList()));
    }

    @Override
    @GetMapping("/v1/contents")
    public ContentListResponse list(
        @AuthenticationPrincipal Long memberId,
        @Valid @ParameterObject ContentListRequest request
    ) {
        ContentListResult result = contentService.list(request.toCommand(memberId));
        return new ContentListResponse(result.totalCount(), result.page(), result.size(), result.hasNext(),
            result.contents().stream()
                .map(content -> new ContentSummaryResponse(content.contentId(), content.savedAt(),
                    content.channels(), content.purpose(), content.tone(), content.title(), content.hashtags()))
                .toList());
    }

    @Override
    @GetMapping("/v1/contents/{contentId}")
    public ContentDetailResponse getById(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long contentId
    ) {
        ContentDetailResult result = contentService.getById(contentId, memberId);
        return new ContentDetailResponse(
            result.contentId(),
            result.purpose(),
            result.tone(),
            result.keywords(),
            result.contents().stream()
                .map(content -> new ContentChannelResponse(content.contentChannelId(), content.channel(),
                    content.title(), content.body(), content.hashtags()))
                .toList());
    }

    @Override
    @DeleteMapping("/v1/contents/{contentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long contentId
    ) {
        contentService.delete(contentId, memberId);
    }

    @Override
    @PutMapping("/v1/contents/{contentId}/channels/{contentChannelId}")
    public ContentChannelResponse edit(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long contentId,
        @PathVariable Long contentChannelId,
        @Valid @RequestBody ContentChannelEditRequest request
    ) {
        ContentChannelResult result = contentService.edit(request.toCommand(memberId, contentId, contentChannelId));
        return new ContentChannelResponse(result.contentChannelId(), result.channel(), result.title(),
            result.body(), result.hashtags());
    }
}
