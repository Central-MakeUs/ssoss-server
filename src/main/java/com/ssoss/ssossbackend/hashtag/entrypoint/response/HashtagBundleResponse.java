package com.ssoss.ssossbackend.hashtag.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "해시태그 묶음 카드 1건")
public record HashtagBundleResponse(
    @Schema(description = "해시태그 묶음 id", example = "3")
    Long id,
    @Schema(description = "묶음 이름 — 카드 제목입니다", example = "카공 카페")
    String name,
    @Schema(description = "묶음에 담긴 해시태그 전부. 카드에 보이는 순서 그대로이며 서버가 자르지 않습니다",
        example = "[\"#카공카페\", \"#노트북카페\", \"#콘센트많은카페\"]")
    List<String> hashtags
) {
}
