package com.ssoss.ssossbackend.content.entrypoint.request;

import java.util.List;

import com.ssoss.ssossbackend.content.application.command.ChannelConversionCommand;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Schema(description = "다른 채널용으로 만들기 요청")
public record ChannelConversionRequest(
    @Schema(description = "새로 만들 채널 목록 (1~3개, 중복 불가) — 원본 채널은 고를 수 없습니다",
        allowableValues = {"BLOG", "INSTAGRAM", "DAANGN_BIZ", "THREADS"},
        example = "[\"INSTAGRAM\", \"THREADS\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "채널을 1개 이상 선택해 주세요")
    @Size(max = 3, message = "채널은 최대 3개까지 선택할 수 있습니다")
    List<String> channels
) {

    public ChannelConversionCommand toCommand(Long memberId, Long contentId, Long contentChannelId) {
        return ChannelConversionCommand.of(memberId, contentId, contentChannelId, channels);
    }
}
