package com.ssoss.ssossbackend.store.domain.service;

import com.ssoss.ssossbackend.store.domain.contract.StoreRepository;
import com.ssoss.ssossbackend.store.domain.model.BusinessDays;
import com.ssoss.ssossbackend.store.domain.model.SignatureMenus;
import com.ssoss.ssossbackend.store.domain.model.Store;
import com.ssoss.ssossbackend.store.domain.model.StoreType;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreWriter {

    private final StoreRepository storeRepository;

    public void create(Long memberId) {
        storeRepository.save(Store.create(memberId));
    }

    public void writeBasicInfo(Store store, String name, StoreType type, String address, String introduction) {
        store.writeBasicInfo(name, type, address, introduction);
        storeRepository.save(store);
    }

    public void writeOperationInfo(Store store, BusinessDays businessDays, String openTime, String closeTime,
        SignatureMenus signatureMenus, boolean takeoutAvailable, boolean reservationAvailable,
        boolean parkingAvailable) {
        store.writeOperationInfo(businessDays, openTime, closeTime, signatureMenus, takeoutAvailable,
            reservationAvailable, parkingAvailable);
        storeRepository.save(store);
    }

    public void deleteByMemberId(Long memberId) {
        storeRepository.deleteByMemberId(memberId);
    }
}
