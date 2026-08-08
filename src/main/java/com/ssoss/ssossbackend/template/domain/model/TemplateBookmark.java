package com.ssoss.ssossbackend.template.domain.model;

import java.time.Instant;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("template_bookmark")
public class TemplateBookmark {

    @Id
    private Long id;
    private Long memberId;
    private Long templateId;
    private Instant bookmarkedAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    TemplateBookmark(Long id, Long memberId, Long templateId, Instant bookmarkedAt) {
        this.id = id;
        this.memberId = memberId;
        this.templateId = templateId;
        this.bookmarkedAt = bookmarkedAt;
    }

    public static TemplateBookmark create(Long memberId, Long templateId) {
        return new TemplateBookmark(null, memberId, templateId, null);
    }

    public boolean bookmark(Instant bookmarkedAt) {
        if (this.bookmarkedAt != null) {
            return false;
        }
        this.bookmarkedAt = bookmarkedAt;
        return true;
    }

    public boolean unbookmark() {
        if (this.bookmarkedAt == null) {
            return false;
        }
        this.bookmarkedAt = null;
        return true;
    }
}
