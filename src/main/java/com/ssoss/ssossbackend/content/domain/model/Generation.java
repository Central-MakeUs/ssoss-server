package com.ssoss.ssossbackend.content.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
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
    private static final String OTHER_CHANNELS_DESCRIPTION = " 외 %d건";
    private static final String DEDUCTION_DESCRIPTION_SUFFIX = " 콘텐츠 생성";

    @Id
    private Long id;
    private Long memberId;
    private String channels;
    private Purpose purpose;
    private Tone tone;
    private String emphasis;
    private String forbidden;
    private Keywords keywords;
    private boolean photoGuideChecked;
    private Long sourceContentChannelId;

    @CreatedDate
    private Instant createdAt;

    private Instant finishedAt;

    Generation(Long id, Long memberId, String channels, Purpose purpose, Tone tone, String emphasis, String forbidden,
        Keywords keywords, boolean photoGuideChecked, Long sourceContentChannelId,
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
        this.sourceContentChannelId = sourceContentChannelId;
        this.createdAt = createdAt;
        this.finishedAt = finishedAt;
    }

    private static Generation of(Long memberId, List<Channel> channels, Purpose purpose, Tone tone, String emphasis,
        String forbidden, List<String> keywords, boolean photoGuideChecked, Long sourceContentChannelId) {
        String joined = channels.stream()
            .map(Channel::name)
            .collect(Collectors.joining(CHANNEL_SEPARATOR));
        return new Generation(null, memberId, joined, purpose, tone, emphasis, forbidden, new Keywords(keywords),
            photoGuideChecked, sourceContentChannelId, null, null);
    }

    public static Generation create(Long memberId, List<Channel> channels, Purpose purpose, Tone tone,
        String emphasis, String forbidden, List<String> keywords, boolean photoGuideChecked) {
        return of(memberId, channels, purpose, tone, emphasis, forbidden, keywords, photoGuideChecked, null);
    }

    public static Generation reuseOf(Content content, ContentChannel origin, String emphasis, String forbidden,
        List<String> keywords, boolean photoGuideChecked) {
        return of(content.getMemberId(), List.of(origin.getChannel()), content.getPurpose(), content.getTone(),
            emphasis, forbidden, keywords, photoGuideChecked, origin.getId());
    }

    public static Generation conversionOf(Generation originGeneration, ContentChannel origin, List<Channel> channels) {
        return of(originGeneration.memberId, channels, originGeneration.purpose, originGeneration.tone,
            originGeneration.emphasis, originGeneration.forbidden, originGeneration.keywordList(),
            originGeneration.photoGuideChecked, origin.getId());
    }

    public List<Channel> channelList() {
        return Arrays.stream(channels.split(CHANNEL_SEPARATOR))
            .map(Channel::valueOf)
            .toList();
    }

    public String deductionDescription() {
        List<Channel> chosen = channelList();
        String leading = chosen.stream().min(Comparator.naturalOrder()).orElseThrow().displayName();
        if (chosen.size() == 1) {
            return leading + DEDUCTION_DESCRIPTION_SUFFIX;
        }
        return leading + OTHER_CHANNELS_DESCRIPTION.formatted(chosen.size() - 1) + DEDUCTION_DESCRIPTION_SUFFIX;
    }

    public List<String> keywordList() {
        return keywords == null ? List.of() : keywords.values();
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

    public GenerationMaterial materialFor(Channel channel, StoreMaterial store, StyleSource styleSource) {
        return new GenerationMaterial(channel, purpose, tone, emphasis, forbidden, keywordList(), photoGuideChecked,
            store, styleSource);
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
