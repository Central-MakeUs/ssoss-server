package com.ssoss.ssossbackend.app.entrypoint.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "앱 버전 조회 요청")
public record AppVersionCheckRequest(
    @Schema(description = "현재 앱 버전 — semver(x.y.z) 형식입니다", example = "1.2.3")
    @NotBlank(message = "현재 앱 버전을 보내 주세요")
    String version
) {
}
