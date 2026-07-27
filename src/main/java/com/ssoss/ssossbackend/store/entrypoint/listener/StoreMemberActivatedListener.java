package com.ssoss.ssossbackend.store.entrypoint.listener;

import com.ssoss.ssossbackend.member.application.event.MemberActivatedEvent;
import com.ssoss.ssossbackend.store.application.service.StoreService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoreMemberActivatedListener {

    private final StoreService storeService;

    @EventListener
    public void createStore(MemberActivatedEvent event) {
        storeService.create(event.memberId());
    }
}
