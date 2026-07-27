package com.ssoss.ssossbackend.content.domain.model;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.StringUtils;

@Getter
@Table("content_channel")
public class ContentChannel {

    public static final Comparator<ContentChannel> CHANNEL_ORDER = Comparator.comparing(ContentChannel::getChannel);

    @Id
    private Long id;
    private Long contentId;
    private Long memberId;
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

    ContentChannel(Long id, Long contentId, Long memberId, Channel channel, Long sourceGenerationResultId,
        String title, String body, Hashtags hashtags, Instant createdAt, Instant updatedAt, Instant deletedAt) {
        this.id = id;
        this.contentId = contentId;
        this.memberId = memberId;
        this.channel = channel;
        this.sourceGenerationResultId = sourceGenerationResultId;
        this.title = title;
        this.body = body;
        this.hashtags = hashtags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static ContentChannel of(Content content, GenerationResult result, ContentChannelDraft draft) {
        result.getChannel().ensureTitleAllowed(draft.title());
        return new ContentChannel(null, content.getId(), content.getMemberId(), result.getChannel(), result.getId(),
            draft.title(), draft.body(), new Hashtags(draft.hashtags()), null, null, null);
    }

    public boolean edit(String title, String body, List<String> hashtags) {
        String editedTitle = StringUtils.hasText(title) ? title : null;
        channel.ensureTitleAllowed(editedTitle);
        if (Objects.equals(this.title, editedTitle) && Objects.equals(this.body, body)
            && hashtagList().equals(hashtags)) {
            return false;
        }
        this.title = editedTitle;
        this.body = body;
        this.hashtags = new Hashtags(hashtags);
        return true;
    }

    public void delete(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public List<String> hashtagList() {
        return hashtags == null ? List.of() : hashtags.values();
    }
}
