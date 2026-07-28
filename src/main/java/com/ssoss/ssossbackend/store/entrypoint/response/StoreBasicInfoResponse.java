package com.ssoss.ssossbackend.store.entrypoint.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "기본 정보 — 매장명·매장 유형·주소·매장 한 줄 소개")
public record StoreBasicInfoResponse(
    @Schema(description = "매장명 — 저장한 적이 없으면 null 입니다", example = "보니스커피", nullable = true)
    String name,
    @Schema(description = "매장 유형 — CAFE: 카페, DESSERT_CAFE: 디저트 카페, BAKERY: 베이커리, BAKERY_CAFE: 베이커리 카페, "
        + "BRUNCH_CAFE: 브런치 카페, ROASTERY_CAFE: 로스터리 카페, CAFE_BAR: 카페바",
        allowableValues = {"CAFE", "DESSERT_CAFE", "BAKERY", "BAKERY_CAFE", "BRUNCH_CAFE", "ROASTERY_CAFE", "CAFE_BAR"},
        example = "CAFE", nullable = true)
    String type,
    @Schema(description = "도로명 주소 — 저장한 적이 없으면 null 입니다", example = "서울 중구 을지로 100", nullable = true)
    String address,
    @Schema(description = "매장 한 줄 소개 — 선택 입력이라 채우지 않았으면 null 입니다",
        example = "매일 아침 굽는 크루아상이 있는 을지로 카페", nullable = true)
    String introduction,
    @Schema(description = "작성 상태 — NOT_WRITTEN: 미작성(네 필드 다 빔), IN_PROGRESS: 작성 중(일부만 있음), "
        + "COMPLETED: 작성 완료(네 필드 다 있음)",
        allowableValues = {"NOT_WRITTEN", "IN_PROGRESS", "COMPLETED"}, example = "NOT_WRITTEN")
    String status
) {
}
