package com.ssoss.ssossbackend.auth.domain.model;

import java.time.Instant;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("social_login")
public class SocialLogin {

    @Id
    private Long id;
    private Long memberId;
    private SocialProvider provider;
    private String socialId;
    private String refreshToken;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    SocialLogin(Long id, Long memberId, SocialProvider provider, String socialId, String refreshToken) {
        this.id = id;
        this.memberId = memberId;
        this.provider = provider;
        this.socialId = socialId;
        this.refreshToken = refreshToken;
    }

    public static SocialLogin of(Long memberId, SocialProvider provider, String socialId, String refreshToken) {
        return new SocialLogin(null, memberId, provider, socialId, refreshToken);
    }

    public SocialLogin refreshWith(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

}
