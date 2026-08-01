package com.ssoss.ssossbackend.credit.entrypoint.response;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "크레딧 내역 행")
public record CreditLedgerResponse(
    @Schema(description = "원장 행 식별자", example = "12")
    Long ledgerId,
    @Schema(description = "변동 유형", allowableValues = {"GRANT", "DEDUCT", "CHARGE"}, example = "DEDUCT")
    String type,
    @Schema(description = "변동 사유 문구", example = "블로그 외 1건 콘텐츠 생성")
    String description,
    @Schema(description = "부호 있는 크레딧 변동량 — 지급·충전은 양수, 차감은 음수입니다", example = "-10")
    int amount,
    @Schema(description = "변동 시각", example = "2026-07-29T05:12:29.086Z")
    Instant occurredAt
) {
}
