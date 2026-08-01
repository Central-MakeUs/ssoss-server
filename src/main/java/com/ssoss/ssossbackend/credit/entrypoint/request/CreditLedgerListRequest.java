package com.ssoss.ssossbackend.credit.entrypoint.request;

import com.ssoss.ssossbackend.credit.application.service.CreditLedgerListCommand;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "크레딧 내역 목록 조회 요청")
public record CreditLedgerListRequest(
    @Schema(description = "탭 (선택) — ALL 은 전체, USE 는 사용, GAIN 은 지급과 충전입니다. 생략하면 ALL 입니다",
        allowableValues = {"ALL", "USE", "GAIN"}, example = "USE",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String type,
    @Schema(description = "페이지 번호 (선택) — 0 부터 셉니다", example = "0", defaultValue = "0",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Min(value = 0, message = "페이지 번호는 0 부터 시작합니다")
    Integer page,
    @Schema(description = "페이지 크기 (선택)", example = "20", defaultValue = "20",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Min(value = 1, message = "한 번에 1건 이상 조회해 주세요")
    @Max(value = 50, message = "한 번에 최대 50건까지 조회할 수 있습니다")
    Integer size
) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    public CreditLedgerListRequest {
        page = page == null ? DEFAULT_PAGE : page;
        size = size == null ? DEFAULT_SIZE : size;
    }

    public CreditLedgerListCommand toCommand(Long memberId) {
        return CreditLedgerListCommand.of(memberId, type, page, size);
    }
}
