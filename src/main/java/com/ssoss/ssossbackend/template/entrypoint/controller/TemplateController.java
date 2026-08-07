package com.ssoss.ssossbackend.template.entrypoint.controller;

import com.ssoss.ssossbackend.shared.paging.PagedResult;
import com.ssoss.ssossbackend.template.application.result.TemplateResult;
import com.ssoss.ssossbackend.template.application.service.TemplateService;
import com.ssoss.ssossbackend.template.entrypoint.request.TemplateListRequest;
import com.ssoss.ssossbackend.template.entrypoint.response.TemplateListResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.TemplateResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
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
}
