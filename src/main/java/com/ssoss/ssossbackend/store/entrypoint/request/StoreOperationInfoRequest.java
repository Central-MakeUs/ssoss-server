package com.ssoss.ssossbackend.store.entrypoint.request;

import java.util.List;

import com.ssoss.ssossbackend.store.application.command.StoreOperationInfoCommand;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "매장 운영 정보 저장 요청")
public record StoreOperationInfoRequest(
    @Schema(description = "영업 요일 목록 (선택) — 같은 요일을 여러 번 보내면 한 번만 저장되고 월요일부터의 순서로 정리됩니다",
        allowableValues = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"},
        example = "[\"MONDAY\", \"TUESDAY\", \"WEDNESDAY\"]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    List<String> businessDays,
    @Schema(description = "영업 시작 시각 (선택) — 24시간 HH:mm, 종료 시각과 한 쌍이라 둘 다 보내거나 둘 다 빼야 합니다",
        example = "09:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Pattern(regexp = "([01]\\d|2[0-3]):[0-5]\\d", message = "영업 시작 시각은 24시간 HH:mm 형식으로 입력해 주세요")
    String openTime,
    @Schema(description = "영업 종료 시각 (선택) — 24시간 HH:mm, 시작보다 이르면 다음날이며 시작 시각과 한 쌍입니다",
        example = "22:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Pattern(regexp = "([01]\\d|2[0-3]):[0-5]\\d", message = "영업 종료 시각은 24시간 HH:mm 형식으로 입력해 주세요")
    String closeTime,
    @Schema(description = "대표 메뉴 목록 (선택) — 최대 10개, 개당 30자", example = "[\"크루아상\", \"바닐라 라떼\"]",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 10, message = "대표 메뉴는 최대 10개까지 입력할 수 있습니다")
    List<@NotBlank(message = "빈 대표 메뉴는 보낼 수 없습니다")
        @Size(max = 30, message = "대표 메뉴는 30자 이내로 입력해 주세요") String> signatureMenus,
    @Schema(description = "포장 가능 여부 (선택) — 보내지 않으면 불가로 저장됩니다", example = "true", defaultValue = "false",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Boolean takeoutAvailable,
    @Schema(description = "예약 가능 여부 (선택) — 보내지 않으면 불가로 저장됩니다", example = "false", defaultValue = "false",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Boolean reservationAvailable,
    @Schema(description = "주차 가능 여부 (선택) — 보내지 않으면 불가로 저장됩니다", example = "true", defaultValue = "false",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Boolean parkingAvailable
) {

    public StoreOperationInfoCommand toCommand(Long memberId) {
        return StoreOperationInfoCommand.of(memberId, businessDays, openTime, closeTime, signatureMenus,
            takeoutAvailable, reservationAvailable, parkingAvailable);
    }
}
