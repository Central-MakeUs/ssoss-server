package com.ssoss.ssossbackend.content.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "생성 작업 폴링 응답 — 파생 상태와 완성된 채널별 결과")
public record GenerationPollResponse(
    @Schema(description = "생성 작업 id", example = "1")
    Long generationId,
    @Schema(description = "작업 상태 — IN_PROGRESS: 진행 중, SUCCEEDED: 성공(성공 결과 1건 이상), FAILED: 실패(성공 결과 없이 종료·시간 초과)",
        allowableValues = {"IN_PROGRESS", "SUCCEEDED", "FAILED"}, example = "SUCCEEDED")
    String status,
    @Schema(description = "실패 원인 범주 — FAILED 일 때만 값이 있고, 원인 파악이 불가하면 FAILED 여도 null 입니다. "
        + "OVERLOADED: 서버 과부하(잠시 후 재시도), TIMED_OUT: 생성 시간 초과, EMPTY_OUTPUT: 생성 결과 없음",
        allowableValues = {"OVERLOADED", "TIMED_OUT", "EMPTY_OUTPUT"}, example = "OVERLOADED", nullable = true)
    String failureReason,
    @Schema(description = "완성된 채널별 결과 (완성된 채널부터 순차로 채워집니다)")
    List<GenerationChannelResultResponse> results,
    @Schema(description = "아직 상태가 정해지지 않은 진행 중 채널 목록", example = "[\"INSTAGRAM\"]")
    List<String> pendingChannels
) {
}
