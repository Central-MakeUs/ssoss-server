package com.ssoss.ssossbackend.credit.entrypoint.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "잔여 크레딧 응답 — 당사이클 잔여와 사이클 한도")
public record CreditBalanceResponse(
    @Schema(description = "당사이클 잔여 크레딧", example = "50")
    int remaining,
    @Schema(description = "사이클 한도", example = "50")
    int limit
) {
}
