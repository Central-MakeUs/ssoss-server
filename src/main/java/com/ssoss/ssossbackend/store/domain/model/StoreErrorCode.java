package com.ssoss.ssossbackend.store.domain.model;

import com.ssoss.ssossbackend.shared.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StoreErrorCode implements ErrorCode {

    STORE_NOT_FOUND("ST0001", HttpStatus.INTERNAL_SERVER_ERROR, "매장 정보를 찾을 수 없습니다. 잠시 후 다시 시도해 주세요");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
