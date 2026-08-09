package com.ssoss.ssossbackend.template.entrypoint.controller;

import com.ssoss.ssossbackend.shared.paging.PagedResult;
import com.ssoss.ssossbackend.template.application.result.SavedTemplateDetailResult;
import com.ssoss.ssossbackend.template.application.result.SavedTemplateSaveResult;
import com.ssoss.ssossbackend.template.application.result.SavedTemplateSummaryResult;
import com.ssoss.ssossbackend.template.application.service.SavedTemplateService;
import com.ssoss.ssossbackend.template.entrypoint.request.SavedTemplateEditRequest;
import com.ssoss.ssossbackend.template.entrypoint.request.SavedTemplateListRequest;
import com.ssoss.ssossbackend.template.entrypoint.request.SavedTemplateRenameRequest;
import com.ssoss.ssossbackend.template.entrypoint.request.SavedTemplateSaveRequest;
import com.ssoss.ssossbackend.template.entrypoint.response.SavedTemplateDetailResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.SavedTemplateListResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.SavedTemplateSaveResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.SavedTemplateSummaryResponse;

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
class SavedTemplateController implements SavedTemplateApi {

    private final SavedTemplateService savedTemplateService;

    @Override
    @PostMapping("/v1/saved-templates")
    public ResponseEntity<SavedTemplateSaveResponse> save(
        @AuthenticationPrincipal Long memberId,
        @Valid @RequestBody SavedTemplateSaveRequest request
    ) {
        SavedTemplateSaveResult result = savedTemplateService.save(request.toCommand(memberId));
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new SavedTemplateSaveResponse(result.savedTemplateId()));
    }

    @Override
    @GetMapping("/v1/saved-templates")
    public SavedTemplateListResponse list(
        @AuthenticationPrincipal Long memberId,
        @Valid @ParameterObject SavedTemplateListRequest request
    ) {
        PagedResult<SavedTemplateSummaryResult> result = savedTemplateService.list(request.toCommand(memberId));
        return new SavedTemplateListResponse(result.totalCount(), result.page(), result.size(), result.hasNext(),
            result.items().stream()
                .map(saved -> new SavedTemplateSummaryResponse(saved.savedTemplateId(), saved.category(),
                    saved.title(), saved.description(), saved.savedAt()))
                .toList());
    }

    @Override
    @GetMapping("/v1/saved-templates/{savedTemplateId}")
    public SavedTemplateDetailResponse getById(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long savedTemplateId
    ) {
        SavedTemplateDetailResult result = savedTemplateService.getById(savedTemplateId, memberId);
        return new SavedTemplateDetailResponse(result.savedTemplateId(), result.category(), result.title(),
            result.description(), result.body(), result.recommendedChannels(), result.savedAt());
    }

    @Override
    @PutMapping("/v1/saved-templates/{savedTemplateId}")
    public SavedTemplateDetailResponse edit(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long savedTemplateId,
        @Valid @RequestBody SavedTemplateEditRequest request
    ) {
        SavedTemplateDetailResult result = savedTemplateService.edit(request.toCommand(memberId, savedTemplateId));
        return new SavedTemplateDetailResponse(result.savedTemplateId(), result.category(), result.title(),
            result.description(), result.body(), result.recommendedChannels(), result.savedAt());
    }

    @Override
    @PutMapping("/v1/saved-templates/{savedTemplateId}/title")
    public SavedTemplateDetailResponse rename(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long savedTemplateId,
        @Valid @RequestBody SavedTemplateRenameRequest request
    ) {
        SavedTemplateDetailResult result = savedTemplateService.rename(request.toCommand(memberId, savedTemplateId));
        return new SavedTemplateDetailResponse(result.savedTemplateId(), result.category(), result.title(),
            result.description(), result.body(), result.recommendedChannels(), result.savedAt());
    }

    @Override
    @DeleteMapping("/v1/saved-templates/{savedTemplateId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long savedTemplateId
    ) {
        savedTemplateService.delete(savedTemplateId, memberId);
    }
}
