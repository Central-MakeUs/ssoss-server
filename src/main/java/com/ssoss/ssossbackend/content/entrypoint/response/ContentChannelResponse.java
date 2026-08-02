package com.ssoss.ssossbackend.content.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채널별 콘텐츠 1건 — 편집·삭제·채널 변환은 이 contentChannelId 로 호출합니다")
public record ContentChannelResponse(
    @Schema(description = "채널별 콘텐츠 id", example = "10")
    Long contentChannelId,
    @Schema(description = "채널", allowableValues = {"BLOG", "INSTAGRAM", "THREADS", "DAANGN_BIZ"}, example = "BLOG")
    String channel,
    @Schema(description = "제목 — 블로그만 있고 나머지 채널은 null 입니다",
        example = "을지로 크루아상 맛집 | 겹겹이 살아있는 결, 보니스커피", nullable = true)
    String title,
    @Schema(description = """
        본문.
        사진 가이드를 체크한 생성에서 저장했으면 본문 안에 `<photo-guide title="..." description="..."/>` 태그가 섞여 있습니다.
        형식은 생성 작업 조회 API 와 같습니다.""",
        example = "을지로에서 크루아상 하나를 제대로 먹고 싶다면, 보니스커피를 추천드려요...")
    String body,
    @Schema(description = "해시태그 목록 — 당근 비즈는 빈 배열입니다", example = "[\"#을지로카페\", \"#을지로크루아상\"]")
    List<String> hashtags
) {
}
