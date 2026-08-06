package com.ssoss.ssossbackend.hashtag.application.service;

import java.util.List;
import java.util.Set;

import com.ssoss.ssossbackend.hashtag.application.command.HashtagBundleListCommand;
import com.ssoss.ssossbackend.hashtag.application.result.HashtagBundleListResult;
import com.ssoss.ssossbackend.hashtag.application.result.HashtagBundleResult;
import com.ssoss.ssossbackend.hashtag.domain.model.HashtagBundle;
import com.ssoss.ssossbackend.hashtag.domain.service.HashtagBundleBookmarkWriter;
import com.ssoss.ssossbackend.hashtag.domain.service.HashtagBundleFinder;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HashtagBundleService {

    private final HashtagBundleFinder hashtagBundleFinder;
    private final HashtagBundleBookmarkWriter hashtagBundleBookmarkWriter;

    public HashtagBundleListResult list(HashtagBundleListCommand command) {
        Page<HashtagBundle> found =
            hashtagBundleFinder.list(command.keyword(), command.page(), command.size());
        Set<Long> bookmarkedIds = hashtagBundleFinder.findBookmarkedIds(command.memberId(),
            found.getContent().stream().map(HashtagBundle::getId).toList());
        return HashtagBundleListResult.from(found, bookmarkedIds);
    }

    public List<HashtagBundleResult> listBookmarked(Long memberId) {
        return hashtagBundleFinder.listBookmarked(memberId).stream()
            .map(bundle -> HashtagBundleResult.from(bundle, true))
            .toList();
    }

    public void bookmark(Long memberId, Long bundleId) {
        hashtagBundleBookmarkWriter.bookmark(hashtagBundleFinder.get(bundleId), memberId);
    }

    public void unbookmark(Long memberId, Long bundleId) {
        hashtagBundleBookmarkWriter.unbookmark(memberId, bundleId);
    }
}
