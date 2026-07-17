package com.ssoss.ssossbackend.member.domain.model;

import com.ssoss.ssossbackend.shared.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TermErrorCode implements ErrorCode {

    REQUIRED_TERMS_NOT_AGREED("T0001", HttpStatus.BAD_REQUEST, "필수 약관에 모두 동의해야 회원가입할 수 있습니다");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
