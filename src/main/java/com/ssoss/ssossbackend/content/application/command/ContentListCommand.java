package com.ssoss.ssossbackend.content.application.command;

import com.ssoss.ssossbackend.content.domain.model.Channel;
import com.ssoss.ssossbackend.shared.paging.CreatedAtSort;

import org.springframework.util.StringUtils;

public record ContentListCommand(Long memberId, Channel channel, CreatedAtSort sort, int page, int size) {

    public static ContentListCommand of(Long memberId, String channel, String sort, int page, int size) {
        return new ContentListCommand(
            memberId,
            StringUtils.hasText(channel) ? Channel.from(channel) : null,
            StringUtils.hasText(sort) ? CreatedAtSort.from(sort) : CreatedAtSort.LATEST,
            page,
            size);
    }
}
