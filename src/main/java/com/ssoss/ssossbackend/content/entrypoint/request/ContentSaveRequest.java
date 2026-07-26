package com.ssoss.ssossbackend.content.entrypoint.request;

import com.ssoss.ssossbackend.content.application.command.ContentSaveCommand;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

@Schema(description = "저장하기 요청")
public record ContentSaveRequest(
    @Schema(description = "저장할 생성 작업 id — 그 작업의 성공한 채널 결과 전체가 저장됩니다", example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "생성 작업 id 를 입력해 주세요")
    Long generationId
) {

    public ContentSaveCommand toCommand(Long memberId) {
        return new ContentSaveCommand(memberId, generationId);
    }
}
