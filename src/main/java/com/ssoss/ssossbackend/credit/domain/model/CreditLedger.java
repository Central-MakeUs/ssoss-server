package com.ssoss.ssossbackend.credit.domain.model;

import java.time.Instant;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("credit_ledger")
public class CreditLedger {

    @Id
    private Long id;
    private Long memberId;
    private CreditLedgerType type;
    private int amount;
    private Long generationId;

    @CreatedDate
    private Instant createdAt;

    CreditLedger(Long id, Long memberId, CreditLedgerType type, int amount, Long generationId) {
        this.id = id;
        this.memberId = memberId;
        this.type = type;
        this.amount = amount;
        this.generationId = generationId;
    }

    public static CreditLedger grant(Long memberId, int amount) {
        return new CreditLedger(null, memberId, CreditLedgerType.GRANT, amount, null);
    }

    public static CreditLedger deduct(Long memberId, int amount, Long generationId) {
        return new CreditLedger(null, memberId, CreditLedgerType.DEDUCT, -amount, generationId);
    }

    public static CreditLedger expire(Long memberId, int amount) {
        return new CreditLedger(null, memberId, CreditLedgerType.EXPIRE, -amount, null);
    }
}
