package com.ssoss.ssossbackend.content.domain.model;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("content_channel")
public class ContentChannel {

    public static final Comparator<ContentChannel> CHANNEL_ORDER = Comparator.comparing(ContentChannel::getChannel);

    @Id
    private Long id;
    private Long contentId;
    private Channel channel;
    private Long sourceGenerationResultId;
    private String title;
    private String body;
    private Hashtags hashtags;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private Instant deletedAt;

    ContentChannel(Long id, Long contentId, Channel channel, Long sourceGenerationResultId,
        String title, String body, Hashtags hashtags, Instant createdAt, Instant updatedAt, Instant deletedAt) {
        this.id = id;
        this.contentId = contentId;
        this.channel = channel;
        this.sourceGenerationResultId = sourceGenerationResultId;
        this.title = title;
        this.body = body;
        this.hashtags = hashtags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static ContentChannel copyOf(Long contentId, GenerationResult result) {
        return new ContentChannel(null, contentId, result.getChannel(), result.getId(),
            result.getTitle(), result.getBody(), result.getHashtags(), null, null, null);
    }

    public List<String> hashtagList() {
        return hashtags == null ? List.of() : hashtags.values();
    }
}
