package com.ssoss.ssossbackend.template.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "추천 템플릿 상세")
public record TemplateDetailResponse(
    @Schema(description = "템플릿 id", example = "8")
    Long id,
    @Schema(description = "분류 — NEW_MENU: 신메뉴, EVENT: 이벤트, STORE_INTRO: 매장 소개, NOTICE: 공지",
        allowableValues = {"NEW_MENU", "EVENT", "STORE_INTRO", "NOTICE"}, example = "NEW_MENU")
    String category,
    @Schema(description = "템플릿 제목 — 상세 화면 상단 제목입니다", example = "신메뉴 출시 안내")
    String title,
    @Schema(description = "어떤 상황에 쓰는 템플릿인지에 대한 설명 — 제목 아래 한 줄입니다",
        example = "새로 나온 메뉴의 특징과 매력을 소개하는 글")
    String description,
    @Schema(description = "자리표시자가 치환되지 않은 원문", example = "[가게명]에 새 메뉴가 출시되었습니다!")
    String body,
    @Schema(description = "자리표시자가 다른 매장 정보로 모두 채워진 예시 본문",
        example = "카페 모먼트에 새 메뉴가 출시되었습니다!")
    String exampleBody,
    @Schema(description = "이 템플릿을 올리기 좋은 채널. 화면에 보이는 순서 그대로입니다",
        example = "[\"INSTAGRAM\", \"BLOG\", \"THREADS\"]")
    List<String> recommendedChannels,
    @Schema(description = "내가 이 템플릿을 북마크했는지 여부 — 화면 하단의 북마크 아이콘을 눌린 상태로 그릴 때 씁니다",
        example = "false")
    boolean bookmarked
) {
}
