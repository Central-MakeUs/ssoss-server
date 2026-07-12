package com.ssoss.ssossbackend.auth.domain.model;

import java.time.Instant;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("refresh_token")
public class RefreshToken {

    @Id
    private Long id;
    private Long memberId;
    private String tokenHash;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    RefreshToken(Long id, Long memberId, String tokenHash) {
        this.id = id;
        this.memberId = memberId;
        this.tokenHash = tokenHash;
    }

    public static RefreshToken issue(Long memberId, String tokenHash) {
        return new RefreshToken(null, memberId, tokenHash);
    }

    public RefreshToken rotate(String tokenHash) {
        this.tokenHash = tokenHash;
        return this;
    }
}
