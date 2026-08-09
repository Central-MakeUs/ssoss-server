package com.ssoss.ssossbackend.template.entrypoint.listener;

import com.ssoss.ssossbackend.member.event.MemberDeletedEvent;
import com.ssoss.ssossbackend.template.application.service.SavedTemplateService;
import com.ssoss.ssossbackend.template.application.service.TemplateService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TemplateMemberDeletedListener {

    private final SavedTemplateService savedTemplateService;
    private final TemplateService templateService;

    @EventListener
    public void deleteSavedTemplates(MemberDeletedEvent event) {
        savedTemplateService.deleteAllByMemberId(event.memberId());
    }

    @EventListener
    public void deleteBookmarks(MemberDeletedEvent event) {
        templateService.deleteBookmarksByMemberId(event.memberId());
    }
}
