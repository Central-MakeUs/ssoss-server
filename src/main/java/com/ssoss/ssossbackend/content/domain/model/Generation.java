package com.ssoss.ssossbackend.content.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Getter
@Table("generation")
public class Generation {

    public static final Duration DEADLINE = Duration.ofSeconds(60);

    private static final String CHANNEL_SEPARATOR = ",";

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

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
    private Long sourceContentId;

    @CreatedDate
    private Instant createdAt;

    private Instant finishedAt;

    Generation(Long id, Long memberId, String channels, Purpose purpose, Tone tone,
        String emphasis, String forbidden, String keywords, boolean photoGuideChecked, Long sourceContentId,
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
        this.sourceContentId = sourceContentId;
        this.createdAt = createdAt;
        this.finishedAt = finishedAt;
    }

    public static Generation create(Long memberId, List<Channel> channels, Purpose purpose, Tone tone,
        String emphasis, String forbidden, List<String> keywords, boolean photoGuideChecked) {
        String joined = channels.stream()
            .map(Channel::name)
            .collect(Collectors.joining(CHANNEL_SEPARATOR));
        String serializedKeywords = keywords == null || keywords.isEmpty()
            ? null
            : JSON_MAPPER.writeValueAsString(keywords);
        return new Generation(null, memberId, joined, purpose, tone, emphasis, forbidden, serializedKeywords,
            photoGuideChecked, null, null, null);
    }

    public List<Channel> channelList() {
        return Arrays.stream(channels.split(CHANNEL_SEPARATOR))
            .map(Channel::valueOf)
            .toList();
    }

    public List<String> keywordList() {
        if (keywords == null || keywords.isBlank()) {
            return List.of();
        }
        return JSON_MAPPER.readValue(keywords, new TypeReference<List<String>>() {
        });
    }

    public List<ChannelResult> channelResults(Instant now, List<GenerationResult> results) {
        if (status(now, results) != GenerationStatus.SUCCEEDED) {
            return List.of();
        }
        return channelList().stream()
            .flatMap(channel -> results.stream().filter(result -> result.getChannel() == channel))
            .map(ChannelResult::from)
            .toList();
    }

    public GenerationMaterial materialFor(Channel channel) {
        return new GenerationMaterial(channel, purpose, tone, emphasis, forbidden, keywordList(), photoGuideChecked);
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

    public boolean isClosed(Instant now) {
        return finishedAt != null || isExpired(now);
    }

    public GenerationStatus status(Instant now, List<GenerationResult> results) {
        if (!isClosed(now)) {
            return GenerationStatus.IN_PROGRESS;
        }
        boolean everyChannelSucceeded = finishedAt != null && channelList().stream()
            .allMatch(channel -> results.stream()
                .anyMatch(result -> result.getChannel() == channel && result.isSucceeded()));
        return everyChannelSucceeded ? GenerationStatus.SUCCEEDED : GenerationStatus.FAILED;
    }

    public boolean finish(Instant now) {
        if (isExpired(now)) {
            return false;
        }
        this.finishedAt = now;
        return true;
    }
}
