package com.ssoss.ssossbackend.template.domain.contract;

import java.util.List;
import java.util.Optional;

import com.ssoss.ssossbackend.template.domain.model.TemplateBookmark;

import org.springframework.data.repository.ListCrudRepository;

public interface TemplateBookmarkRepository extends ListCrudRepository<TemplateBookmark, Long> {

    List<TemplateBookmark> findAllByMemberIdAndBookmarkedAtIsNotNullOrderByBookmarkedAtDescIdDesc(Long memberId);

    List<TemplateBookmark> findAllByMemberIdAndTemplateIdInAndBookmarkedAtIsNotNull(
        Long memberId, List<Long> templateIds);

    Optional<TemplateBookmark> findByMemberIdAndTemplateId(Long memberId, Long templateId);

    void deleteAllByMemberId(Long memberId);
}
