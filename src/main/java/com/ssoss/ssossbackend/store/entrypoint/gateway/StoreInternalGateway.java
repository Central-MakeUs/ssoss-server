package com.ssoss.ssossbackend.store.entrypoint.gateway;

import com.ssoss.ssossbackend.store.application.service.StoreService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoreInternalGateway {

    private final StoreService storeService;

    public StoreProfileReply getProfile(Long memberId) {
        return StoreProfileReply.from(storeService.getProfile(memberId));
    }
}
