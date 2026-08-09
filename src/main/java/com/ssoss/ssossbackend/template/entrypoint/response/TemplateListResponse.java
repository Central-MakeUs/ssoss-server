package com.ssoss.ssossbackend.template.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "추천 템플릿 목록 응답")
public record TemplateListResponse(
    @Schema(description = "조건에 해당하는 템플릿 개수", example = "8")
    long totalCount,
    @Schema(description = "현재 페이지 번호 — 0 부터 셉니다", example = "0")
    int page,
    @Schema(description = "페이지 크기", example = "20")
    int size,
    @Schema(description = "다음 페이지가 있는지 여부", example = "false")
    boolean hasNext,
    @Schema(description = "최근에 심은 템플릿부터 담긴 카드 목록")
    List<TemplateResponse> templates
) {
}
