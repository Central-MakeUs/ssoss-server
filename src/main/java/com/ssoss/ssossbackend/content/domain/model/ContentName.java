package com.ssoss.ssossbackend.content.domain.model;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import com.ssoss.ssossbackend.shared.exception.BusinessException;

public record ContentName(String value) {

    private static final Pattern WHITESPACE_RUN = Pattern.compile("\\s+");
    private static final String ELLIPSIS = "…";
    private static final int LIMIT = 20;
    private static final int TRUNCATED_LIMIT = LIMIT - ELLIPSIS.codePointCount(0, ELLIPSIS.length());

    private static ContentName of(Channel channel, String title, String body) {
        String source = WHITESPACE_RUN
            .matcher(channel.hasTitle() && title != null ? title : PhotoGuideTag.removeFrom(body))
            .replaceAll(" ")
            .strip();
        return new ContentName(source.codePointCount(0, source.length()) <= LIMIT
            ? source
            : source.substring(0, source.offsetByCodePoints(0, TRUNCATED_LIMIT)) + ELLIPSIS);
    }

    public static ContentName of(ContentChannel channel) {
        return of(channel.getChannel(), channel.getTitle(), channel.getBody());
    }

    public static ContentName from(List<ContentChannelDraft> drafts) {
        ContentChannelDraft representative = drafts.stream()
            .min(Comparator.comparing(ContentChannelDraft::channel))
            .orElseThrow(() -> new BusinessException(ContentErrorCode.SAVE_CHANNELS_MISMATCHED));
        return of(representative.channel(), representative.title(), representative.body());
    }
}
