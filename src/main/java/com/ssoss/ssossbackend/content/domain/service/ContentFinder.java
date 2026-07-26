package com.ssoss.ssossbackend.content.domain.service;

import java.util.List;

import com.ssoss.ssossbackend.content.domain.contract.ContentChannelRepository;
import com.ssoss.ssossbackend.content.domain.contract.ContentRepository;
import com.ssoss.ssossbackend.content.domain.model.Content;
import com.ssoss.ssossbackend.content.domain.model.ContentChannel;
import com.ssoss.ssossbackend.content.domain.model.ContentErrorCode;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentFinder {

    private final ContentRepository contentRepository;
    private final ContentChannelRepository contentChannelRepository;

    public Content get(Long contentId, Long memberId) {
        return contentRepository.findByIdAndMemberId(contentId, memberId)
            .orElseThrow(() -> new BusinessException(ContentErrorCode.CONTENT_NOT_FOUND));
    }

    public List<ContentChannel> channelsOf(Long contentId) {
        return contentChannelRepository.findAllByContentIdAndDeletedAtIsNull(contentId).stream()
            .sorted(ContentChannel.CHANNEL_ORDER)
            .toList();
    }
}
