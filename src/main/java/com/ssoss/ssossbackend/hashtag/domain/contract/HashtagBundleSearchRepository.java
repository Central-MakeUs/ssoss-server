package com.ssoss.ssossbackend.hashtag.domain.contract;

import com.ssoss.ssossbackend.hashtag.domain.model.HashtagBundle;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HashtagBundleSearchRepository {

    Page<HashtagBundle> searchByKeyword(String keyword, Pageable pageable);
}
