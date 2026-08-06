package com.ssoss.ssossbackend.content.infrastructure.persistence;

import java.time.Instant;

import com.ssoss.ssossbackend.content.domain.model.GenerationLock;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;

interface GenerationLockQueries extends Repository<GenerationLock, Long> {

    @Modifying
    @Query("INSERT INTO generation_lock (member_id, created_at) VALUES (:memberId, :acquiredAt) "
        + "ON DUPLICATE KEY UPDATE member_id = member_id")
    void acquire(Long memberId, Instant acquiredAt);
}
