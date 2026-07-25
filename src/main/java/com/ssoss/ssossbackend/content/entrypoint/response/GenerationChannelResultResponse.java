package com.ssoss.ssossbackend.content.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채널별 생성 결과")
public record GenerationChannelResultResponse(
    @Schema(description = "채널", allowableValues = {"BLOG", "INSTAGRAM", "DAANGN_BIZ", "THREADS"}, example = "BLOG")
    String channel,
    @Schema(description = "채널 상태 — PENDING: 생성 중, SUCCEEDED: 성공, FAILED: 실패",
        allowableValues = {"PENDING", "SUCCEEDED", "FAILED"}, example = "SUCCEEDED")
    String status,
    @Schema(description = "채널 상태를 사용자에게 보여줄 문구. 상태와 무관하게 항상 값이 있습니다",
        example = "생성에 성공했습니다")
    String message,
    @Schema(description = "제목 — 성공한 블로그만 있고 나머지는 null 입니다",
        example = "주말엔 아메리카노 1+1, 놓치면 아쉬운 이벤트", nullable = true)
    String title,
    @Schema(description = """
        본문 — 성공일 때만 값이 있습니다.
        사진 가이드를 체크한 생성이면 본문 안에 `<photo-guide type="..." title="..." description="..."/>` 태그가 섞여 있습니다.
        닫는 태그가 없는 한 덩어리이고, type 은 MENU·STORE·MOOD·PEOPLE 넷 중 하나입니다.
        속성값의 &, <, >, " 는 HTML 방식으로 이스케이프됩니다.""",
        example = "이번 주말, 매장에서 아메리카노 1+1 이벤트를 진행합니다..."
            + "<photo-guide type=\"MENU\" title=\"시그니처 메뉴\" description=\"위에서 내려다보는 각도로 찍어 주세요\"/>...",
        nullable = true)
    String body,
    @Schema(description = "해시태그 목록 — 성공이 아니면 빈 배열입니다", example = "[\"#카페이벤트\", \"#아메리카노\"]")
    List<String> hashtags
) {
}
