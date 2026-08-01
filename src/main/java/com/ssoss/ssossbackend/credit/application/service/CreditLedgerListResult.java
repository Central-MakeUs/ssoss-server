package com.ssoss.ssossbackend.credit.application.service;

import java.time.Instant;
import java.util.List;

import com.ssoss.ssossbackend.credit.domain.model.CreditLedger;

import org.springframework.data.domain.Page;

public record CreditLedgerListResult(long totalCount, int page, int size, boolean hasNext, List<Item> ledgers) {

    public static CreditLedgerListResult from(Page<CreditLedger> found) {
        return new CreditLedgerListResult(found.getTotalElements(), found.getNumber(), found.getSize(),
            found.hasNext(), found.getContent().stream().map(Item::from).toList());
    }

    public record Item(Long ledgerId, String type, String description, int amount, Instant occurredAt) {

        public static Item from(CreditLedger ledger) {
            return new Item(ledger.getId(), ledger.getType().name(), ledger.getDescription(), ledger.getAmount(),
                ledger.getCreatedAt());
        }
    }
}
