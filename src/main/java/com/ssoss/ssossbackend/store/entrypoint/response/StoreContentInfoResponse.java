package com.ssoss.ssossbackend.store.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "콘텐츠 정보 — 생성 화면의 강조 내용·금지 내용·키워드·톤 입력란을 미리 채우는 기본값 묶음")
public record StoreContentInfoResponse(
    @Schema(description = "매장 강점 — 생성 작업의 강조 내용을 미리 채우는 값입니다",
        example = "매일 아침 직접 굽는 크루아상과 직접 로스팅한 원두", nullable = true)
    String strength,
    @Schema(description = "매장 키워드 목록 — 생성 작업의 키워드를 미리 채우는 값입니다. 저장한 적이 없으면 빈 배열입니다",
        example = "[\"디저트\", \"크루아상\", \"을지로베이커리\"]")
    List<String> keywords,
    @Schema(description = "금지 내용 — 콘텐츠에 나오면 안 되는 내용입니다", example = "최저가, 1위 같은 과장 표현", nullable = true)
    String forbidden,
    @Schema(description = "콘텐츠 작성 톤 — CASUAL: 일상형, EMOTIONAL: 감성형, INFORMATIVE: 정보형, PROMOTIONAL: 홍보형",
        allowableValues = {"CASUAL", "EMOTIONAL", "INFORMATIVE", "PROMOTIONAL"}, example = "CASUAL", nullable = true)
    String tone,
    @Schema(description = "작성 상태 — NOT_WRITTEN: 미작성(네 필드 다 빔), COMPLETED: 작성 완료(하나라도 있음)",
        allowableValues = {"NOT_WRITTEN", "COMPLETED"}, example = "NOT_WRITTEN")
    String status
) {
}
