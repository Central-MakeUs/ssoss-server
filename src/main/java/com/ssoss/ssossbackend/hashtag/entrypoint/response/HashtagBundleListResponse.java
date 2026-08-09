package com.ssoss.ssossbackend.hashtag.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "해시태그 묶음 목록 응답")
public record HashtagBundleListResponse(
    @Schema(description = "조건에 해당하는 묶음 개수", example = "3")
    long totalCount,
    @Schema(description = "현재 페이지 번호 — 0 부터 셉니다", example = "0")
    int page,
    @Schema(description = "페이지 크기", example = "20")
    int size,
    @Schema(description = "다음 페이지가 있는지 여부", example = "false")
    boolean hasNext,
    @Schema(description = "최근에 심은 묶음부터 담긴 카드 목록")
    List<HashtagBundleResponse> bundles
) {
}
