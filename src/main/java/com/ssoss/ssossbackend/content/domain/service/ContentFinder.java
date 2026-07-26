package com.ssoss.ssossbackend.content.domain.service;

import java.util.Map;
import java.util.stream.Collectors;

import com.ssoss.ssossbackend.content.domain.contract.ContentChannelRepository;
import com.ssoss.ssossbackend.content.domain.contract.ContentRepository;
import com.ssoss.ssossbackend.content.domain.model.Channel;
import com.ssoss.ssossbackend.content.domain.model.Content;
import com.ssoss.ssossbackend.content.domain.model.ContentCard;
import com.ssoss.ssossbackend.content.domain.model.ContentChannel;
import com.ssoss.ssossbackend.content.domain.model.ContentChannelView;
import com.ssoss.ssossbackend.content.domain.model.ContentErrorCode;
import com.ssoss.ssossbackend.content.domain.model.ContentWithChannels;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentFinder {

    private static final Sort SAVED_AT_DESC = Sort.by(Sort.Direction.DESC, "createdAt", "id");

    private final ContentRepository contentRepository;
    private final ContentChannelRepository contentChannelRepository;
    private final ContentCardAssembler contentCardAssembler;

    @Transactional(readOnly = true)
    public Page<ContentCard> list(Long memberId, Channel channel, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, SAVED_AT_DESC);
        if (channel == null) {
            return contentCardAssembler
                .assemble(contentRepository.findAllByMemberIdAndDeletedAtIsNull(memberId, pageable));
        }
        Page<ContentChannelView> filtered = contentChannelRepository
            .findViewsByMemberIdAndChannelAndDeletedAtIsNull(memberId, channel, pageable);
        Map<Long, Content> parents = contentRepository
            .findAllById(filtered.stream().map(ContentChannelView::contentId).toList()).stream()
            .collect(Collectors.toMap(Content::getId, parent -> parent));
        return contentCardAssembler.assemble(filtered.map(view -> parents.get(view.contentId())));
    }

    public ContentWithChannels get(Long contentId, Long memberId) {
        Content content = contentRepository.findByIdAndMemberIdAndDeletedAtIsNull(contentId, memberId)
            .orElseThrow(() -> new BusinessException(ContentErrorCode.CONTENT_NOT_FOUND));
        return new ContentWithChannels(content, contentChannelRepository
            .findAllByContentIdAndDeletedAtIsNull(content.getId()).stream()
            .sorted(ContentChannel.CHANNEL_ORDER)
            .toList());
    }

    public ContentChannel channelOf(Long contentId, Long contentChannelId, Long memberId) {
        return get(contentId, memberId).channels().stream()
            .filter(channel -> channel.getId().equals(contentChannelId))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ContentErrorCode.CONTENT_CHANNEL_NOT_FOUND));
    }
}
