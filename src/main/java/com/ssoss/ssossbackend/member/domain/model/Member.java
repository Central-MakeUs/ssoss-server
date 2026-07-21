package com.ssoss.ssossbackend.member.domain.model;

import java.time.Duration;
import java.time.Instant;

import com.ssoss.ssossbackend.shared.exception.BusinessException;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("member")
public class Member {

    public static final Duration RECOVERY_GRACE_PERIOD = Duration.ofDays(7);

    @Id
    private Long id;
    private SocialProvider provider;
    private String socialId;
    private String email;
    private MemberStatus status;
    private Instant lastWithdrawnAt;

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    Member(Long id, SocialProvider provider, String socialId, String email, MemberStatus status) {
        this.id = id;
        this.provider = provider;
        this.socialId = socialId;
        this.email = email;
        this.status = status;
    }

    public static Member register(SocialProvider provider, String socialId, String email) {
        return new Member(null, provider, socialId, email, MemberStatus.PENDING);
    }

    public Member activate() {
        if (status != MemberStatus.PENDING) {
            throw new BusinessException(MemberErrorCode.ALREADY_SIGNED_UP);
        }
        this.status = MemberStatus.ACTIVE;
        return this;
    }

    public Member withdraw(Instant withdrawnAt) {
        if (status != MemberStatus.ACTIVE) {
            throw new BusinessException(MemberErrorCode.ALREADY_WITHDRAWN);
        }
        this.status = MemberStatus.WITHDRAWN;
        this.lastWithdrawnAt = withdrawnAt;
        return this;
    }

    public Member recover(Instant now) {
        if (status != MemberStatus.WITHDRAWN) {
            throw new BusinessException(MemberErrorCode.ALREADY_RECOVERED);
        }
        if (now.isAfter(lastWithdrawnAt.plus(RECOVERY_GRACE_PERIOD))) {
            throw new BusinessException(MemberErrorCode.RECOVERY_GRACE_EXPIRED);
        }
        this.status = MemberStatus.ACTIVE;
        return this;
    }
}
