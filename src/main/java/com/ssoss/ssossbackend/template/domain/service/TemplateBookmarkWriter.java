package com.ssoss.ssossbackend.template.domain.service;

import java.time.Clock;

import com.ssoss.ssossbackend.template.domain.contract.TemplateBookmarkRepository;
import com.ssoss.ssossbackend.template.domain.model.Template;
import com.ssoss.ssossbackend.template.domain.model.TemplateBookmark;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TemplateBookmarkWriter {

    private final TemplateBookmarkRepository templateBookmarkRepository;
    private final Clock clock;

    public void bookmark(Template template, Long memberId) {
        TemplateBookmark bookmark = templateBookmarkRepository
            .findByMemberIdAndTemplateId(memberId, template.getId())
            .orElseGet(() -> TemplateBookmark.create(memberId, template.getId()));
        if (!bookmark.bookmark(clock.instant())) {
            return;
        }
        templateBookmarkRepository.save(bookmark);
    }

    public void unbookmark(Long memberId, Long templateId) {
        templateBookmarkRepository.findByMemberIdAndTemplateId(memberId, templateId)
            .ifPresent(bookmark -> {
                if (bookmark.unbookmark()) {
                    templateBookmarkRepository.save(bookmark);
                }
            });
    }
}
