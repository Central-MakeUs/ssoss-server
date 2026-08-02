package com.ssoss.ssossbackend.content.domain.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ssoss.ssossbackend.content.domain.contract.ContentChannelRepository;
import com.ssoss.ssossbackend.content.domain.model.Content;
import com.ssoss.ssossbackend.content.domain.model.ContentCard;
import com.ssoss.ssossbackend.content.domain.model.ContentChannel;
import com.ssoss.ssossbackend.content.domain.model.ContentChannelView;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentCardAssembler {

    private final ContentChannelRepository contentChannelRepository;

    public Page<ContentCard> assemble(Page<Content> contents) {
        List<Long> contentIds = contents.stream().map(Content::getId).toList();
        Map<Long, List<ContentChannelView>> viewsByContentId = contentIds.isEmpty() ? Map.of()
            : contentChannelRepository.findViewsByContentIdInAndDeletedAtIsNull(contentIds).stream()
                .sorted(ContentChannelView.CHANNEL_ORDER)
                .collect(Collectors.groupingBy(ContentChannelView::contentId));
        Map<Long, ContentChannel> representatives = viewsByContentId.isEmpty() ? Map.of()
            : contentChannelRepository
                .findAllById(viewsByContentId.values().stream().map(views -> views.getFirst().id()).toList()).stream()
                .collect(Collectors.toMap(ContentChannel::getContentId, representative -> representative));
        return new PageImpl<>(contents.stream()
            .filter(content -> viewsByContentId.containsKey(content.getId()))
            .map(content -> ContentCard.of(content, viewsByContentId.get(content.getId()),
                representatives.get(content.getId())))
            .toList(), contents.getPageable(), contents.getTotalElements());
    }
}
