package com.ssoss.ssossbackend.template.infrastructure.persistence;

import com.ssoss.ssossbackend.persistence.LikePattern;
import com.ssoss.ssossbackend.template.domain.contract.TemplateSearchRepository;
import com.ssoss.ssossbackend.template.domain.model.Template;
import com.ssoss.ssossbackend.template.domain.model.TemplateCategory;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class DataJdbcTemplateSearchRepository implements TemplateSearchRepository {

    private final TemplateSearchQueries searchQueries;

    @Override
    public Page<Template> searchByKeyword(String keyword, TemplateCategory category, Pageable pageable) {
        String pattern = LikePattern.forPartialMatch(keyword);
        String categoryName = category == null ? null : category.name();
        return PageableExecutionUtils.getPage(
            searchQueries.search(pattern, categoryName, pageable.getPageSize(), pageable.getOffset()),
            pageable,
            () -> searchQueries.countMatches(pattern, categoryName));
    }
}
