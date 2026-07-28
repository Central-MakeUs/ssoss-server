package com.ssoss.ssossbackend.store.application.service;

import com.ssoss.ssossbackend.store.application.command.StoreBasicInfoCommand;
import com.ssoss.ssossbackend.store.application.command.StoreOperationInfoCommand;
import com.ssoss.ssossbackend.store.application.result.StoreInfoResult;
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
        return StoreInfoResult.from(storeFinder.get(memberId));
    }

    public void saveBasicInfo(StoreBasicInfoCommand command) {
        storeWriter.writeBasicInfo(storeFinder.get(command.memberId()), command.name(), command.type(),
            command.address(), command.introduction());
    }

    public void saveOperationInfo(StoreOperationInfoCommand command) {
        storeWriter.writeOperationInfo(storeFinder.get(command.memberId()), command.businessDays(),
            command.openTime(), command.closeTime(), command.signatureMenus(), command.amenities());
    }

    public void create(Long memberId) {
        storeWriter.create(memberId);
    }

    public void deleteByMemberId(Long memberId) {
        storeWriter.deleteByMemberId(memberId);
    }
}
