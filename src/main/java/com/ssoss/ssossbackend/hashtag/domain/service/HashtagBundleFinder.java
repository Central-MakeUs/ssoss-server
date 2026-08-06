package com.ssoss.ssossbackend.hashtag.domain.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.ssoss.ssossbackend.hashtag.domain.contract.HashtagBundleBookmarkRepository;
import com.ssoss.ssossbackend.hashtag.domain.contract.HashtagBundleRepository;
import com.ssoss.ssossbackend.hashtag.domain.model.HashtagBundle;
import com.ssoss.ssossbackend.hashtag.domain.model.HashtagBundleBookmark;
import com.ssoss.ssossbackend.hashtag.domain.model.HashtagErrorCode;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HashtagBundleFinder {

    private static final Sort LATEST_FIRST = Sort.by(Sort.Direction.DESC, "id");

    private final HashtagBundleRepository hashtagBundleRepository;
    private final HashtagBundleBookmarkRepository hashtagBundleBookmarkRepository;

    public Page<HashtagBundle> list(int page, int size) {
        return hashtagBundleRepository.findAll(PageRequest.of(page, size, LATEST_FIRST));
    }

    public HashtagBundle get(Long bundleId) {
        return hashtagBundleRepository.findById(bundleId)
            .orElseThrow(() -> new BusinessException(HashtagErrorCode.HASHTAG_BUNDLE_NOT_FOUND));
    }

    public Set<Long> findBookmarkedIds(Long memberId, List<Long> bundleIds) {
        if (bundleIds.isEmpty()) {
            return Set.of();
        }
        return hashtagBundleBookmarkRepository
            .findAllByMemberIdAndBundleIdInAndBookmarkedAtIsNotNull(memberId, bundleIds).stream()
            .map(HashtagBundleBookmark::getBundleId)
            .collect(Collectors.toUnmodifiableSet());
    }

    public List<HashtagBundle> listBookmarked(Long memberId) {
        List<Long> bookmarkedIds = hashtagBundleBookmarkRepository
            .findAllByMemberIdAndBookmarkedAtIsNotNull(memberId).stream()
            .map(HashtagBundleBookmark::getBundleId)
            .toList();
        if (bookmarkedIds.isEmpty()) {
            return List.of();
        }
        return hashtagBundleRepository.findAllByIdInOrderByIdDesc(bookmarkedIds);
    }
}
