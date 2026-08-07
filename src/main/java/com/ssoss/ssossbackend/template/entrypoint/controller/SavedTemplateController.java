package com.ssoss.ssossbackend.template.entrypoint.controller;

import com.ssoss.ssossbackend.template.application.result.SavedTemplateSaveResult;
import com.ssoss.ssossbackend.template.application.service.SavedTemplateService;
import com.ssoss.ssossbackend.template.entrypoint.request.SavedTemplateSaveRequest;
import com.ssoss.ssossbackend.template.entrypoint.response.SavedTemplateSaveResponse;

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
}
