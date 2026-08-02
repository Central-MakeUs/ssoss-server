package com.ssoss.ssossbackend.auth.entrypoint.request;

import com.ssoss.ssossbackend.auth.application.command.WithdrawalCommand;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Size;

@Schema(description = "탈퇴 요청 — 사유를 남기지 않으면 본문 없이 호출합니다")
public record WithdrawalRequest(
    @Schema(description = "탈퇴 사유 (선택)",
        allowableValues = {"MISSING_FEATURE", "CONTENT_QUALITY", "HARD_TO_USE", "RARELY_USED", "OTHER"},
        example = "HARD_TO_USE", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String reason,
    @Schema(description = "기타 사유의 자유 입력 (선택) — 최대 500자",
        example = "쓰고 싶은 채널이 없었어요", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 500, message = "기타 사유는 500자 이내로 입력해 주세요")
    String detail
) {

    public WithdrawalCommand toCommand(Long memberId) {
        return new WithdrawalCommand(memberId, reason, detail);
    }
}
