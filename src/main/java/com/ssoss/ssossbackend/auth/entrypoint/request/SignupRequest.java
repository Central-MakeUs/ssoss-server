package com.ssoss.ssossbackend.auth.entrypoint.request;

import com.ssoss.ssossbackend.auth.application.command.SignupCommand;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

@Schema(description = "회원가입 요청")
public record SignupRequest(
    @Schema(description = "서비스 이용약관 동의 여부 (필수 약관 — true 여야 합니다)", example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "서비스 이용약관 동의 여부를 입력해 주세요")
    Boolean serviceTermsAgreed,
    @Schema(description = "개인정보 수집·이용 동의 여부 (필수 약관 — true 여야 합니다)", example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "개인정보 수집·이용 동의 여부를 입력해 주세요")
    Boolean privacyPolicyAgreed,
    @Schema(description = "마케팅 수신 동의 여부 (선택 약관 — false 도 허용됩니다)", example = "false",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "마케팅 수신 동의 여부를 입력해 주세요")
    Boolean marketingAgreed
) {

    public SignupCommand toCommand(Long memberId) {
        return new SignupCommand(memberId, serviceTermsAgreed, privacyPolicyAgreed, marketingAgreed);
    }
}
