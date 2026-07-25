package com.ssoss.ssossbackend.content.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "생성 작업 조회 응답 — 파생 상태와 선택 채널별 결과")
public record GenerationDetailResponse(
    @Schema(description = "생성 작업 id", example = "1")
    Long generationId,
    @Schema(description = "작업 상태 — IN_PROGRESS: 작업이 아직 돌고 있음, COMPLETED: 작업이 끝났거나 60초가 지나 더 이상 결과가 더해지지 않음",
        allowableValues = {"IN_PROGRESS", "COMPLETED"}, example = "IN_PROGRESS")
    String status,
    @Schema(description = "목적 — INFORMATION: 정보성, EVENT_DISCOUNT: 이벤트/할인, NEW_MENU_PROMOTION: 신메뉴/홍보",
        allowableValues = {"INFORMATION", "EVENT_DISCOUNT", "NEW_MENU_PROMOTION"}, example = "INFORMATION")
    String purpose,
    @Schema(description = "톤 — CASUAL: 일상형, EMOTIONAL: 감성형, INFORMATIVE: 정보형, PROMOTIONAL: 홍보형",
        allowableValues = {"CASUAL", "EMOTIONAL", "INFORMATIVE", "PROMOTIONAL"}, example = "CASUAL")
    String tone,
    @Schema(description = "생성에 쓴 키워드 목록 — 입력하지 않았으면 빈 배열입니다",
        example = "[\"디저트\", \"크루아상\", \"을지로베이커리\"]")
    List<String> keywords,
    @Schema(description = "선택한 채널 전부의 결과. 요청한 채널 순서 그대로 담깁니다")
    List<GenerationChannelResultResponse> results
) {
}
