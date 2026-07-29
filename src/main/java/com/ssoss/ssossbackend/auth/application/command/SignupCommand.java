package com.ssoss.ssossbackend.auth.application.command;

public record SignupCommand(
    Long memberId,
    boolean ageOver14Agreed,
    boolean serviceTermsAgreed,
    boolean privacyPolicyAgreed
) {
}
