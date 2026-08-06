package com.ssoss.ssossbackend.hashtag.infrastructure.persistence;

import java.util.List;

import com.ssoss.ssossbackend.hashtag.domain.model.HashtagBundle;
import com.ssoss.ssossbackend.persistence.LikePattern;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;

interface HashtagBundleSearchQueries extends Repository<HashtagBundle, Long> {

    String KEYWORD_MATCHES = "(name LIKE :pattern ESCAPE '" + LikePattern.ESCAPE + "'"
        + " OR JSON_SEARCH(hashtags, 'one', :pattern, '" + LikePattern.ESCAPE + "') IS NOT NULL)";

    @Query("SELECT * FROM hashtag_bundle WHERE " + KEYWORD_MATCHES
        + " ORDER BY id DESC LIMIT :limit OFFSET :offset")
    List<HashtagBundle> search(String pattern, int limit, long offset);

    @Query("SELECT COUNT(*) FROM hashtag_bundle WHERE " + KEYWORD_MATCHES)
    long countMatches(String pattern);
}
