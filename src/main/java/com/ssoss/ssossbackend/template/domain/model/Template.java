package com.ssoss.ssossbackend.template.domain.model;

import java.time.Instant;
import java.util.List;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("template")
public class Template {

    @Id
    private Long id;
    private TemplateCategory category;
    private String title;
    private String description;
    private String body;
    private String exampleBody;
    private RecommendedChannels recommendedChannels;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    Template(Long id, TemplateCategory category, String title, String description, String body, String exampleBody,
        RecommendedChannels recommendedChannels) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.description = description;
        this.body = body;
        this.exampleBody = exampleBody;
        this.recommendedChannels = recommendedChannels;
    }

    public List<Channel> recommendedChannelList() {
        return recommendedChannels == null ? List.of() : recommendedChannels.values();
    }
}
