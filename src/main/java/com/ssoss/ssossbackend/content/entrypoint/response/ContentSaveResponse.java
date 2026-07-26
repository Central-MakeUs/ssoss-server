package com.ssoss.ssossbackend.content.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "저장하기 응답 — 저장된 채널별 콘텐츠 목록")
public record ContentSaveResponse(
    @Schema(description = "저장된 콘텐츠. 작업에서 성공한 채널 수만큼 담깁니다")
    List<ContentItemResponse> contents
) {
}
