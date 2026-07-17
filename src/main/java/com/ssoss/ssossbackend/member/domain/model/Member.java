package com.ssoss.ssossbackend.member.domain.model;

import java.time.Instant;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("member")
public class Member {

    @Id
    private Long id;
    private SocialProvider provider;
    private String socialId;
    private MemberStatus status;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    Member(Long id, SocialProvider provider, String socialId, MemberStatus status) {
        this.id = id;
        this.provider = provider;
        this.socialId = socialId;
        this.status = status;
    }

    public static Member register(SocialProvider provider, String socialId) {
        return new Member(null, provider, socialId, MemberStatus.PENDING);
    }
}
