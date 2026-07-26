package com.ssoss.ssossbackend.content.entrypoint.controller;

import com.ssoss.ssossbackend.content.application.result.ContentSaveResult;
import com.ssoss.ssossbackend.content.application.service.ContentService;
import com.ssoss.ssossbackend.content.entrypoint.request.ContentSaveRequest;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentItemResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentSaveResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
            .body(new ContentSaveResponse(result.contents().stream()
                .map(content -> new ContentItemResponse(
                    content.contentId(), content.generationResultId(), content.channel()))
                .toList()));
    }
}
