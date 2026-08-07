package com.ssoss.ssossbackend.template.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "저장한 템플릿 목록 응답")
public record SavedTemplateListResponse(
    @Schema(description = "저장한 템플릿 전체 건수", example = "2")
    long totalCount,
    @Schema(description = "현재 페이지 번호 — 0 부터 셉니다", example = "0")
    int page,
    @Schema(description = "페이지 크기", example = "20")
    int size,
    @Schema(description = "다음 페이지가 있는지 여부", example = "false")
    boolean hasNext,
    @Schema(description = "고른 정렬 방향의 카드 목록")
    List<SavedTemplateSummaryResponse> savedTemplates
) {
}
