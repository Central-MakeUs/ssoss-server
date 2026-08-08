package com.ssoss.ssossbackend.template.domain.model;

import java.time.Instant;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("saved_template_history")
public class SavedTemplateHistory {

    @Id
    private Long id;
    private Long savedTemplateId;
    private String title;
    private String body;

    @CreatedDate
    private Instant createdAt;

    SavedTemplateHistory(Long id, Long savedTemplateId, String title, String body, Instant createdAt) {
        this.id = id;
        this.savedTemplateId = savedTemplateId;
        this.title = title;
        this.body = body;
        this.createdAt = createdAt;
    }

    public static SavedTemplateHistory previousOf(SavedTemplate savedTemplate) {
        return new SavedTemplateHistory(null, savedTemplate.getId(), savedTemplate.getTitle(),
            savedTemplate.getBody(), null);
    }
}
