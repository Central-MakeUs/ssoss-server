package com.ssoss.ssossbackend.content.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채널별 생성 결과")
public record GenerationChannelResultResponse(
    @Schema(description = "채널", allowableValues = {"BLOG", "INSTAGRAM", "DAANGN_BIZ", "THREADS"}, example = "BLOG")
    String channel,
    @Schema(description = "제목 — 블로그만 있고 나머지 채널은 null 입니다", example = "주말엔 아메리카노 1+1, 놓치면 아쉬운 이벤트")
    String title,
    @Schema(description = "본문", example = "이번 주말, 매장에서 아메리카노 1+1 이벤트를 진행합니다...")
    String body,
    @Schema(description = "해시태그 목록", example = "[\"#카페이벤트\", \"#아메리카노\"]")
    List<String> hashtags
) {
}
