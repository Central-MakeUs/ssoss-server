package com.ssoss.ssossbackend.hashtag.domain.model;

import java.time.Instant;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("hashtag_bundle_bookmark")
public class HashtagBundleBookmark {

    @Id
    private Long id;
    private Long memberId;
    private Long bundleId;
    private Instant bookmarkedAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    HashtagBundleBookmark(Long id, Long memberId, Long bundleId, Instant bookmarkedAt) {
        this.id = id;
        this.memberId = memberId;
        this.bundleId = bundleId;
        this.bookmarkedAt = bookmarkedAt;
    }
}
