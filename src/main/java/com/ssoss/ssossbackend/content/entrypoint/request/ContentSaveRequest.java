package com.ssoss.ssossbackend.content.entrypoint.request;

import java.util.List;

import com.ssoss.ssossbackend.content.application.command.ContentSaveCommand;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "저장하기 요청")
public record ContentSaveRequest(
    @Schema(description = "저장할 생성 작업 id", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "생성 작업 id 를 입력해 주세요")
    Long generationId,
    @Schema(description = "저장할 채널별 콘텐츠 — 작업의 채널을 빠짐없이 담습니다",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 4, message = "채널은 최대 4개까지 저장할 수 있습니다")
    @Valid
    List<@NotNull(message = "저장할 채널별 콘텐츠를 입력해 주세요") ContentSaveChannelRequest> contents
) {

    public ContentSaveCommand toCommand(Long memberId) {
        if (contents == null) {
            return ContentSaveCommand.of(memberId, generationId, null);
        }
        return ContentSaveCommand.of(memberId, generationId, contents.stream()
            .map(content -> new ContentSaveCommand.Item(
                content.channel(), content.title(), content.body(), content.hashtags()))
            .toList());
    }
}
