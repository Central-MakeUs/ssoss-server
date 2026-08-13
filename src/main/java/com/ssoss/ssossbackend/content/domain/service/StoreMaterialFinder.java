package com.ssoss.ssossbackend.content.domain.service;

import com.ssoss.ssossbackend.content.domain.contract.StoreMaterialClient;
import com.ssoss.ssossbackend.content.domain.model.StoreMaterial;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreMaterialFinder {

    private final StoreMaterialClient storeMaterialClient;

    public StoreMaterial get(Long memberId) {
        return storeMaterialClient.get(memberId);
    }
}
