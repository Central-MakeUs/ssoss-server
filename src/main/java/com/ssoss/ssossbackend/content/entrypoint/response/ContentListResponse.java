package com.ssoss.ssossbackend.content.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "생성 기록 목록 응답")
public record ContentListResponse(
    @Schema(description = "저장한 콘텐츠 전체 건수 — 화면 상단의 건수입니다", example = "3")
    long totalCount,
    @Schema(description = "현재 페이지 번호 — 0 부터 셉니다", example = "0")
    int page,
    @Schema(description = "페이지 크기", example = "20")
    int size,
    @Schema(description = "다음 페이지가 있는지 여부", example = "false")
    boolean hasNext,
    @Schema(description = "저장 시각 최신순 카드 목록")
    List<ContentSummaryResponse> contents
) {
}
