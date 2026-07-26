package com.ssoss.ssossbackend.content.application.command;

import com.ssoss.ssossbackend.content.domain.model.Channel;

import org.springframework.util.StringUtils;

public record ContentListCommand(Long memberId, Channel channel, int page, int size) {

    public static ContentListCommand of(Long memberId, String channel, int page, int size) {
        return new ContentListCommand(
            memberId, StringUtils.hasText(channel) ? Channel.from(channel) : null, page, size);
    }
}
