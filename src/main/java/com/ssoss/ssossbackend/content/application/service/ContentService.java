package com.ssoss.ssossbackend.content.application.service;

import java.util.List;

import com.ssoss.ssossbackend.content.application.command.ContentChannelEditCommand;
import com.ssoss.ssossbackend.content.application.command.ContentListCommand;
import com.ssoss.ssossbackend.content.application.command.ContentSaveCommand;
import com.ssoss.ssossbackend.content.application.result.ContentChannelResult;
import com.ssoss.ssossbackend.content.application.result.ContentDetailResult;
import com.ssoss.ssossbackend.content.application.result.ContentSaveResult;
import com.ssoss.ssossbackend.content.application.result.ContentSummaryResult;
import com.ssoss.ssossbackend.content.domain.model.Content;
import com.ssoss.ssossbackend.content.domain.model.ContentChannel;
import com.ssoss.ssossbackend.content.domain.model.ContentWithChannels;
import com.ssoss.ssossbackend.content.domain.model.Generation;
import com.ssoss.ssossbackend.content.domain.model.GenerationResult;
import com.ssoss.ssossbackend.content.domain.service.ContentFinder;
import com.ssoss.ssossbackend.content.domain.service.ContentWriter;
import com.ssoss.ssossbackend.content.domain.service.GenerationFinder;
import com.ssoss.ssossbackend.content.domain.service.GenerationValidator;
import com.ssoss.ssossbackend.content.domain.service.GenerationWriter;
import com.ssoss.ssossbackend.shared.paging.PagedResult;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final GenerationFinder generationFinder;
    private final GenerationValidator generationValidator;
    private final GenerationWriter generationWriter;
    private final ContentWriter contentWriter;
    private final ContentFinder contentFinder;

    public ContentSaveResult save(ContentSaveCommand command) {
        Generation generation = generationFinder.get(command.generationId(), command.memberId());
        List<GenerationResult> results = generationFinder.results(generation.getId());
        generationValidator.ensureSavable(generation, results);
        ContentWithChannels saved = contentWriter.save(generation, results, command.contents());
        return new ContentSaveResult(saved.content().getId(), saved.channels().stream()
            .map(channel -> new ContentSaveResult.Item(channel.getId(), channel.getChannel().name()))
            .toList());
    }

    public ContentChannelResult edit(ContentChannelEditCommand command) {
        ContentChannel channel = contentFinder.channelOf(
            command.contentId(), command.contentChannelId(), command.memberId());
        return ContentChannelResult.from(
            contentWriter.edit(channel, command.title(), command.body(), command.hashtags()));
    }

    public void delete(Long contentId, Long memberId) {
        contentWriter.delete(contentFinder.get(contentId, memberId));
    }

    public void deleteAllByMemberId(Long memberId) {
        contentWriter.deleteAllByMemberId(memberId);
        generationWriter.deleteAllByMemberId(memberId);
    }

    public PagedResult<ContentSummaryResult> list(ContentListCommand command) {
        return PagedResult.from(contentFinder.list(
                command.memberId(), command.channel(), command.sort(), command.page(), command.size()),
            ContentSummaryResult::from);
    }

    public ContentDetailResult getById(Long contentId, Long memberId) {
        ContentWithChannels found = contentFinder.get(contentId, memberId);
        Content content = found.content();
        return new ContentDetailResult(
            content.getId(),
            content.getPurpose().name(),
            content.getTone().name(),
            content.keywordList(),
            found.channels().stream()
                .map(ContentChannelResult::from)
                .toList());
    }
}
