package com.ssoss.ssossbackend.content.application.command;

import com.ssoss.ssossbackend.content.domain.model.Channel;
import com.ssoss.ssossbackend.content.domain.model.ContentSort;

import org.springframework.util.StringUtils;

public record ContentListCommand(Long memberId, Channel channel, ContentSort sort, int page, int size) {

    public static ContentListCommand of(Long memberId, String channel, String sort, int page, int size) {
        return new ContentListCommand(
            memberId,
            StringUtils.hasText(channel) ? Channel.from(channel) : null,
            StringUtils.hasText(sort) ? ContentSort.from(sort) : ContentSort.LATEST,
            page,
            size);
    }
}
