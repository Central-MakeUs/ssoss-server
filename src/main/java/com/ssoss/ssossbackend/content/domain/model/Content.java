package com.ssoss.ssossbackend.content.domain.model;

import java.time.Instant;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("content")
public class Content {

    @Id
    private Long id;
    private Long memberId;
    private Long generationId;
    private Long generationResultId;
    private Channel channel;
    private String title;
    private String body;
    private String hashtags;

    @CreatedDate
    private Instant createdAt;

    private Instant deletedAt;

    Content(Long id, Long memberId, Long generationId, Long generationResultId, Channel channel,
        String title, String body, String hashtags, Instant createdAt, Instant deletedAt) {
        this.id = id;
        this.memberId = memberId;
        this.generationId = generationId;
        this.generationResultId = generationResultId;
        this.channel = channel;
        this.title = title;
        this.body = body;
        this.hashtags = hashtags;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public static Content copyOf(Long memberId, GenerationResult result) {
        return new Content(null, memberId, result.getGenerationId(), result.getId(), result.getChannel(),
            result.getTitle(), result.getBody(), result.getHashtags(), null, null);
    }
}
