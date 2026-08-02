package com.ssoss.ssossbackend.auth.application.command;

public record WithdrawalCommand(Long memberId, String reason, String detail) {

    public static WithdrawalCommand withoutReason(Long memberId) {
        return new WithdrawalCommand(memberId, null, null);
    }
}
