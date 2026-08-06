package com.ssoss.ssossbackend.hashtag.application.result;

import java.util.List;

import com.ssoss.ssossbackend.hashtag.domain.model.HashtagBundle;

public record HashtagBundleResult(Long id, String name, List<String> hashtags, boolean bookmarked) {

    public static HashtagBundleResult from(HashtagBundle bundle, boolean bookmarked) {
        return new HashtagBundleResult(bundle.getId(), bundle.getName(), bundle.hashtagList(), bookmarked);
    }
}
