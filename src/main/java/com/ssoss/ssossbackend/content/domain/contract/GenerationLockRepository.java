package com.ssoss.ssossbackend.content.domain.contract;

import java.time.Instant;

import com.ssoss.ssossbackend.content.domain.model.GenerationLock;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

public interface GenerationLockRepository extends ListCrudRepository<GenerationLock, Long> {

    @Modifying
    @Query("INSERT INTO generation_lock (member_id, created_at) VALUES (:memberId, :acquiredAt) "
        + "ON DUPLICATE KEY UPDATE member_id = member_id")
    void acquire(Long memberId, Instant acquiredAt);
}
