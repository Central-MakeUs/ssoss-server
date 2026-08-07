package com.ssoss.ssossbackend.template.entrypoint.listener;

import com.ssoss.ssossbackend.member.event.MemberDeletedEvent;
import com.ssoss.ssossbackend.template.application.service.SavedTemplateService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TemplateMemberDeletedListener {

    private final SavedTemplateService savedTemplateService;

    @EventListener
    public void deleteSavedTemplates(MemberDeletedEvent event) {
        savedTemplateService.deleteAllByMemberId(event.memberId());
    }
}
