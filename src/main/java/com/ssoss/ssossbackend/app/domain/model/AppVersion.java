package com.ssoss.ssossbackend.app.domain.model;

import java.time.Instant;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("app_version")
public class AppVersion {

    @Id
    private Long id;
    private AppOs os;
    private String minimumVersion;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    AppVersion(Long id, AppOs os, String minimumVersion) {
        this.id = id;
        this.os = os;
        this.minimumVersion = minimumVersion;
    }

    public boolean requiresUpdateFrom(SemanticVersion clientVersion) {
        return clientVersion.isLowerThan(SemanticVersion.from(minimumVersion));
    }
}
