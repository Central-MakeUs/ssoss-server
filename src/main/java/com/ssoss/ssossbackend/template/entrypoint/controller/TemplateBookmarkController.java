package com.ssoss.ssossbackend.template.entrypoint.controller;

import com.ssoss.ssossbackend.template.application.service.TemplateService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class TemplateBookmarkController implements TemplateBookmarkApi {

    private final TemplateService templateService;

    @Override
    @PutMapping("/v1/members/me/templates/{templateId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bookmark(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long templateId
    ) {
        templateService.bookmark(memberId, templateId);
    }

    @Override
    @DeleteMapping("/v1/members/me/templates/{templateId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unbookmark(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long templateId
    ) {
        templateService.unbookmark(memberId, templateId);
    }
}
