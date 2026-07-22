package com.ssoss.ssossbackend.content.entrypoint.controller;

import java.net.URI;

import com.ssoss.ssossbackend.content.application.result.GenerationStartResult;
import com.ssoss.ssossbackend.content.application.service.GenerationService;
import com.ssoss.ssossbackend.content.entrypoint.request.GenerationStartRequest;
import com.ssoss.ssossbackend.content.entrypoint.response.GenerationStartResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class GenerationController implements GenerationApi {

    private final GenerationService generationService;

    @Override
    @PostMapping("/v1/generations")
    public ResponseEntity<GenerationStartResponse> start(
        @AuthenticationPrincipal Long memberId,
        @Valid @RequestBody GenerationStartRequest request
    ) {
        GenerationStartResult result = generationService.start(request.toCommand(memberId));
        return ResponseEntity.created(URI.create("/v1/generations/" + result.generationId()))
            .body(new GenerationStartResponse(result.generationId()));
    }
}
