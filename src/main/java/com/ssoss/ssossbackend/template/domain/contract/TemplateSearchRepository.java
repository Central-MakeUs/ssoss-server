package com.ssoss.ssossbackend.template.domain.contract;

import com.ssoss.ssossbackend.template.domain.model.Template;
import com.ssoss.ssossbackend.template.domain.model.TemplateCategory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TemplateSearchRepository {

    Page<Template> searchByKeyword(String keyword, TemplateCategory category, Pageable pageable);
}
