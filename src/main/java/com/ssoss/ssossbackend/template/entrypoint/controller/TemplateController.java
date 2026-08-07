package com.ssoss.ssossbackend.template.entrypoint.controller;

import com.ssoss.ssossbackend.shared.paging.PagedResult;
import com.ssoss.ssossbackend.template.application.result.TemplateAppliedResult;
import com.ssoss.ssossbackend.template.application.result.TemplateDetailResult;
import com.ssoss.ssossbackend.template.application.result.TemplateResult;
import com.ssoss.ssossbackend.template.application.service.TemplateService;
import com.ssoss.ssossbackend.template.entrypoint.request.TemplateListRequest;
import com.ssoss.ssossbackend.template.entrypoint.response.TemplateAppliedResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.TemplateDetailResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.TemplateListResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.TemplateResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class TemplateController implements TemplateApi {

    private final TemplateService templateService;

    @Override
    @GetMapping("/v1/templates")
    public TemplateListResponse list(@Valid @ParameterObject TemplateListRequest request) {
        PagedResult<TemplateResult> result = templateService.list(request.toCommand());
        return new TemplateListResponse(result.totalCount(), result.page(), result.size(), result.hasNext(),
            result.items().stream()
                .map(template -> new TemplateResponse(template.id(), template.category(), template.title(),
                    template.description(), template.recommendedChannels(), template.bookmarked()))
                .toList());
    }

    @Override
    @GetMapping("/v1/templates/{templateId}")
    public TemplateDetailResponse getById(@PathVariable Long templateId) {
        TemplateDetailResult result = templateService.getById(templateId);
        return new TemplateDetailResponse(result.id(), result.category(), result.title(), result.description(),
            result.body(), result.exampleBody(), result.recommendedChannels(), result.bookmarked());
    }

    @Override
    @GetMapping("/v1/templates/{templateId}/applied")
    public TemplateAppliedResponse getApplied(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long templateId
    ) {
        TemplateAppliedResult result = templateService.apply(templateId, memberId);
        return new TemplateAppliedResponse(result.id(), result.body());
    }
}
