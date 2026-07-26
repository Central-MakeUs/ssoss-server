package com.ssoss.ssossbackend.content.domain.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ssoss.ssossbackend.content.domain.contract.ContentChannelHistoryRepository;
import com.ssoss.ssossbackend.content.domain.contract.ContentChannelRepository;
import com.ssoss.ssossbackend.content.domain.contract.ContentRepository;
import com.ssoss.ssossbackend.content.domain.model.Content;
import com.ssoss.ssossbackend.content.domain.model.ContentChannel;
import com.ssoss.ssossbackend.content.domain.model.ContentChannelHistory;
import com.ssoss.ssossbackend.content.domain.model.ContentErrorCode;
import com.ssoss.ssossbackend.content.domain.model.ContentSource;
import com.ssoss.ssossbackend.content.domain.model.ContentWithChannels;
import com.ssoss.ssossbackend.content.domain.model.Generation;
import com.ssoss.ssossbackend.content.domain.model.GenerationResult;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentWriter {

    private final ContentRepository contentRepository;
    private final ContentChannelRepository contentChannelRepository;
    private final ContentChannelHistoryRepository contentChannelHistoryRepository;
    private final Clock clock;

    @Transactional
    public ContentChannel edit(ContentChannel channel, String title, String body, List<String> hashtags) {
        ContentChannelHistory previous = ContentChannelHistory.previousOf(channel);
        if (!channel.edit(title, body, hashtags)) {
            return channel;
        }
        contentChannelHistoryRepository.save(previous);
        return contentChannelRepository.save(channel);
    }

    @Transactional
    public void delete(ContentWithChannels content) {
        Instant deletedAt = clock.instant();
        content.content().delete(deletedAt);
        content.channels().forEach(channel -> channel.delete(deletedAt));
        contentRepository.save(content.content());
        contentChannelRepository.saveAll(content.channels());
    }

    @Transactional
    public ContentWithChannels save(Generation generation, List<GenerationResult> results) {
        Content content = contentRepository.findBySourceTypeAndSourceId(ContentSource.GENERATION, generation.getId())
            .orElseGet(() -> contentRepository.save(Content.copyOf(generation)));
        if (content.isDeleted()) {
            throw new BusinessException(ContentErrorCode.CONTENT_DELETED);
        }
        List<ContentChannel> saved = contentChannelRepository.findAllByContentId(content.getId());
        Set<Long> savedResultIds = saved.stream()
            .map(ContentChannel::getSourceGenerationResultId)
            .collect(Collectors.toSet());
        List<ContentChannel> added = contentChannelRepository.saveAll(results.stream()
            .filter(result -> !savedResultIds.contains(result.getId()))
            .map(result -> ContentChannel.copyOf(content, result))
            .toList());
        return new ContentWithChannels(content, Stream.concat(saved.stream(), added.stream())
            .sorted(ContentChannel.CHANNEL_ORDER)
            .toList());
    }
}
