package com.ssoss.ssossbackend.hashtag.application.result;

import java.util.List;

import com.ssoss.ssossbackend.hashtag.domain.model.HashtagBundle;

import org.springframework.data.domain.Page;

public record HashtagBundleListResult(long totalCount, int page, int size, boolean hasNext, List<Item> bundles) {

    public static HashtagBundleListResult from(Page<HashtagBundle> found) {
        return new HashtagBundleListResult(found.getTotalElements(), found.getNumber(), found.getSize(),
            found.hasNext(), found.getContent().stream().map(Item::from).toList());
    }

    public record Item(Long id, String name, List<String> hashtags) {

        public static Item from(HashtagBundle bundle) {
            return new Item(bundle.getId(), bundle.getName(), bundle.hashtagList());
        }
    }
}
