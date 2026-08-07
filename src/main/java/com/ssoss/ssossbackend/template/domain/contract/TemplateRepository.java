package com.ssoss.ssossbackend.template.domain.contract;

import com.ssoss.ssossbackend.template.domain.model.Template;
import com.ssoss.ssossbackend.template.domain.model.TemplateCategory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TemplateRepository extends PagingAndSortingRepository<Template, Long> {

    Page<Template> findAllByCategory(TemplateCategory category, Pageable pageable);
}
