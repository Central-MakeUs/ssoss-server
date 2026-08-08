package com.ssoss.ssossbackend.template.domain.model;

import java.time.Instant;
import java.util.List;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("saved_template")
public class SavedTemplate {

    @Id
    private Long id;
    private Long memberId;
    private Long templateId;
    private TemplateCategory category;
    private String title;
    private String description;
    private String body;
    private RecommendedChannels recommendedChannels;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private Instant deletedAt;

    SavedTemplate(Long id, Long memberId, Long templateId, TemplateCategory category, String title,
        String description, String body, RecommendedChannels recommendedChannels, Instant deletedAt) {
        this.id = id;
        this.memberId = memberId;
        this.templateId = templateId;
        this.category = category;
        this.title = title;
        this.description = description;
        this.body = body;
        this.recommendedChannels = recommendedChannels;
        this.deletedAt = deletedAt;
    }

    public List<Channel> recommendedChannelList() {
        return recommendedChannels == null ? List.of() : recommendedChannels.values();
    }

    public static SavedTemplate copyOf(Template template, Long memberId, String body) {
        return new SavedTemplate(null, memberId, template.getId(), template.getCategory(), template.getTitle(),
            template.getDescription(), body, template.getRecommendedChannels(), null);
    }
}
