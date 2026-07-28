package com.ssoss.ssossbackend.store.domain.service;

import com.ssoss.ssossbackend.shared.exception.BusinessException;
import com.ssoss.ssossbackend.store.domain.contract.StoreRepository;
import com.ssoss.ssossbackend.store.domain.model.Store;
import com.ssoss.ssossbackend.store.domain.model.StoreErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreFinder {

    private final StoreRepository storeRepository;

    public Store get(Long memberId) {
        return storeRepository.findByMemberId(memberId)
            .orElseThrow(() -> new BusinessException(StoreErrorCode.STORE_NOT_FOUND));
    }
}
