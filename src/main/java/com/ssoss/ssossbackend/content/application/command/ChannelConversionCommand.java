package com.ssoss.ssossbackend.content.application.command;

import java.util.List;

import com.ssoss.ssossbackend.content.domain.model.Channel;

public record ChannelConversionCommand(
    Long memberId,
    Long contentId,
    Long contentChannelId,
    List<Channel> channels
) {

    public ChannelConversionCommand {
        channels = List.copyOf(channels);
    }

    public static ChannelConversionCommand of(Long memberId, Long contentId, Long contentChannelId,
        List<String> channels) {
        return new ChannelConversionCommand(memberId, contentId, contentChannelId, Channel.listFrom(channels));
    }
}
