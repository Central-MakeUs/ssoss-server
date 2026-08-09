package com.ssoss.ssossbackend.template.domain.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ssoss.ssossbackend.shared.exception.BusinessException;
import com.ssoss.ssossbackend.template.domain.contract.TemplateBookmarkRepository;
import com.ssoss.ssossbackend.template.domain.contract.TemplateRepository;
import com.ssoss.ssossbackend.template.domain.contract.TemplateSearchRepository;
import com.ssoss.ssossbackend.template.domain.model.Template;
import com.ssoss.ssossbackend.template.domain.model.TemplateBookmark;
import com.ssoss.ssossbackend.template.domain.model.TemplateCategory;
import com.ssoss.ssossbackend.template.domain.model.TemplateErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TemplateFinder {

    private static final Sort LATEST_FIRST = Sort.by(Sort.Direction.DESC, "id");

    private final TemplateRepository templateRepository;
    private final TemplateSearchRepository templateSearchRepository;
    private final TemplateBookmarkRepository templateBookmarkRepository;

    public Page<Template> list(TemplateCategory category, String keyword, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, LATEST_FIRST);
        if (StringUtils.hasText(keyword)) {
            return templateSearchRepository.searchByKeyword(keyword, category, pageRequest);
        }
        if (category == null) {
            return templateRepository.findAll(pageRequest);
        }
        return templateRepository.findAllByCategory(category, pageRequest);
    }

    public Template get(Long templateId) {
        return templateRepository.findById(templateId)
            .orElseThrow(() -> new BusinessException(TemplateErrorCode.TEMPLATE_NOT_FOUND));
    }

    public List<Template> listBookmarked(Long memberId) {
        List<Long> bookmarkedIds = templateBookmarkRepository
            .findAllByMemberIdAndBookmarkedAtIsNotNullOrderByBookmarkedAtDescIdDesc(memberId).stream()
            .map(TemplateBookmark::getTemplateId)
            .toList();
        if (bookmarkedIds.isEmpty()) {
            return List.of();
        }
        Map<Long, Template> byId = templateRepository.findAllByIdIn(bookmarkedIds).stream()
            .collect(Collectors.toMap(Template::getId, Function.identity()));
        return bookmarkedIds.stream().map(byId::get).toList();
    }

    public Set<Long> findBookmarkedIds(Long memberId, List<Long> templateIds) {
        if (templateIds.isEmpty()) {
            return Set.of();
        }
        return templateBookmarkRepository
            .findAllByMemberIdAndTemplateIdInAndBookmarkedAtIsNotNull(memberId, templateIds).stream()
            .map(TemplateBookmark::getTemplateId)
            .collect(Collectors.toUnmodifiableSet());
    }
}
