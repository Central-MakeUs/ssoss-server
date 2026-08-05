package com.ssoss.ssossbackend.hashtag.application.result;

import java.util.List;

import com.ssoss.ssossbackend.hashtag.domain.model.HashtagBundle;

import org.springframework.data.domain.Page;

public record HashtagBundleListResult(long totalCount, int page, int size, boolean hasNext,
                                      List<HashtagBundleResult> bundles) {

    public static HashtagBundleListResult from(Page<HashtagBundle> found) {
        return new HashtagBundleListResult(found.getTotalElements(), found.getNumber(), found.getSize(),
            found.hasNext(), found.getContent().stream().map(HashtagBundleResult::from).toList());
    }
}
