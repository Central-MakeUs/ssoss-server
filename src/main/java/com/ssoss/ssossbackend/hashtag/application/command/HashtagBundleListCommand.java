package com.ssoss.ssossbackend.hashtag.application.command;

import org.springframework.util.StringUtils;

public record HashtagBundleListCommand(Long memberId, String keyword, int page, int size) {

    public static HashtagBundleListCommand of(Long memberId, String keyword, int page, int size) {
        return new HashtagBundleListCommand(
            memberId,
            StringUtils.hasText(keyword) ? keyword.strip() : null,
            page,
            size);
    }
}
