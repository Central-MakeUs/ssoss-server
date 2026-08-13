package com.ssoss.ssossbackend.template.domain.service;

import com.ssoss.ssossbackend.template.domain.contract.StoreInfoClient;
import com.ssoss.ssossbackend.template.domain.model.StoreInfo;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreInfoFinder {

    private final StoreInfoClient storeInfoClient;

    public StoreInfo get(Long memberId) {
        return storeInfoClient.get(memberId);
    }
}
