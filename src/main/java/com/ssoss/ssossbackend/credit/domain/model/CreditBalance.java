package com.ssoss.ssossbackend.credit.domain.model;

import java.util.List;

public record CreditBalance(int remaining, int limit) {

    public static final int CYCLE_LIMIT = 50;

    public static CreditBalance of(List<CreditLedgerEntry> currentCycleEntries) {
        int currentCycleSum = currentCycleEntries.stream()
            .mapToInt(CreditLedgerEntry::getAmount)
            .sum();
        return new CreditBalance(CYCLE_LIMIT + currentCycleSum, CYCLE_LIMIT);
    }
}
