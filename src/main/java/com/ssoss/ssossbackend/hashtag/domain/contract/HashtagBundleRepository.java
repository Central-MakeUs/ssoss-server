package com.ssoss.ssossbackend.hashtag.domain.contract;

import com.ssoss.ssossbackend.hashtag.domain.model.HashtagBundle;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface HashtagBundleRepository extends PagingAndSortingRepository<HashtagBundle, Long> {
}
