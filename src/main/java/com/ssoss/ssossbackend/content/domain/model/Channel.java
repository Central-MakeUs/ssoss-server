package com.ssoss.ssossbackend.content.domain.model;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.ssoss.ssossbackend.shared.exception.BusinessException;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;

public enum Channel {

    BLOG(true, "블로그"),
    INSTAGRAM(false, "인스타그램"),
    DAANGN_BIZ(false, "당근 비즈"),
    THREADS(false, "스레드");

    private final boolean titled;
    private final String displayName;

    Channel(boolean titled, String displayName) {
        this.titled = titled;
        this.displayName = displayName;
    }

    public boolean hasTitle() {
        return titled;
    }

    public String displayName() {
        return displayName;
    }

    public void ensureTitleAllowed(String title) {
        if (titled && title == null) {
            throw new BusinessException(ContentErrorCode.TITLE_REQUIRED);
        }
        if (!titled && title != null) {
            throw new BusinessException(ContentErrorCode.TITLE_NOT_ALLOWED);
        }
    }

    public static Channel from(String value) {
        return Arrays.stream(values())
            .filter(channel -> channel.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_INPUT));
    }

    public static List<Channel> listFrom(List<String> values) {
        List<Channel> channels = values.stream().map(Channel::from).toList();
        if (Set.copyOf(channels).size() != channels.size()) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT);
        }
        return channels;
    }
}
