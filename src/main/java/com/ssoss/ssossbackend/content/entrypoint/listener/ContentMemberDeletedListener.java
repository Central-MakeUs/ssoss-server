package com.ssoss.ssossbackend.content.entrypoint.listener;

import com.ssoss.ssossbackend.content.application.service.ContentService;
import com.ssoss.ssossbackend.member.event.MemberDeletedEvent;

import lombok.RequiredArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentMemberDeletedListener {

    private final ContentService contentService;

    @EventListener
    public void deleteContentsAndGenerations(MemberDeletedEvent event) {
        contentService.deleteAllByMemberId(event.memberId());
    }
}
