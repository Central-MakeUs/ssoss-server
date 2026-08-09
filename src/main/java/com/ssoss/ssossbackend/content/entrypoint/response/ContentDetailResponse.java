package com.ssoss.ssossbackend.content.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "콘텐츠 상세 응답 — 저장할 때의 생성 조건과 채널별 최신본")
public record ContentDetailResponse(
    @Schema(description = "콘텐츠 id", example = "1")
    Long contentId,
    @Schema(description = """
        콘텐츠 이름. 저장할 때 서버가 채널 목록의 첫 채널에서 뽑아 두며, 회원이 이름 수정으로 바꿀 수 있습니다.
        목록 카드의 name 과 같은 값입니다.""",
        example = "을지로 크루아상 맛집 | 겹겹이 살…")
    String name,
    @Schema(description = "목적 — INFORMATION: 정보성, EVENT_DISCOUNT: 이벤트/할인, NEW_MENU_PROMOTION: 신메뉴/홍보",
        allowableValues = {"INFORMATION", "EVENT_DISCOUNT", "NEW_MENU_PROMOTION"}, example = "INFORMATION")
    String purpose,
    @Schema(description = "톤 — CASUAL: 일상형, EMOTIONAL: 감성형, INFORMATIVE: 정보형, PROMOTIONAL: 홍보형",
        allowableValues = {"CASUAL", "EMOTIONAL", "INFORMATIVE", "PROMOTIONAL"}, example = "CASUAL")
    String tone,
    @Schema(description = "생성에 쓴 키워드 목록 — 입력하지 않았으면 빈 배열입니다",
        example = "[\"디저트\", \"크루아상\", \"을지로베이커리\"]")
    List<String> keywords,
    @Schema(description = "콘텐츠에 담긴 채널별 본문. 블로그 → 인스타그램 → 당근 비즈 → 스레드 고정 순서입니다")
    List<ContentChannelResponse> contents
) {
}
