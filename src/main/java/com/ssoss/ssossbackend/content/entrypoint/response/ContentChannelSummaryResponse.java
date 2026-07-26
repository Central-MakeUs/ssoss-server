package com.ssoss.ssossbackend.content.entrypoint.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "저장된 채널별 콘텐츠 1건 — 상세 조회의 contents 항목에서 제목·본문·해시태그까지 받습니다")
public record ContentChannelSummaryResponse(
    @Schema(description = "채널별 콘텐츠 id — 편집·삭제·채널 변환에 쓰입니다", example = "10")
    Long contentChannelId,
    @Schema(description = "채널", allowableValues = {"BLOG", "INSTAGRAM", "DAANGN_BIZ", "THREADS"}, example = "BLOG")
    String channel
) {
}
