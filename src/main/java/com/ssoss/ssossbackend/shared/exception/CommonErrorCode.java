package com.ssoss.ssossbackend.shared.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INVALID_INPUT("C0001", HttpStatus.BAD_REQUEST, "입력값을 다시 확인해 주세요"),
    NOT_FOUND("C0002", HttpStatus.NOT_FOUND, "요청하신 정보를 찾을 수 없습니다"),
    METHOD_NOT_ALLOWED("C0003", HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 요청 방식입니다"),
    NOT_ACCEPTABLE("C0004", HttpStatus.NOT_ACCEPTABLE, "요청하신 응답 형식을 지원하지 않습니다"),
    UNSUPPORTED_MEDIA_TYPE("C0005", HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 요청 형식입니다"),
    PAYLOAD_TOO_LARGE("C0006", HttpStatus.PAYLOAD_TOO_LARGE, "요청 용량이 너무 큽니다"),
    INTERNAL_ERROR("C9999", HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
