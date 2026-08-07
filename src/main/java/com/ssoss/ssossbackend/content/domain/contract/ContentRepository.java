package com.ssoss.ssossbackend.content.domain.contract;

import java.util.Optional;

import com.ssoss.ssossbackend.content.domain.model.Content;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;

public interface ContentRepository extends ListCrudRepository<Content, Long> {

    Optional<Content> findByIdAndMemberIdAndDeletedAtIsNull(Long id, Long memberId);

    Page<Content> findAllByMemberIdAndDeletedAtIsNull(Long memberId, Pageable pageable);

    Optional<Content> findByGenerationId(Long generationId);

    void deleteAllByMemberId(Long memberId);
}
