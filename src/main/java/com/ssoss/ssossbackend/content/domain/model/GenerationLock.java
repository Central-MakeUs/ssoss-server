package com.ssoss.ssossbackend.content.domain.model;

import java.time.Instant;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("generation_lock")
public class GenerationLock {

    @Id
    private Long id;
    private Long memberId;

    @CreatedDate
    private Instant createdAt;

    GenerationLock(Long id, Long memberId) {
        this.id = id;
        this.memberId = memberId;
    }

    public static GenerationLock create(Long memberId) {
        return new GenerationLock(null, memberId);
    }
}
