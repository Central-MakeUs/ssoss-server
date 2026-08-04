package com.ssoss.ssossbackend.hashtag.domain.model;

import java.time.Instant;
import java.util.List;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("hashtag_bundle")
public class HashtagBundle {

    @Id
    private Long id;
    private String name;
    private Hashtags hashtags;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    HashtagBundle(Long id, String name, Hashtags hashtags) {
        this.id = id;
        this.name = name;
        this.hashtags = hashtags;
    }

    public List<String> hashtagList() {
        return hashtags == null ? List.of() : hashtags.values();
    }
}
