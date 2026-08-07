package com.ssoss.ssossbackend.template.domain.contract;

import com.ssoss.ssossbackend.template.domain.model.SavedTemplate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;

public interface SavedTemplateRepository extends ListCrudRepository<SavedTemplate, Long> {

    Page<SavedTemplate> findAllByMemberIdAndDeletedAtIsNull(Long memberId, Pageable pageable);

    void deleteAllByMemberId(Long memberId);
}
