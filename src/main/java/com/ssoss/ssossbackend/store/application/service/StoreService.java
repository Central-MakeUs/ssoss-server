package com.ssoss.ssossbackend.store.application.service;

import com.ssoss.ssossbackend.shared.exception.BusinessException;
import com.ssoss.ssossbackend.store.application.result.StoreInfoResult;
import com.ssoss.ssossbackend.store.domain.model.StoreErrorCode;
import com.ssoss.ssossbackend.store.domain.service.StoreFinder;
import com.ssoss.ssossbackend.store.domain.service.StoreWriter;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreFinder storeFinder;
    private final StoreWriter storeWriter;

    public StoreInfoResult getInfo(Long memberId) {
        return StoreInfoResult.from(storeFinder.find(memberId)
            .orElseThrow(() -> new BusinessException(StoreErrorCode.STORE_NOT_FOUND)));
    }

    public void create(Long memberId) {
        storeWriter.create(memberId);
    }

    public void deleteByMemberId(Long memberId) {
        storeWriter.deleteByMemberId(memberId);
    }
}
