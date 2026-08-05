package com.ssoss.ssossbackend.hashtag.domain.service;

import java.time.Clock;

import com.ssoss.ssossbackend.hashtag.domain.contract.HashtagBundleBookmarkRepository;
import com.ssoss.ssossbackend.hashtag.domain.model.HashtagBundle;
import com.ssoss.ssossbackend.hashtag.domain.model.HashtagBundleBookmark;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HashtagBundleBookmarkWriter {

    private final HashtagBundleBookmarkRepository hashtagBundleBookmarkRepository;
    private final Clock clock;

    public void bookmark(HashtagBundle bundle, Long memberId) {
        HashtagBundleBookmark bookmark = hashtagBundleBookmarkRepository
            .findByMemberIdAndBundleId(memberId, bundle.getId())
            .orElseGet(() -> HashtagBundleBookmark.create(memberId, bundle.getId()));
        if (!bookmark.bookmark(clock.instant())) {
            return;
        }
        hashtagBundleBookmarkRepository.save(bookmark);
    }
}
