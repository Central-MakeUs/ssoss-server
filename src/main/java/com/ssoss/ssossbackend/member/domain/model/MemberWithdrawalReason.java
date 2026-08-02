package com.ssoss.ssossbackend.member.domain.model;

import java.time.Instant;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.StringUtils;

@Getter
@Table("member_withdrawal_reason")
public class MemberWithdrawalReason {

    @Id
    private Long id;
    private WithdrawalReason reason;
    private String detail;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    MemberWithdrawalReason(Long id, WithdrawalReason reason, String detail) {
        this.id = id;
        this.reason = reason;
        this.detail = StringUtils.hasText(detail) ? detail : null;
    }

    public static MemberWithdrawalReason record(WithdrawalReason reason, String detail) {
        return new MemberWithdrawalReason(null, reason, detail);
    }
}
