package com.ssoss.ssossbackend.store.application.command;

import java.util.List;

import com.ssoss.ssossbackend.store.domain.model.StoreKeywords;
import com.ssoss.ssossbackend.store.domain.model.Tone;

import org.springframework.util.StringUtils;

public record StoreContentInfoCommand(
    Long memberId,
    String strength,
    StoreKeywords keywords,
    String forbidden,
    Tone tone
) {

    public static StoreContentInfoCommand of(Long memberId, String strength, List<String> keywords, String forbidden,
        String tone) {
        return new StoreContentInfoCommand(memberId, strength, new StoreKeywords(keywords), forbidden,
            StringUtils.hasText(tone) ? Tone.from(tone) : null);
    }
}
