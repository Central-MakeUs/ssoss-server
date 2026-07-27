package com.ssoss.ssossbackend.app.domain.model;

import com.ssoss.ssossbackend.shared.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AppErrorCode implements ErrorCode {

    UNSUPPORTED_APP_OS("AP0001", HttpStatus.BAD_REQUEST, "지원하지 않는 OS 입니다"),
    APP_VERSION_NOT_FOUND("AP0002", HttpStatus.INTERNAL_SERVER_ERROR, "앱 버전 정보를 찾을 수 없습니다. 잠시 후 다시 시도해 주세요"),
    INVALID_APP_VERSION("AP0003", HttpStatus.BAD_REQUEST, "앱 버전 형식이 올바르지 않습니다");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
