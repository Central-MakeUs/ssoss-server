package com.ssoss.ssossbackend.content.entrypoint.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "생성 작업 생성 응답 — 폴링에 쓸 작업 id")
public record GenerationStartResponse(
    @Schema(description = "생성 작업 id", example = "1")
    Long generationId
) {
}
