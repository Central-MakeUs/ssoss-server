package com.ssoss.ssossbackend.hashtag.application.result;

import java.util.List;
import java.util.Set;

import com.ssoss.ssossbackend.hashtag.domain.model.HashtagBundle;

import org.springframework.data.domain.Page;

public record HashtagBundleListResult(long totalCount, int page, int size, boolean hasNext,
                                      List<HashtagBundleResult> bundles) {

    public static HashtagBundleListResult from(Page<HashtagBundle> found, Set<Long> bookmarkedIds) {
        return new HashtagBundleListResult(found.getTotalElements(), found.getNumber(), found.getSize(),
            found.hasNext(), found.getContent().stream()
            .map(bundle -> HashtagBundleResult.from(bundle, bookmarkedIds.contains(bundle.getId())))
            .toList());
    }
}
