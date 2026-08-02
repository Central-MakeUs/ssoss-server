package com.ssoss.ssossbackend.content.entrypoint.response;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "생성 기록 목록의 카드 1건 — 저장하기 1회로 만들어진 콘텐츠 하나")
public record ContentSummaryResponse(
    @Schema(description = "콘텐츠 id — 상세 조회에 쓰입니다", example = "1")
    Long contentId,
    @Schema(description = "저장 시각 — 목록의 정렬 기준이며 편집해도 움직이지 않습니다", example = "2026-09-01T09:41:00Z")
    Instant savedAt,
    @Schema(description = "콘텐츠에 담긴 채널 목록. 블로그 → 인스타그램 → 당근 비즈 → 스레드 고정 순서입니다",
        example = "[\"BLOG\", \"INSTAGRAM\"]")
    List<String> channels,
    @Schema(description = "목적 — INFORMATION: 정보성, EVENT_DISCOUNT: 이벤트/할인, NEW_MENU_PROMOTION: 신메뉴/홍보",
        allowableValues = {"INFORMATION", "EVENT_DISCOUNT", "NEW_MENU_PROMOTION"}, example = "INFORMATION")
    String purpose,
    @Schema(description = "톤 — CASUAL: 일상형, EMOTIONAL: 감성형, INFORMATIVE: 정보형, PROMOTIONAL: 홍보형",
        allowableValues = {"CASUAL", "EMOTIONAL", "INFORMATIVE", "PROMOTIONAL"}, example = "CASUAL")
    String tone,
    @Schema(description = """
        카드에 보여줄 제목. 채널 목록의 첫 채널에서 가져옵니다.
        그 채널에 제목이 있으면 저장된 제목이고, 없으면 사진 가이드 태그를 걷어낸 본문입니다.
        어느 쪽이든 20자까지만 담기며, 넘으면 20자에서 자르고 말줄임표(…)를 붙입니다.
        줄바꿈과 연속 공백은 공백 하나로 합쳐집니다.""",
        example = "을지로 크루아상 맛집 | 겹겹이 살…")
    String title,
    @Schema(description = "카드에 보여줄 해시태그. 채널 목록에서 해시태그가 있는 첫 채널의 앞 2개만 담기며, 어느 채널에도 없으면 빈 배열입니다",
        example = "[\"#을지로카페\", \"#을지로크루아상\"]")
    List<String> hashtags
) {
}
