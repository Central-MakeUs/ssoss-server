package com.ssoss.ssossbackend.auth.application.command;

public record SignupCommand(
    Long memberId,
    boolean serviceTermsAgreed,
    boolean privacyPolicyAgreed,
    boolean marketingAgreed
) {
}
