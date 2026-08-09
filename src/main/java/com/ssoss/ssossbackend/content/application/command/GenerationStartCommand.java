package com.ssoss.ssossbackend.content.application.command;

import java.util.List;

import com.ssoss.ssossbackend.content.domain.model.Channel;
import com.ssoss.ssossbackend.content.domain.model.Purpose;
import com.ssoss.ssossbackend.content.domain.model.Tone;

public record GenerationStartCommand(
    Long memberId,
    List<Channel> channels,
    Purpose purpose,
    Tone tone,
    String emphasis,
    String forbidden,
    List<String> keywords,
    boolean photoGuideChecked
) {

    public static GenerationStartCommand of(Long memberId, List<String> channels, String purpose, String tone,
        String emphasis, String forbidden, List<String> keywords, Boolean photoGuideChecked) {
        return new GenerationStartCommand(
            memberId,
            Channel.listFrom(channels),
            Purpose.from(purpose),
            Tone.from(tone),
            emphasis,
            forbidden,
            keywords,
            Boolean.TRUE.equals(photoGuideChecked));
    }
}
