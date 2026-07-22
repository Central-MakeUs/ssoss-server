package com.ssoss.ssossbackend.content.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("generation")
public class Generation {

    public static final Duration DEADLINE = Duration.ofSeconds(60);

    private static final String CHANNEL_SEPARATOR = ",";

    @Id
    private Long id;
    private Long memberId;
    private String channels;
    private Purpose purpose;
    private Tone tone;
    private String emphasis;
    private String forbidden;
    private String keywords;
    private boolean photoGuideChecked;
    private Long sourceSavedContentId;

    @CreatedDate
    private Instant createdAt;

    private Instant finishedAt;

    Generation(Long id, Long memberId, String channels, Purpose purpose, Tone tone,
        String emphasis, String forbidden, String keywords, boolean photoGuideChecked, Long sourceSavedContentId,
        Instant createdAt, Instant finishedAt) {
        this.id = id;
        this.memberId = memberId;
        this.channels = channels;
        this.purpose = purpose;
        this.tone = tone;
        this.emphasis = emphasis;
        this.forbidden = forbidden;
        this.keywords = keywords;
        this.photoGuideChecked = photoGuideChecked;
        this.sourceSavedContentId = sourceSavedContentId;
        this.createdAt = createdAt;
        this.finishedAt = finishedAt;
    }

    public static Generation create(Long memberId, List<Channel> channels, Purpose purpose, Tone tone,
        String emphasis, String forbidden, String keywords) {
        String joined = channels.stream()
            .map(Channel::name)
            .collect(Collectors.joining(CHANNEL_SEPARATOR));
        return new Generation(null, memberId, joined, purpose, tone, emphasis, forbidden, keywords, false, null,
            null, null);
    }

    public List<Channel> channelList() {
        return Arrays.stream(channels.split(CHANNEL_SEPARATOR))
            .map(Channel::valueOf)
            .toList();
    }

    public List<Channel> pendingChannels(Collection<Channel> settledChannels) {
        return channelList().stream()
            .filter(channel -> !settledChannels.contains(channel))
            .toList();
    }

    public GenerationMaterial materialFor(Channel channel) {
        return new GenerationMaterial(channel, purpose, tone, emphasis, forbidden, keywords);
    }

    public Instant deadline() {
        return createdAt.plus(DEADLINE);
    }

    public Duration deadlineBudget(Instant now) {
        if (isExpired(now)) {
            return Duration.ZERO;
        }
        return Duration.between(now, deadline());
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(deadline());
    }

    public boolean finish(Instant now) {
        if (isExpired(now)) {
            return false;
        }
        this.finishedAt = now;
        return true;
    }

    public GenerationStatus status(Instant now, Collection<Channel> settledChannels) {
        if (finishedAt != null && !finishedAt.isAfter(deadline())) {
            return settledChannels.isEmpty() ? GenerationStatus.FAILED : GenerationStatus.SUCCEEDED;
        }
        if (isExpired(now)) {
            return GenerationStatus.FAILED;
        }
        return GenerationStatus.IN_PROGRESS;
    }
}
