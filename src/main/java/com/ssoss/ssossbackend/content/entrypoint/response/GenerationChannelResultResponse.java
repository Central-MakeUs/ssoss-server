package com.ssoss.ssossbackend.content.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채널별 생성 결과")
public record GenerationChannelResultResponse(
    @Schema(description = "채널", allowableValues = {"BLOG", "INSTAGRAM", "DAANGN_BIZ", "THREADS"}, example = "BLOG")
    String channel,
    @Schema(description = "채널 상태 — PENDING: 생성 중, SUCCEEDED: 성공, FAILED: 실패",
        allowableValues = {"PENDING", "SUCCEEDED", "FAILED"}, example = "SUCCEEDED")
    String status,
    @Schema(description = "채널 상태를 사용자에게 보여줄 문구. 상태와 무관하게 항상 값이 있습니다",
        example = "생성에 성공했습니다")
    String message,
    @Schema(description = "제목 — 성공한 블로그만 있고 나머지는 null 입니다",
        example = "주말엔 아메리카노 1+1, 놓치면 아쉬운 이벤트", nullable = true)
    String title,
    @Schema(description = "본문 — 성공일 때만 값이 있습니다",
        example = "이번 주말, 매장에서 아메리카노 1+1 이벤트를 진행합니다...", nullable = true)
    String body,
    @Schema(description = "해시태그 목록 — 성공이 아니면 빈 배열입니다", example = "[\"#카페이벤트\", \"#아메리카노\"]")
    List<String> hashtags
) {
}
