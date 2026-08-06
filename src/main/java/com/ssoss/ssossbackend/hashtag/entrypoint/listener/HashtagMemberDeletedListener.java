package com.ssoss.ssossbackend.hashtag.entrypoint.listener;

import com.ssoss.ssossbackend.hashtag.application.service.HashtagBundleService;
import com.ssoss.ssossbackend.member.event.MemberDeletedEvent;

import lombok.RequiredArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HashtagMemberDeletedListener {

    private final HashtagBundleService hashtagBundleService;

    @EventListener
    public void deleteBookmarks(MemberDeletedEvent event) {
        hashtagBundleService.deleteBookmarksByMemberId(event.memberId());
    }
}
