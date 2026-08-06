package com.ssoss.ssossbackend.hashtag.entrypoint.request;

import com.ssoss.ssossbackend.hashtag.application.command.HashtagBundleListCommand;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "해시태그 묶음 카탈로그 조회 요청")
public record HashtagBundleListRequest(
    @Schema(description = "검색어 (선택) — 묶음 이름과 태그 내용을 함께 부분 일치로 거릅니다", example = "콘센트",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String keyword,
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

    public HashtagBundleListRequest {
        page = page == null ? DEFAULT_PAGE : page;
        size = size == null ? DEFAULT_SIZE : size;
    }

    public HashtagBundleListCommand toCommand(Long memberId) {
        return HashtagBundleListCommand.of(memberId, keyword, page, size);
    }
}
