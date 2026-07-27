package com.ssoss.ssossbackend.store.entrypoint.listener;

import com.ssoss.ssossbackend.member.application.event.MemberDeletedEvent;
import com.ssoss.ssossbackend.store.application.service.StoreService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoreMemberDeletedListener {

    private final StoreService storeService;

    @EventListener
    public void deleteStore(MemberDeletedEvent event) {
        storeService.deleteByMemberId(event.memberId());
    }
}
