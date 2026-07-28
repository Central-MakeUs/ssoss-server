package com.ssoss.ssossbackend.store.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "운영 정보 — 영업 요일·영업 시각·대표 메뉴·편의 시설")
public record StoreOperationInfoResponse(
    @Schema(description = "영업 요일 목록 — 저장한 적이 없으면 빈 배열입니다",
        example = "[\"MONDAY\", \"TUESDAY\", \"WEDNESDAY\"]")
    List<String> businessDays,
    @Schema(description = "영업 시작 시각 (24시간 HH:mm)", example = "09:00", nullable = true)
    String openTime,
    @Schema(description = "영업 종료 시각 (24시간 HH:mm) — 시작보다 이르면 다음날입니다", example = "22:00", nullable = true)
    String closeTime,
    @Schema(description = "대표 메뉴 목록 — 저장한 적이 없으면 빈 배열입니다", example = "[\"크루아상\", \"바닐라 라떼\"]")
    List<String> signatureMenus,
    @Schema(description = "포장 가능 여부", example = "false")
    boolean takeoutAvailable,
    @Schema(description = "예약 가능 여부", example = "false")
    boolean reservationAvailable,
    @Schema(description = "주차 가능 여부", example = "false")
    boolean parkingAvailable,
    @Schema(description = "작성 상태 — 영업 요일·영업 시각·대표 메뉴·편의 시설 네 가지 중 몇 가지에 값이 있는지로 갈립니다. "
        + "NOT_WRITTEN: 미작성(넷 다 빔), IN_PROGRESS: 작성 중(일부만 있음), COMPLETED: 작성 완료(넷 다 있음). "
        + "영업 시각은 시작·종료가 둘 다 있어야, 편의 시설은 셋 중 하나라도 가능이어야 값이 있는 것으로 셉니다",
        allowableValues = {"NOT_WRITTEN", "IN_PROGRESS", "COMPLETED"}, example = "NOT_WRITTEN")
    String status
) {
}
