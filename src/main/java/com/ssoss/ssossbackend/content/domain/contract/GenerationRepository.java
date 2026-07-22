package com.ssoss.ssossbackend.content.domain.contract;

import java.time.Instant;
import java.util.Optional;

import com.ssoss.ssossbackend.content.domain.model.Generation;

import org.springframework.data.repository.ListCrudRepository;

public interface GenerationRepository extends ListCrudRepository<Generation, Long> {

    Optional<Generation> findByIdAndMemberId(Long id, Long memberId);

    boolean existsByMemberIdAndFinishedAtIsNullAndCreatedAtGreaterThanEqual(Long memberId, Instant threshold);
}
