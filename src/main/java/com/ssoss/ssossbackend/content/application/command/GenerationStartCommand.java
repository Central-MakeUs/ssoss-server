package com.ssoss.ssossbackend.content.application.command;

import java.util.List;
import java.util.Set;

import com.ssoss.ssossbackend.content.domain.model.Channel;
import com.ssoss.ssossbackend.content.domain.model.Purpose;
import com.ssoss.ssossbackend.content.domain.model.Tone;
import com.ssoss.ssossbackend.shared.exception.BusinessException;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;

public record GenerationStartCommand(
    Long memberId,
    List<Channel> channels,
    Purpose purpose,
    Tone tone,
    String emphasis,
    String forbidden,
    String keywords
) {

    public static GenerationStartCommand of(Long memberId, List<String> channels, String purpose, String tone,
        String emphasis, String forbidden, String keywords) {
        List<Channel> parsedChannels = channels.stream().map(Channel::from).toList();
        if (Set.copyOf(parsedChannels).size() != parsedChannels.size()) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT);
        }
        return new GenerationStartCommand(
            memberId,
            parsedChannels,
            Purpose.from(purpose),
            Tone.from(tone),
            emphasis,
            forbidden,
            keywords);
    }
}
