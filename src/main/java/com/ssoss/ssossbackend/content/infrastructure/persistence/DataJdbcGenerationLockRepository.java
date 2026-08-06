package com.ssoss.ssossbackend.content.infrastructure.persistence;

import java.time.Instant;

import com.ssoss.ssossbackend.content.domain.contract.GenerationLockRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class DataJdbcGenerationLockRepository implements GenerationLockRepository {

    private final GenerationLockQueries lockQueries;

    @Override
    public void acquire(Long memberId, Instant acquiredAt) {
        lockQueries.acquire(memberId, acquiredAt);
    }

    @Override
    public void deleteAllByMemberId(Long memberId) {
        lockQueries.deleteAllByMemberId(memberId);
    }
}
