package com.ssoss.ssossbackend.credit.domain.model;

import java.time.Instant;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("credit_ledger")
public class CreditLedgerEntry {

    @Id
    private Long id;
    private Long memberId;
    private int amount;
    private Long generationResultId;

    @CreatedDate
    private Instant createdAt;

    CreditLedgerEntry(Long id, Long memberId, int amount, Long generationResultId) {
        this.id = id;
        this.memberId = memberId;
        this.amount = amount;
        this.generationResultId = generationResultId;
    }
}
