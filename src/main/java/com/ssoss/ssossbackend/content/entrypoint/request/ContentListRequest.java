package com.ssoss.ssossbackend.content.entrypoint.request;

import com.ssoss.ssossbackend.content.application.command.ContentListCommand;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "생성 기록 목록 조회 요청")
public record ContentListRequest(
    @Schema(description = "채널 (선택) — 생략하면 전체입니다. 그 채널을 포함한 저장 건이 걸러집니다",
        allowableValues = {"BLOG", "INSTAGRAM", "DAANGN_BIZ", "THREADS"}, example = "BLOG",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String channel,
    @Schema(description = "정렬 (선택) — LATEST 는 저장 시각 최신순, OLDEST 는 오래된 순입니다. 생략하면 LATEST 입니다",
        allowableValues = {"LATEST", "OLDEST"}, example = "OLDEST", defaultValue = "LATEST",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String sort,
    @Schema(description = "페이지 번호 (선택) — 0 부터 셉니다", example = "0", defaultValue = "0",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Min(value = 0, message = "페이지 번호는 0 부터 시작합니다")
    Integer page,
    @Schema(description = "페이지 크기 (선택) — 홈의 최근 저장 3건은 3 을 보냅니다", example = "20", defaultValue = "20",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Min(value = 1, message = "한 번에 1건 이상 조회해 주세요")
    @Max(value = 50, message = "한 번에 최대 50건까지 조회할 수 있습니다")
    Integer size
) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    public ContentListRequest {
        page = page == null ? DEFAULT_PAGE : page;
        size = size == null ? DEFAULT_SIZE : size;
    }

    public ContentListCommand toCommand(Long memberId) {
        return ContentListCommand.of(memberId, channel, sort, page, size);
    }
}
