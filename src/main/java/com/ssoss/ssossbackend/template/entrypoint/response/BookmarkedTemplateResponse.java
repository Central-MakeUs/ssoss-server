package com.ssoss.ssossbackend.template.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내가 북마크한 추천 템플릿 카드 1건")
public record BookmarkedTemplateResponse(
    @Schema(description = "템플릿 id", example = "8")
    Long id,
    @Schema(description = "분류 — NEW_MENU: 신메뉴, EVENT: 이벤트, STORE_INTRO: 매장 소개, NOTICE: 공지",
        allowableValues = {"NEW_MENU", "EVENT", "STORE_INTRO", "NOTICE"}, example = "NEW_MENU")
    String category,
    @Schema(description = "템플릿 제목 — 카드 제목입니다", example = "신메뉴 출시 안내")
    String title,
    @Schema(description = "어떤 상황에 쓰는 템플릿인지에 대한 설명 — 카드 제목 아래 한 줄입니다",
        example = "새로 나온 메뉴의 특징과 매력을 소개하는 글")
    String description,
    @Schema(description = "이 템플릿을 올리기 좋은 채널. 카드에 보이는 순서 그대로입니다",
        example = "[\"INSTAGRAM\", \"BLOG\", \"THREADS\"]")
    List<String> recommendedChannels
) {
}
