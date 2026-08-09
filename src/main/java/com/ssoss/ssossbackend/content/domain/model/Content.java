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
    private String name;
    private Long generationId;
    private Purpose purpose;
    private Tone tone;
    private Keywords keywords;

    @CreatedDate
    private Instant createdAt;

    private Instant deletedAt;

    Content(Long id, Long memberId, String name, Long generationId, Purpose purpose, Tone tone,
        Keywords keywords, Instant createdAt, Instant deletedAt) {
        this.id = id;
        this.memberId = memberId;
        this.name = name;
        this.generationId = generationId;
        this.purpose = purpose;
        this.tone = tone;
        this.keywords = keywords;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public static Content copyOf(Generation generation, ContentName name) {
        return new Content(null, generation.getMemberId(), name.value(), generation.getId(),
            generation.getPurpose(), generation.getTone(), generation.getKeywords(), null, null);
    }

    public void rename(String name) {
        this.name = name;
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
