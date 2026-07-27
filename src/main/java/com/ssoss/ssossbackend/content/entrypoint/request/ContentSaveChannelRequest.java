package com.ssoss.ssossbackend.content.entrypoint.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "저장할 채널별 콘텐츠")
public record ContentSaveChannelRequest(
    @Schema(description = "채널", allowableValues = {"BLOG", "INSTAGRAM", "DAANGN_BIZ", "THREADS"}, example = "BLOG",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "채널을 선택해 주세요")
    String channel,
    @Schema(description = "제목 — 블로그는 필수, 나머지 채널은 보내지 않습니다",
        example = "을지로 크루아상 맛집 | 겹겹이 살아있는 결, 보니스커피",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 60, message = "제목은 60자 이내로 입력해 주세요")
    String title,
    @Schema(description = "본문 (필수)", example = "을지로에서 크루아상 하나를 제대로 먹고 싶다면, 보니스커피를 추천드려요...",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "본문을 입력해 주세요")
    String body,
    @Schema(description = "해시태그 목록 (선택) — 최대 20개, 비우려면 빈 배열을 보냅니다",
        example = "[\"#을지로카페\", \"#을지로크루아상\"]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 20, message = "해시태그는 최대 20개까지 입력할 수 있습니다")
    List<@NotBlank(message = "빈 해시태그는 보낼 수 없습니다") String> hashtags
) {
}
