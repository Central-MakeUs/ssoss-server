package com.ssoss.ssossbackend.hashtag.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내가 북마크한 해시태그 묶음 카드 1건")
public record BookmarkedHashtagBundleResponse(
    @Schema(description = "해시태그 묶음 id", example = "2")
    Long id,
    @Schema(description = "묶음 이름 — 카드 제목입니다", example = "이벤트/할인 홍보")
    String name,
    @Schema(description = "묶음에 담긴 해시태그 전부. 카드에 보이는 순서 그대로이며 서버가 자르지 않습니다",
        example = "[\"#오픈이벤트\", \"#할인이벤트\", \"#신메뉴출시\"]")
    List<String> hashtags
) {
}
