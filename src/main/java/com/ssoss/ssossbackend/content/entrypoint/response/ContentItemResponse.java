package com.ssoss.ssossbackend.content.entrypoint.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "저장된 콘텐츠 1건")
public record ContentItemResponse(
    @Schema(description = "콘텐츠 id — 상세 조회·편집·채널 변환에 쓰입니다", example = "1")
    Long contentId,
    @Schema(description = "복사해 온 원본 생성 결과 id", example = "10")
    Long generationResultId,
    @Schema(description = "채널", allowableValues = {"BLOG", "INSTAGRAM", "DAANGN_BIZ", "THREADS"}, example = "BLOG")
    String channel
) {
}
