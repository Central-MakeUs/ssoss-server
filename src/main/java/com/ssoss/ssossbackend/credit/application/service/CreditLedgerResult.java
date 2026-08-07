package com.ssoss.ssossbackend.credit.application.service;

import java.time.Instant;

import com.ssoss.ssossbackend.credit.domain.model.CreditLedger;

public record CreditLedgerResult(Long ledgerId, String type, String description, int amount, Instant occurredAt) {

    public static CreditLedgerResult from(CreditLedger ledger) {
        return new CreditLedgerResult(ledger.getId(), ledger.getType().name(), ledger.getDescription(),
            ledger.getAmount(), ledger.getCreatedAt());
    }
}
