package com.ssoss.ssossbackend.auth.domain.model;

import com.ssoss.ssossbackend.shared.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_SOCIAL_TOKEN("A0001", HttpStatus.UNAUTHORIZED, "소셜 인증에 실패했습니다. 다시 로그인해 주세요"),
    UNSUPPORTED_SOCIAL_PROVIDER("A0002", HttpStatus.NOT_FOUND, "지원하지 않는 소셜 로그인입니다"),
    SOCIAL_PROVIDER_UNAVAILABLE("A0003", HttpStatus.SERVICE_UNAVAILABLE, "소셜 로그인 서비스가 일시적으로 불안정합니다. 잠시 후 다시 시도해 주세요"),
    INVALID_REFRESH_TOKEN("A0004", HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다. 다시 로그인해 주세요"),
    EXPIRED_REFRESH_TOKEN("A0005", HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다. 다시 로그인해 주세요");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
