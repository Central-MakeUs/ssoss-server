package com.ssoss.ssossbackend.app.entrypoint.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "앱 버전 조회 응답 — 업데이트 필요 여부와 최소 지원 버전")
public record AppVersionResponse(
    @Schema(description = "업데이트가 필요한지 여부 — true 면 보낸 버전이 최소 지원 버전보다 낮습니다", example = "true")
    boolean updateRequired,
    @Schema(description = "최소 지원 버전 (semver)", example = "1.0.0")
    String minimumVersion
) {
}
