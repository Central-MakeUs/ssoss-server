package com.ssoss.ssossbackend.template.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 북마크 추천 템플릿 목록 응답")
public record BookmarkedTemplateListResponse(
    @Schema(description = "내가 북마크한 템플릿 전부 — 페이징하지 않습니다")
    List<BookmarkedTemplateResponse> templates
) {
}
