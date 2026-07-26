package com.ssoss.ssossbackend.content.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "저장하기 응답 — 콘텐츠 1건과 그 안의 채널별 콘텐츠")
public record ContentSaveResponse(
    @Schema(description = "콘텐츠 id — 상세 조회에 쓰입니다", example = "1")
    Long contentId,
    @Schema(description = "저장된 채널별 콘텐츠. 작업에서 성공한 채널 수만큼 담깁니다")
    List<ContentChannelSummaryResponse> contents
) {
}
