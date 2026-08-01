package com.ssoss.ssossbackend.credit.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "크레딧 내역 목록 응답")
public record CreditLedgerListResponse(
    @Schema(description = "탭에 해당하는 전체 건수", example = "3")
    long totalCount,
    @Schema(description = "현재 페이지 번호 — 0 부터 셉니다", example = "0")
    int page,
    @Schema(description = "페이지 크기", example = "20")
    int size,
    @Schema(description = "다음 페이지가 있는지 여부", example = "false")
    boolean hasNext,
    @Schema(description = "변동 시각 최신순 내역 목록")
    List<CreditLedgerResponse> ledgers
) {
}
