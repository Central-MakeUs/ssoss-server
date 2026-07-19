package com.ssoss.ssossbackend.member.domain.model;

import java.time.Duration;
import java.time.Instant;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("member_withdrawal_history")
public class MemberWithdrawalHistory {

    public static final Duration SIGNUP_RESTRICTION_PERIOD = Duration.ofDays(60);

    @Id
    private Long id;
    private SocialProvider provider;
    private String socialId;
    private Instant withdrawnAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    MemberWithdrawalHistory(Long id, SocialProvider provider, String socialId, Instant withdrawnAt) {
        this.id = id;
        this.provider = provider;
        this.socialId = socialId;
        this.withdrawnAt = withdrawnAt;
    }

    public static MemberWithdrawalHistory record(SocialProvider provider, String socialId, Instant withdrawnAt) {
        return new MemberWithdrawalHistory(null, provider, socialId, withdrawnAt);
    }
}
