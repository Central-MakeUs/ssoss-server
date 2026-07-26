package com.ssoss.ssossbackend.content.domain.model;

import java.time.Instant;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("content_channel_history")
public class ContentChannelHistory {

    @Id
    private Long id;
    private Long contentChannelId;
    private String title;
    private String body;
    private Hashtags hashtags;

    @CreatedDate
    private Instant createdAt;

    ContentChannelHistory(Long id, Long contentChannelId, String title, String body, Hashtags hashtags,
        Instant createdAt) {
        this.id = id;
        this.contentChannelId = contentChannelId;
        this.title = title;
        this.body = body;
        this.hashtags = hashtags;
        this.createdAt = createdAt;
    }

    public static ContentChannelHistory previousOf(ContentChannel channel) {
        return new ContentChannelHistory(null, channel.getId(), channel.getTitle(), channel.getBody(),
            channel.getHashtags(), null);
    }
}
