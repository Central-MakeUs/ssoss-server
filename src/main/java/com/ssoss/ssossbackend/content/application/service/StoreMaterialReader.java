package com.ssoss.ssossbackend.content.application.service;

import com.ssoss.ssossbackend.content.domain.model.StoreMaterial;
import com.ssoss.ssossbackend.store.application.service.StoreProfile;
import com.ssoss.ssossbackend.store.application.service.StoreService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreMaterialReader {

    private final StoreService storeService;

    public StoreMaterial read(Long memberId) {
        StoreProfile profile = storeService.getProfile(memberId);
        return new StoreMaterial(profile.name(), profile.type(), profile.address(), profile.introduction(),
            profile.businessDays(), profile.openTime(), profile.closeTime(), profile.signatureMenus(),
            profile.amenities());
    }
}
