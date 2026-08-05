package com.ssoss.ssossbackend.hashtag.domain.contract;

import java.util.List;

import com.ssoss.ssossbackend.hashtag.domain.model.HashtagBundleBookmark;

import org.springframework.data.repository.ListCrudRepository;

public interface HashtagBundleBookmarkRepository extends ListCrudRepository<HashtagBundleBookmark, Long> {

    List<HashtagBundleBookmark> findAllByMemberIdAndBookmarkedAtIsNotNull(Long memberId);
}
