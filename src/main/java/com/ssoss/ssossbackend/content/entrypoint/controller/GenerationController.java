package com.ssoss.ssossbackend.content.entrypoint.controller;

import java.net.URI;

import com.ssoss.ssossbackend.content.application.result.GenerationPollResult;
import com.ssoss.ssossbackend.content.application.result.GenerationStartResult;
import com.ssoss.ssossbackend.content.application.service.GenerationService;
import com.ssoss.ssossbackend.content.entrypoint.request.GenerationStartRequest;
import com.ssoss.ssossbackend.content.entrypoint.response.GenerationChannelResultResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.GenerationPollResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.GenerationStartResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Override
    @GetMapping("/v1/generations/{generationId}")
    public GenerationPollResponse poll(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long generationId
    ) {
        GenerationPollResult result = generationService.poll(generationId, memberId);
        return new GenerationPollResponse(
            result.generationId(),
            result.status(),
            result.failureReason(),
            result.results().stream()
                .map(channelResult -> new GenerationChannelResultResponse(channelResult.channel(),
                    channelResult.title(), channelResult.body(), channelResult.hashtags()))
                .toList(),
            result.pendingChannels());
    }
}
