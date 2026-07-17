package com.ssoss.ssossbackend.member.domain.model;

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

    @Id
    private Long id;
    private SocialProvider provider;
    private String socialId;
    private String email;
    private MemberStatus status;

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
}
