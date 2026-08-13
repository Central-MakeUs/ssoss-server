package com.ssoss.ssossbackend.template.infrastructure.gateway;

import com.ssoss.ssossbackend.store.entrypoint.gateway.StoreInternalGateway;
import com.ssoss.ssossbackend.store.entrypoint.gateway.StoreProfileReply;
import com.ssoss.ssossbackend.template.domain.contract.StoreInfoClient;
import com.ssoss.ssossbackend.template.domain.model.StoreInfo;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class InternalStoreInfoClient implements StoreInfoClient {

    private final StoreInternalGateway storeInternalGateway;

    @Override
    public StoreInfo get(Long memberId) {
        StoreProfileReply profile = storeInternalGateway.getProfile(memberId);
        return StoreInfo.of(profile.name(), profile.address(),
            profile.businessDays(), profile.openTime(), profile.closeTime());
    }
}
