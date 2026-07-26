package com.ssoss.ssossbackend.content.domain.model;

import java.time.Instant;
import java.util.List;

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
    private ContentSource sourceType;
    private Long sourceId;
    private Purpose purpose;
    private Tone tone;
    private Keywords keywords;

    @CreatedDate
    private Instant createdAt;

    private Instant deletedAt;

    Content(Long id, Long memberId, ContentSource sourceType, Long sourceId, Purpose purpose, Tone tone,
        Keywords keywords, Instant createdAt, Instant deletedAt) {
        this.id = id;
        this.memberId = memberId;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.purpose = purpose;
        this.tone = tone;
        this.keywords = keywords;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public static Content copyOf(Generation generation) {
        return new Content(null, generation.getMemberId(), ContentSource.GENERATION, generation.getId(),
            generation.getPurpose(), generation.getTone(), generation.getKeywords(), null, null);
    }

    public void delete(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public List<String> keywordList() {
        return keywords == null ? List.of() : keywords.values();
    }
}
