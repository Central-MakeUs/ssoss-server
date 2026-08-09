package com.ssoss.ssossbackend.content.entrypoint.controller;

import java.net.URI;

import com.ssoss.ssossbackend.content.application.result.GenerationStartResult;
import com.ssoss.ssossbackend.content.application.service.GenerationService;
import com.ssoss.ssossbackend.content.entrypoint.request.ChannelConversionRequest;
import com.ssoss.ssossbackend.content.entrypoint.response.GenerationStartResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class ChannelConversionController implements ChannelConversionApi {

    private final GenerationService generationService;

    @Override
    @PostMapping("/v1/contents/{contentId}/channels/{contentChannelId}/conversions")
    public ResponseEntity<GenerationStartResponse> convert(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long contentId,
        @PathVariable Long contentChannelId,
        @Valid @RequestBody ChannelConversionRequest request
    ) {
        GenerationStartResult result = generationService.convert(
            request.toCommand(memberId, contentId, contentChannelId));
        return ResponseEntity.created(URI.create("/v1/generations/" + result.generationId()))
            .body(new GenerationStartResponse(result.generationId()));
    }
}
