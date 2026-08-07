package com.ssoss.ssossbackend.template.domain.model;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

public enum StorePlaceholder {

    STORE_NAME("[가게명]", StoreInfo::name),
    ADDRESS("[주소]", StoreInfo::address),
    BUSINESS_HOURS("[영업시간]", storeInfo -> storeInfo.operatingHours().format());

    private static final Map<String, StorePlaceholder> BY_MARK = Arrays.stream(values())
        .collect(Collectors.toMap(placeholder -> placeholder.mark, placeholder -> placeholder));
    private static final Pattern ANY_MARK = Pattern.compile(Arrays.stream(values())
        .map(placeholder -> Pattern.quote(placeholder.mark))
        .collect(Collectors.joining("|")));

    private final String mark;
    private final Function<StoreInfo, String> source;

    StorePlaceholder(String mark, Function<StoreInfo, String> source) {
        this.mark = mark;
        this.source = source;
    }

    static String replaceAll(String body, StoreInfo storeInfo) {
        return ANY_MARK.matcher(body).replaceAll(match -> {
            String value = BY_MARK.get(match.group()).source.apply(storeInfo);
            return Matcher.quoteReplacement(StringUtils.hasText(value) ? value : match.group());
        });
    }
}
