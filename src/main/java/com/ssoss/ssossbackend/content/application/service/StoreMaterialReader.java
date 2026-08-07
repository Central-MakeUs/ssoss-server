package com.ssoss.ssossbackend.content.application.service;

import java.time.format.TextStyle;
import java.util.Locale;

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
            profile.businessDays().stream().map(day -> day.getDisplayName(TextStyle.FULL, Locale.KOREAN)).toList(),
            profile.openTime(), profile.closeTime(), profile.signatureMenus(), profile.amenities());
    }
}
