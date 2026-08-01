package com.ssoss.ssossbackend.credit.domain.model;

import java.time.Instant;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("credit_ledger")
public class CreditLedger {

    private static final String SIGNUP_GRANT_DESCRIPTION = "가입 크레딧 지급";
    private static final String CYCLE_GRANT_DESCRIPTION = "%d월 크레딧 지급";
    private static final String EXPIRE_DESCRIPTION = "%d월 크레딧 소멸";

    @Id
    private Long id;
    private Long memberId;
    private CreditLedgerType type;
    private int amount;
    private String description;
    private Long generationId;

    @CreatedDate
    private Instant createdAt;

    CreditLedger(Long id, Long memberId, CreditLedgerType type, int amount, String description, Long generationId) {
        this.id = id;
        this.memberId = memberId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.generationId = generationId;
    }

    public static CreditLedger signupGrant(Long memberId, int amount) {
        return new CreditLedger(null, memberId, CreditLedgerType.GRANT, amount, SIGNUP_GRANT_DESCRIPTION, null);
    }

    public static CreditLedger cycleGrant(Long memberId, int amount, CreditCycle cycle) {
        return new CreditLedger(null, memberId, CreditLedgerType.GRANT, amount,
            CYCLE_GRANT_DESCRIPTION.formatted(cycle.month()), null);
    }

    public static CreditLedger deduct(Long memberId, int amount, Long generationId, String description) {
        return new CreditLedger(null, memberId, CreditLedgerType.DEDUCT, -amount, description, generationId);
    }

    public static CreditLedger expire(Long memberId, int amount, CreditCycle cycle) {
        return new CreditLedger(null, memberId, CreditLedgerType.EXPIRE, -amount,
            EXPIRE_DESCRIPTION.formatted(cycle.month()), null);
    }
}
