package com.ssoss.ssossbackend.hashtag.entrypoint.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 북마크 해시태그 묶음 목록 응답")
public record BookmarkedHashtagBundleListResponse(
    @Schema(description = "내가 북마크한 묶음 전부 — 페이징하지 않습니다")
    List<BookmarkedHashtagBundleResponse> bundles
) {
}
