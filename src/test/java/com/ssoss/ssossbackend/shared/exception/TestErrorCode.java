package com.ssoss.ssossbackend.shared.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TestErrorCode implements ErrorCode {

    SAMPLE_CONFLICT("T0001", HttpStatus.CONFLICT, "이미 처리된 요청입니다");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
