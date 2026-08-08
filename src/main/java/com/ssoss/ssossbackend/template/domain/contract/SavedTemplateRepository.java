package com.ssoss.ssossbackend.template.domain.contract;

import java.util.List;
import java.util.Optional;

import com.ssoss.ssossbackend.template.domain.model.SavedTemplate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;

public interface SavedTemplateRepository extends ListCrudRepository<SavedTemplate, Long> {

    Optional<SavedTemplate> findByIdAndMemberIdAndDeletedAtIsNull(Long id, Long memberId);

    Page<SavedTemplate> findAllByMemberIdAndDeletedAtIsNull(Long memberId, Pageable pageable);

    List<SavedTemplate> findAllByMemberId(Long memberId);

    void deleteAllByMemberId(Long memberId);
}
