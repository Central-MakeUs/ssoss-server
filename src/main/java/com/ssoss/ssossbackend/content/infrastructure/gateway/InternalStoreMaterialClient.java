package com.ssoss.ssossbackend.content.infrastructure.gateway;

import com.ssoss.ssossbackend.content.domain.contract.StoreMaterialClient;
import com.ssoss.ssossbackend.content.domain.model.StoreMaterial;
import com.ssoss.ssossbackend.store.entrypoint.gateway.StoreInternalGateway;
import com.ssoss.ssossbackend.store.entrypoint.gateway.StoreProfileReply;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class InternalStoreMaterialClient implements StoreMaterialClient {

    private final StoreInternalGateway storeInternalGateway;

    @Override
    public StoreMaterial get(Long memberId) {
        StoreProfileReply profile = storeInternalGateway.getProfile(memberId);
        return StoreMaterial.of(profile.name(), profile.type(), profile.address(), profile.introduction(),
            profile.businessDays(), profile.openTime(), profile.closeTime(),
            profile.signatureMenus(), profile.amenities());
    }
}
