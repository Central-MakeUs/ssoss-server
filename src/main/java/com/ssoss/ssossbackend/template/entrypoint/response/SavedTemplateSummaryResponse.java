package com.ssoss.ssossbackend.template.entrypoint.response;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "저장한 템플릿 카드 1건")
public record SavedTemplateSummaryResponse(
    @Schema(description = "저장한 글의 id — 저장하기 응답의 savedTemplateId 와 같은 값이고, 원본 템플릿 id 와는 다릅니다",
        example = "1")
    Long savedTemplateId,
    @Schema(description = "저장 시점에 복사한 분류 — NEW_MENU: 신메뉴, EVENT: 이벤트, STORE_INTRO: 매장 소개, NOTICE: 공지",
        allowableValues = {"NEW_MENU", "EVENT", "STORE_INTRO", "NOTICE"}, example = "NEW_MENU")
    String category,
    @Schema(description = "저장 시점에 복사한 제목 — 카드 제목입니다", example = "신메뉴 출시 안내")
    String title,
    @Schema(description = "저장 시점에 복사한 설명 — 카드 제목 아래 한 줄입니다",
        example = "새로 나온 메뉴의 특징과 매력을 소개하는 글")
    String description,
    @Schema(description = "저장 시각 — 목록의 정렬 기준입니다", example = "2026-09-01T09:41:00Z")
    Instant savedAt
) {
}
