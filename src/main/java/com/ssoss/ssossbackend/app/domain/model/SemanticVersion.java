package com.ssoss.ssossbackend.app.domain.model;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ssoss.ssossbackend.shared.exception.BusinessException;

public record SemanticVersion(int major, int minor, int patch) {

    private static final Pattern FORMAT = Pattern.compile("(\\d{1,4})\\.(\\d{1,4})\\.(\\d{1,4})");

    private static final Comparator<SemanticVersion> ORDER = Comparator
        .comparingInt(SemanticVersion::major)
        .thenComparingInt(SemanticVersion::minor)
        .thenComparingInt(SemanticVersion::patch);

    public static SemanticVersion from(String value) {
        Matcher matcher = FORMAT.matcher(value == null ? "" : value);
        if (!matcher.matches()) {
            throw new BusinessException(AppErrorCode.INVALID_APP_VERSION);
        }
        return new SemanticVersion(
            Integer.parseInt(matcher.group(1)),
            Integer.parseInt(matcher.group(2)),
            Integer.parseInt(matcher.group(3)));
    }

    public boolean isLowerThan(SemanticVersion other) {
        return ORDER.compare(this, other) < 0;
    }
}
