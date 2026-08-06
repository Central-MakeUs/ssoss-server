package com.ssoss.ssossbackend.content.domain.contract;

import java.time.Instant;

public interface GenerationLockRepository {

    void acquire(Long memberId, Instant acquiredAt);

    void deleteAllByMemberId(Long memberId);
}
