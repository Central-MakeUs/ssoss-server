package com.ssoss.ssossbackend.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("refresh_token")
public class RefreshToken {

    @Id
    private Long id;
    private Long memberId;
    private String sessionId;
    private String tokenHash;
    private RefreshTokenStatus status;
    private Instant expiresAt;
    private Instant deletedAt;

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    RefreshToken(Long id, Long memberId, String sessionId, String tokenHash, RefreshTokenStatus status, Instant expiresAt) {
        this.id = id;
        this.memberId = memberId;
        this.sessionId = sessionId;
        this.tokenHash = tokenHash;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public static RefreshToken issue(Long memberId, String tokenHash, Instant expiresAt) {
        return new RefreshToken(null, memberId, UUID.randomUUID().toString(), tokenHash, RefreshTokenStatus.ACTIVE, expiresAt);
    }

    public boolean isExpired(Instant now) {
        return expiresAt.isBefore(now);
    }

    public boolean isDeleted() {
        return status == RefreshTokenStatus.DELETED;
    }

    public RefreshToken markDeleted(Instant deletedAt) {
        this.status = RefreshTokenStatus.DELETED;
        this.deletedAt = deletedAt;
        return this;
    }

    public RefreshToken nextInSession(String tokenHash, Instant expiresAt) {
        return new RefreshToken(null, memberId, sessionId, tokenHash, RefreshTokenStatus.ACTIVE, expiresAt);
    }
}
