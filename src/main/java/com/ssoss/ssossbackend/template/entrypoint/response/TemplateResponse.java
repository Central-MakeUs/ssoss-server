package com.ssoss.ssossbackend.template.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "추천 템플릿 카드 1건")
public record TemplateResponse(
    @Schema(description = "템플릿 id", example = "8")
    Long id,
    @Schema(description = "분류 — NEW_MENU: 신메뉴, EVENT: 이벤트, STORE_INTRO: 매장 소개, NOTICE: 공지",
        allowableValues = {"NEW_MENU", "EVENT", "STORE_INTRO", "NOTICE"}, example = "NEW_MENU")
    String category,
    @Schema(description = "템플릿 제목 — 카드 제목입니다", example = "신메뉴 출시 알림")
    String title,
    @Schema(description = "어떤 상황에 쓰는 템플릿인지에 대한 설명 — 카드 제목 아래 한 줄입니다",
        example = "새로 나온 메뉴를 사진과 함께 처음 알릴 때 쓰는 글입니다")
    String description,
    @Schema(description = "이 템플릿을 올리기 좋은 채널. 카드에 보이는 순서 그대로입니다",
        example = "[\"INSTAGRAM\", \"BLOG\", \"THREADS\"]")
    List<String> recommendedChannels,
    @Schema(description = "내가 이 템플릿을 북마크했는지 여부 — 카드의 북마크 아이콘을 눌린 상태로 그릴 때 씁니다",
        example = "false")
    boolean bookmarked
) {
}
