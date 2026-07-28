package com.ssoss.ssossbackend.store.entrypoint.request;

import com.ssoss.ssossbackend.store.application.command.StoreBasicInfoCommand;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "매장 기본 정보 저장 요청")
public record StoreBasicInfoRequest(
    @Schema(description = "매장명 (필수)", example = "보니스커피", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "매장명을 입력해 주세요")
    @Size(max = 50, message = "매장명은 50자 이내로 입력해 주세요")
    String name,
    @Schema(description = "매장 유형 (필수) — CAFE: 카페, DESSERT_CAFE: 디저트 카페, BAKERY: 베이커리, "
        + "BAKERY_CAFE: 베이커리 카페, BRUNCH_CAFE: 브런치 카페, ROASTERY_CAFE: 로스터리 카페, CAFE_BAR: 카페바",
        allowableValues = {"CAFE", "DESSERT_CAFE", "BAKERY", "BAKERY_CAFE", "BRUNCH_CAFE", "ROASTERY_CAFE", "CAFE_BAR"},
        example = "CAFE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "매장 유형을 선택해 주세요")
    String type,
    @Schema(description = "도로명 주소 (필수)", example = "서울 중구 을지로 100",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "주소를 입력해 주세요")
    @Size(max = 200, message = "주소는 200자 이내로 입력해 주세요")
    String address,
    @Schema(description = "매장 한 줄 소개 (선택) — 보내지 않거나 빈 문자열·공백만 보내면 비운 것으로 봅니다",
        example = "매일 아침 굽는 크루아상이 있는 을지로 카페", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 100, message = "매장 한 줄 소개는 100자 이내로 입력해 주세요")
    String introduction
) {

    public StoreBasicInfoCommand toCommand(Long memberId) {
        return StoreBasicInfoCommand.of(memberId, name, type, address, introduction);
    }
}
