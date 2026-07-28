package com.ssoss.ssossbackend.store.entrypoint.controller;

import com.ssoss.ssossbackend.store.application.result.StoreBasicInfoResult;
import com.ssoss.ssossbackend.store.application.result.StoreContentInfoResult;
import com.ssoss.ssossbackend.store.application.result.StoreOperationInfoResult;
import com.ssoss.ssossbackend.store.application.result.StoreInfoResult;
import com.ssoss.ssossbackend.store.application.service.StoreService;
import com.ssoss.ssossbackend.store.entrypoint.request.StoreBasicInfoRequest;
import com.ssoss.ssossbackend.store.entrypoint.request.StoreOperationInfoRequest;
import com.ssoss.ssossbackend.store.entrypoint.response.StoreBasicInfoResponse;
import com.ssoss.ssossbackend.store.entrypoint.response.StoreContentInfoResponse;
import com.ssoss.ssossbackend.store.entrypoint.response.StoreOperationInfoResponse;
import com.ssoss.ssossbackend.store.entrypoint.response.StoreInfoResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class StoreInfoController implements StoreInfoApi {

    private final StoreService storeService;

    @Override
    @GetMapping("/v1/stores/me")
    public StoreInfoResponse getInfo(@AuthenticationPrincipal Long memberId) {
        StoreInfoResult result = storeService.getInfo(memberId);
        StoreBasicInfoResult basic = result.basic();
        StoreOperationInfoResult operation = result.operation();
        StoreContentInfoResult content = result.content();
        return new StoreInfoResponse(
            new StoreBasicInfoResponse(basic.name(), basic.type(), basic.address(), basic.introduction(),
                basic.status()),
            new StoreOperationInfoResponse(operation.businessDays(), operation.openTime(), operation.closeTime(),
                operation.signatureMenus(), operation.takeoutAvailable(), operation.reservationAvailable(),
                operation.parkingAvailable(), operation.status()),
            new StoreContentInfoResponse(content.strength(), content.keywords(), content.forbidden(), content.tone(),
                content.status()));
    }

    @Override
    @PutMapping("/v1/stores/me/basic")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveBasicInfo(
        @AuthenticationPrincipal Long memberId,
        @Valid @RequestBody StoreBasicInfoRequest request
    ) {
        storeService.saveBasicInfo(request.toCommand(memberId));
    }

    @Override
    @PutMapping("/v1/stores/me/operation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveOperationInfo(
        @AuthenticationPrincipal Long memberId,
        @Valid @RequestBody StoreOperationInfoRequest request
    ) {
        storeService.saveOperationInfo(request.toCommand(memberId));
    }
}
