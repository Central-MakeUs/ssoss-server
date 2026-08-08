package com.ssoss.ssossbackend.template.entrypoint.response;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "저장한 템플릿 상세")
public record SavedTemplateDetailResponse(
    @Schema(description = "저장한 글의 id — 원본 템플릿 id 와는 다릅니다", example = "1")
    Long savedTemplateId,
    @Schema(description = "저장 시점에 복사한 분류 — NEW_MENU: 신메뉴, EVENT: 이벤트, STORE_INTRO: 매장 소개, NOTICE: 공지",
        allowableValues = {"NEW_MENU", "EVENT", "STORE_INTRO", "NOTICE"}, example = "NEW_MENU")
    String category,
    @Schema(description = "저장 시점에 복사한 제목 — 상세 화면 상단 제목입니다", example = "신메뉴 출시 안내")
    String title,
    @Schema(description = "저장 시점에 복사한 설명 — 제목 아래 한 줄입니다",
        example = "새로 나온 메뉴의 특징과 매력을 소개하는 글")
    String description,
    @Schema(description = "저장할 때 보낸 본문 그대로입니다. 원본 템플릿이 바뀌어도 영향받지 않습니다",
        example = "보니스커피에 새 메뉴가 출시되었습니다!")
    String body,
    @Schema(description = "저장 시점에 복사한 추천 채널. 화면에 보이는 순서 그대로입니다",
        example = "[\"INSTAGRAM\", \"BLOG\", \"THREADS\"]")
    List<String> recommendedChannels,
    @Schema(description = "저장 시각", example = "2026-09-01T09:41:00Z")
    Instant savedAt
) {
}
