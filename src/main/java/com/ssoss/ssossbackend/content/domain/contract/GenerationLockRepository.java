package com.ssoss.ssossbackend.content.domain.contract;

import java.util.Optional;

import com.ssoss.ssossbackend.content.domain.model.GenerationLock;

import org.springframework.data.relational.core.sql.LockMode;
import org.springframework.data.relational.repository.Lock;
import org.springframework.data.repository.ListCrudRepository;

public interface GenerationLockRepository extends ListCrudRepository<GenerationLock, Long> {

    boolean existsByMemberId(Long memberId);

    @Lock(LockMode.PESSIMISTIC_WRITE)
    Optional<GenerationLock> findByMemberId(Long memberId);
}
