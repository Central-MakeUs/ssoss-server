package com.ssoss.ssossbackend.hashtag.infrastructure.persistence;

import com.ssoss.ssossbackend.hashtag.domain.contract.HashtagBundleSearchRepository;
import com.ssoss.ssossbackend.hashtag.domain.model.HashtagBundle;
import com.ssoss.ssossbackend.persistence.LikePattern;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class DataJdbcHashtagBundleSearchRepository implements HashtagBundleSearchRepository {

    private final HashtagBundleSearchQueries searchQueries;

    @Override
    public Page<HashtagBundle> searchByKeyword(String keyword, Pageable pageable) {
        String pattern = LikePattern.forPartialMatch(keyword);
        return PageableExecutionUtils.getPage(
            searchQueries.search(pattern, pageable.getPageSize(), pageable.getOffset()),
            pageable,
            () -> searchQueries.countMatches(pattern));
    }
}
